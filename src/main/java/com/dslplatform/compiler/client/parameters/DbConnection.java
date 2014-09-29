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
		return getDatabaseDslAndVersion(context).dsl;
	}

	private static String extractPostgresVersion(final String version, final Context context) {
		final String[] parts = version.split(",");
		if (parts.length < 2) {
			context.log("Suspicious Postgres version found. Expecting: PostgreSQL version, compiled by");
		}
		final String[] info = parts[0].split(" ");
		if (info.length != 2) {
			context.error("Unable to detect postgres version. Found version info: " + version);
			return "";
		}
		return info[1];
	}

	public static class DatabaseInfo {
		public final String compilerVersion;
		public final String postgresVersion;
		public final Map<String, String> dsl;
		public DatabaseInfo(final String compiler, final String postgres, final Map<String, String> dsl) {
			this.compilerVersion = compiler;
			this.postgresVersion = postgres;
			this.dsl = dsl;
		}
	}

	public static DatabaseInfo getDatabaseDslAndVersion(final Context context) throws ExitException {
		final DatabaseInfo cache = context.load(CACHE_NAME);
		if (cache != null) {
			return cache;
		}
		final String value = context.get(InputParameter.CONNECTION_STRING);
		final String connectionString = "jdbc:postgresql://" + value;
		Connection conn;
		Statement stmt;
		final String postgres;
		try {
			conn = DriverManager.getConnection(connectionString);
			stmt = conn.createStatement();
			final ResultSet pgVersion = stmt.executeQuery("SELECT version()");
			if (pgVersion.next()) {
				postgres = extractPostgresVersion(pgVersion.getString(1), context);
			} else {
				postgres = "";
			}
			pgVersion.close();
		} catch (SQLException e) {
			context.error("Error opening connection to " + connectionString);
			context.error(e);
			throw new ExitException();
		}
		final DatabaseInfo emptyResult = new DatabaseInfo("", postgres, new HashMap<String, String>());
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
			cleanup(conn, context);
			throw new ExitException();
		}
		try {
			final ResultSet lastMigration =
					stmt.executeQuery("SELECT dsls, version FROM \"-NGS-\".database_migration ORDER BY ordinal DESC LIMIT 1");
			final String lastDsl;
			final String compiler;
			if (lastMigration.next()) {
				lastDsl = lastMigration.getString(1);
				compiler = lastMigration.getString(2);
			} else {
				lastDsl = compiler = "";
			}
			lastMigration.close();
			stmt.close();
			conn.close();
			if (lastDsl.length() > 0) {
				final Map<String, String> dslMap = convertToMap(lastDsl, context);
				final DatabaseInfo result = new DatabaseInfo(compiler, postgres, dslMap);
				context.cache(CACHE_NAME, result);
				return result;
			}
		} catch (SQLException ex) {
			context.error("Error loading previous DSL from migration table in -NGS- schema");
			context.error(ex);
			cleanup(conn, context);
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
			cleanup(conn, context);
			throw new ExitException();
		}
	}

	private static void cleanup(final Connection conn, final Context context) {
		try {
			conn.close();
		} catch (SQLException ex2) {
			context.error("Error cleaning up connection.");
			context.error(ex2);
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

	private static boolean testConnection(final Context context) throws ExitException {
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
			final boolean dbDoesntExists = "3D000".equals(e.getSQLState());
			final boolean dbMissingPassword = "08004".equals(e.getSQLState());
			final boolean dbWrongPassword = "28P01".equals(e.getSQLState());
			final Map<String, String> args = parse(connectionString);
			if (args == null) {
				context.show();
				context.error("Invalid connection string provided: " + connectionString);
				context.show("Example connection string: 127.0.0.1:5432/RevenjDb?user=postgres&password=secret");
				return false;
			}
			if (dbDoesntExists && context.contains(InputParameter.FORCE_MIGRATION) && context.contains(InputParameter.APPLY_MIGRATION)
					&& args.containsKey("user") && args.containsKey("password")) {
				final int sl = connectionString.indexOf("/");
				final String dbName = connectionString.substring(sl + 1, connectionString.indexOf("?"));
				if (!context.canInteract()) {
					context.show("Trying to create new database " + dbName + " due to force option");
				} else {
					final String answer = context.ask("Create a new database " + dbName + " (y/N):");
					if (!"y".equalsIgnoreCase(answer)) {
						throw new ExitException();
					}
				}
				try {
					final StringBuilder newCs = new StringBuilder(connectionString.substring(0, sl + 1));
					newCs.append("postgres?");
					for (final Map.Entry<String, String> kv : args.entrySet()) {
						newCs.append(kv.getKey()).append("=").append(kv.getValue());
						newCs.append("&");
					}
					final Connection conn = DriverManager.getConnection("jdbc:postgresql://" + newCs.toString());
					final Statement stmt = conn.createStatement();
					stmt.execute("CREATE DATABASE \"" + dbName + "\"");
					stmt.close();
					conn.close();
				} catch (SQLException ex) {
					context.error("Error creating new database: " + dbName);
					context.error(ex);
					return false;
				}
				return true;
			} else if (!context.canInteract()) {
				if (dbMissingPassword) {
					context.show();
					context.error("Password not sent. Since interaction is not available, password must be sent as argument.");
					context.show("Example connection string: my.server.com:5432/MyDatabase?user=user&password=password");
				} else if (dbDoesntExists) {
					context.show();
					context.error("Database not found. Since interaction is not available and both force and apply option are not enabled, existing database must be used.");
				} else if (dbWrongPassword) {
					context.show();
					context.error("Please provide correct password to access Postgres database.");
				}
				throw new ExitException();
			} else if (dbDoesntExists) {
				context.show();
				context.error("Database not found. Since force option is not enabled, existing database must be used.");
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
					throw new ExitException();
				}
			}
			final char[] pass = context.askSecret("Database password: ");
			args.put("password", new String(pass));
			final StringBuilder newCs = new StringBuilder(connectionString.substring(0, connectionString.indexOf("?") + 1));
			for (final Map.Entry<String, String> kv : args.entrySet()) {
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
