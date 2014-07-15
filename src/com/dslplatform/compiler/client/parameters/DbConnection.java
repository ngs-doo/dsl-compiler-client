package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.Console;
import java.sql.*;
import java.util.*;

public enum DbConnection implements CompileParameter {
	INSTANCE;

	private static String unescape(final String element) {
		return element.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	private static Map<String, String> convertToMap(final String dsls) {
		final Map<String, String> tuples = new LinkedHashMap<String, String>();
		if (dsls == null || dsls.length() == 0) {
			return tuples;
		}
		final int endLength = dsls.length() - 1;
		if (dsls.charAt(0) != '"' || dsls.charAt(endLength) != '"') {
			System.out.print("Invalid DSL found in database. Can't convert to HSTORE: " + dsls);
			System.exit(0);
		}
		final String[] pairs = dsls.substring(1, endLength).split("\", ?\"", -1);
		for (final String pair : pairs) {
			final String[] kv = pair.split("\"=>\"", -1);
			if (kv.length != 2) {
				System.out.print("Invalid DSL found in database. Can't convert to HSTORE: " + dsls);
				System.exit(0);
			}
			tuples.put(unescape(kv[0]), unescape(kv[1]));
		}
		return tuples;
	}

	private static Map.Entry<Map<String, String>, String> cache;

	public static Map<String, String> getDatabaseDsl(final Map<InputParameter, String> parameters) {
		return getDatabaseDslAndVersion(parameters).getKey();
	}

	public static Map.Entry<Map<String, String>, String> getDatabaseDslAndVersion(final Map<InputParameter, String> parameters) {
		if (cache != null) {
			return cache;
		}
		final String value = parameters.get(InputParameter.CONNECTION_STRING);
		final Map.Entry<Map<String, String>, String> emptyResult
				= new AbstractMap.SimpleEntry<Map<String, String>, String>(new HashMap<String, String>(), "");
		final String connectionString = "jdbc:postgresql://" + value;
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(connectionString);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error opening connection to " + connectionString);
			System.out.println(e.getMessage());
			System.exit(0);
		}
		try {
			final ResultSet migrationExist =
					stmt.executeQuery(
							"SELECT EXISTS(SELECT 1 FROM pg_tables " +
									"WHERE schemaname = '-NGS-' AND tablename = 'database_migration')");
			final boolean hasTable = migrationExist.next() && migrationExist.getBoolean(1);
			migrationExist.close();
			if (!hasTable) {
				stmt.close();
				conn.close();
				return cache = emptyResult;
			}
		} catch (SQLException ex) {
			System.out.println("Error checking for migration table in -NGS- schema");
			System.out.println(ex.getMessage());
			System.exit(0);
		}
		try {
			final ResultSet lastMigration =
					stmt.executeQuery("SELECT dsls, version FROM \"-NGS-\".database_migration ORDER BY ordinal DESC LIMIT 1");
			final String lastDsl;
			final String version;
			if (lastMigration.next()) {
				lastDsl = lastMigration.getString(1);
				version = lastMigration.getString(2);
			} else {
				lastDsl = version = "";
			}
			lastMigration.close();
			stmt.close();
			conn.close();
			if (lastDsl.length() > 0) {
				final Map<String, String> dslMap = convertToMap(lastDsl);
				return cache = new AbstractMap.SimpleEntry<Map<String, String>, String>(dslMap, version);
			}
		} catch (SQLException ex) {
			System.out.println("Error loading previous DSL from migration table in -NGS- schema");
			System.out.println(ex.getMessage());
			System.exit(0);
		}
		return cache = emptyResult;
	}

	public static void execute(final Map<InputParameter, String> parameters, final String sql) {
		final String value = parameters.get(InputParameter.CONNECTION_STRING);
		final String connectionString = "jdbc:postgresql://" + value;
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = DriverManager.getConnection(connectionString);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error opening connection to " + connectionString);
			System.out.println(e.getMessage());
			System.exit(0);
		}
		try {
			stmt.execute(sql);
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			System.out.println("Error executing sql script");
			System.out.println(ex.getMessage());
			System.exit(0);
		}
	}

	private static Map<String, String> getArguments(final Map<InputParameter, String> parameters) {
		final String cs = parameters.get(InputParameter.CONNECTION_STRING);
		final String[] args = cs.substring(cs.indexOf("?") + 1).split("&");
		final Map<String, String> map = new LinkedHashMap<String, String>();
		for (final String a : args) {
			final String[] vals = a.split("=");
			if (vals.length != 2) {
				return null;
			}
			map.put(vals[0], vals[1]);
		}
		return map;
	}

	private static boolean testConnection(final Map<InputParameter, String> parameters) {
		final String connectionString = parameters.get(InputParameter.CONNECTION_STRING);
		try {
			final Connection conn = DriverManager.getConnection("jdbc:postgresql://" + connectionString);
			final Statement stmt = conn.createStatement();
			stmt.execute(";");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error connecting to the database.");
			System.out.println(e.getMessage());
			final Map<String, String> args = getArguments(parameters);
			if (!Prompt.canUsePrompt()) {
				//TODO: Postgres error messages are localized. Investigate error code
				if ("The server requested password-based authentication, but no password was provided.".equals(e.getMessage())) {
					System.out.println();
					System.out.println("Since console is not available, password must be sent as argument.");
					System.out.println("Example connection string: my.server.com:5432/MyDatabase?user=user&password=password");
				}
				else if (e.getMessage() != null && e.getMessage().startsWith("FATAL: password authentication failed for user")) {
					System.out.println();
					System.out.println("Please provide correct password to access Postgres database.");
				}
				return false;
			}
			if (args == null) {
				System.out.println();
				System.out.println("Invalid connection string provided: " + connectionString);
				System.out.println("Example connection string: 127.0.0.1:5432/RevenjDb?user=postgres&password=secret");
				return false;
			}
			if (args.get("password") != null) {
				System.out.print("Retry database connection with different credentials (y/N):");
				final String answer = System.console().readLine();
				if (!"y".equalsIgnoreCase(answer)) {
					return false;
				}
			} else {
				final String user = args.get("user");
				if (user != null) {
					System.out.print("Database username (" + user + "): ");
				} else {
					System.out.print("Database username: ");
				}
				final String value = System.console().readLine();
				if (value.length() > 0) {
					args.put("user", value);
				} else if (user == null) {
					System.out.println("Username not provided");
					return false;
				}
			}
			System.out.print("Database password: ");
			final char[] pass = System.console().readPassword();
			args.put("password", new String(pass));
			final StringBuilder newCs = new StringBuilder(connectionString.substring(0, connectionString.indexOf("?") +1));
			for(final Map.Entry<String, String> kv : args.entrySet()) {
				newCs.append(kv.getKey() + "=" + kv.getValue());
				newCs.append("&");
			}
			parameters.put(InputParameter.CONNECTION_STRING, newCs.toString());
			return testConnection(parameters);
		}
		return true;
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (!parameters.containsKey(InputParameter.CONNECTION_STRING)) {
			return true;
		}
		final String value = parameters.get(InputParameter.CONNECTION_STRING);
		if (value == null || !value.contains("/") || !value.contains("?")) {
			System.out.println("Invalid connection string defined. An example: localhost:5433/DbRevenj?user=postgres&password=password");
			System.exit(0);
		}
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException ex) {
			System.out.println("Error loading Postgres driver.");
			System.exit(0);
		}
		return testConnection(parameters);
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Connection string to Postgres database. To create a SQL migration a database with previous dsl must be provided";
	}

	@Override
	public String getDetailedDescription() {
		return "Previous version of DSL is required for various actions, such as diff and SQL migration.\n" +
				"Connection string can be passed from the properties file or as command argument.\n" +
				"If password is not defined in the connection string and console is available, it will prompt for database credentials.";
	}
}
