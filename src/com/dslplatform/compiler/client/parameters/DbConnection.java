package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.InputParameter;

import java.sql.*;
import java.util.*;

public enum DbConnection implements CompileParameter {
	INSTANCE;

	private static String unescape(final String element) {
		return element.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	private static Map<String, String> convertToMap(final String dsls, final Context context) throws ExitException {
		final Map<String, String> tuples = new LinkedHashMap<String, String>();
		if (dsls == null || dsls.length() == 0) {
			return tuples;
		}
		final int endLength = dsls.length() - 1;
		if (dsls.charAt(0) != '"' || dsls.charAt(endLength) != '"') {
			context.error("Invalid DSL found in database. Can't convert to HSTORE: " + dsls);
			throw new ExitException();
		}
		final String[] pairs = dsls.substring(1, endLength).split("\", ?\"", -1);
		for (final String pair : pairs) {
			final String[] kv = pair.split("\"=>\"", -1);
			if (kv.length != 2) {
				context.error("Invalid DSL found in database. Can't convert to HSTORE: " + dsls);
				throw new ExitException();
			}
			tuples.put(unescape(kv[0]), unescape(kv[1]));
		}
		return tuples;
	}

	private static final String CACHE_NAME = "database_dsl_cache";

	public static Map<String, String> getDatabaseDsl(final Context context) throws ExitException {
		return getDatabaseDslAndVersion(context).getKey();
	}

	public static Map.Entry<Map<String, String>, String> getDatabaseDslAndVersion(final Context context) throws ExitException {
		final Map.Entry<Map<String, String>, String> cache = context.load(CACHE_NAME);
		if (cache != null) {
			return cache;
		}
		final String value = context.get(InputParameter.CONNECTION_STRING);
		final Map.Entry<Map<String, String>, String> emptyResult
				= new AbstractMap.SimpleEntry<Map<String, String>, String>(new HashMap<String, String>(), "");
		final String connectionString = "jdbc:postgresql://" + value;
		Connection conn;
		Statement stmt;
		try {
			conn = DriverManager.getConnection(connectionString);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			context.error("Error opening connection to " + connectionString);
			context.error(e);
			throw new ExitException();
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
				context.cache(CACHE_NAME, emptyResult);
				return emptyResult;
			}
		} catch (SQLException ex) {
			context.error("Error checking for migration table in -NGS- schema");
			context.error(ex);
			throw new ExitException();
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
				final Map<String, String> dslMap = convertToMap(lastDsl, context);
				final Map.Entry<Map<String, String>, String> result =
						new AbstractMap.SimpleEntry<Map<String, String>, String>(dslMap, version);
				context.cache(CACHE_NAME, result);
				return result;
			}
		} catch (SQLException ex) {
			context.error("Error loading previous DSL from migration table in -NGS- schema");
			context.error(ex);
			throw new ExitException();
		}
		context.cache(CACHE_NAME, emptyResult);
		return emptyResult;
	}

	public static void execute(final Context context, final String sql) throws ExitException {
		final String value = context.get(InputParameter.CONNECTION_STRING);
		final String connectionString = "jdbc:postgresql://" + value;
		Connection conn;
		Statement stmt;
		try {
			conn = DriverManager.getConnection(connectionString);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			context.error("Error opening connection to " + connectionString);
			context.error(e);
			throw new ExitException();
		}
		try {
			stmt.execute(sql);
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			context.error("Error executing sql script");
			context.error(ex);
			throw new ExitException();
		}
	}

	private static Map<String, String> parse(final String connectionString) {
		final String[] args = connectionString.substring(connectionString.indexOf("?") + 1).split("&");
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

	private static boolean testConnection(final Context context) {
		final String connectionString = context.get(InputParameter.CONNECTION_STRING);
		try {
			final Connection conn = DriverManager.getConnection("jdbc:postgresql://" + connectionString);
			final Statement stmt = conn.createStatement();
			stmt.execute(";");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			context.error("Error connecting to the database.");
			context.error(e);
			final Map<String, String> args = parse(connectionString);
			if (!context.canInteract()) {
				//TODO: Postgres error messages are localized. Investigate error code
				if ("The server requested password-based authentication, but no password was provided.".equals(e.getMessage())) {
					context.show();
					context.show("Since console is not available, password must be sent as argument.");
					context.show("Example connection string: my.server.com:5432/MyDatabase?user=user&password=password");
				}
				else if (e.getMessage() != null && e.getMessage().startsWith("FATAL: password authentication failed for user")) {
					context.show();
					context.show("Please provide correct password to access Postgres database.");
				}
				return false;
			}
			if (args == null) {
				context.show();
				context.show("Invalid connection string provided: " + connectionString);
				context.show("Example connection string: 127.0.0.1:5432/RevenjDb?user=postgres&password=secret");
				return false;
			}
			if (args.get("password") != null) {
				final String answer = context.ask("Retry database connection with different credentials (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					return false;
				}
			} else {
				final String user = args.get("user");
				final String question;
				if (user != null) {
					question = "Database username (" + user + "): ";
				} else {
					question = "Database username: ";
				}
				final String value = context.ask(question);
				if (value.length() > 0) {
					args.put("user", value);
				} else if (user == null) {
					context.error("Username not provided");
					return false;
				}
			}
			final char[] pass = context.askSecret("Database password: ");
			args.put("password", new String(pass));
			final StringBuilder newCs = new StringBuilder(connectionString.substring(0, connectionString.indexOf("?") +1));
			for(final Map.Entry<String, String> kv : args.entrySet()) {
				newCs.append(kv.getKey()).append("=").append(kv.getValue());
				newCs.append("&");
			}
			context.put(InputParameter.CONNECTION_STRING, newCs.toString());
			return testConnection(context);
		}
		return true;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (!context.contains(InputParameter.CONNECTION_STRING)) {
			return true;
		}
		final String value = context.get(InputParameter.CONNECTION_STRING);
		if (value == null || !value.contains("/") || !value.contains("?")) {
			context.error("Invalid connection string defined. An example: localhost:5433/DbRevenj?user=postgres&password=password");
			throw new ExitException();
		}
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException ex) {
			context.error("Error loading Postgres driver.");
			throw new ExitException();
		}
		return testConnection(context);
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Connection string to Postgres database. To create a SQL migration a database with previous dsl must be provided";
	}

	@Override
	public String getDetailedDescription() {
		return "Previous version of DSL is required for various actions, such as diff and SQL migration.\n" +
				"Connection string can be passed from the properties file or as command argument.\n" +
				"If password is not defined in the connection string and console is available, it will prompt for database credentials.\n" +
				"\n" +
				"Example connection strings:\n" +
				"\n" +
				"	localhost/Database?user=postgres\n" +
				"	server:5432/DB?user=migration&password=secret&ssl=true\n" +
				"\n" +
				"More info about connection strings can be found on PostgreSQL JDBC site: http://jdbc.postgresql.org/documentation/80/connect.html\n" +
				"Connection string is defined without the jdbc:postgresql:// part";
	}
}
