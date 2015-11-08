package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.sql.*;
import java.util.*;
import java.util.regex.*;

public enum PostgresConnection implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "postgres"; }
	@Override
	public String getUsage() { return "connection_string"; }

	private static final String CACHE_NAME = "postgres_dsl_cache";

	public static Map<String, String> getDatabaseDsl(final Context context) throws ExitException {
		return getDatabaseDslAndVersion(context).dsl;
	}

	static String extractPostgresVersion(final String version, final Context context) {
		final Matcher matcher = Pattern.compile("^\\w+\\s+(\\d+\\.\\d+)").matcher(version);
		if (!matcher.find()) {
			context.error("Unable to detect Postgres version. Found version info: " + version);
			return "";
		}
		return matcher.group(1);
	}

	public static DatabaseInfo getDatabaseDslAndVersion(final Context context) throws ExitException {
		final DatabaseInfo cache = context.load(CACHE_NAME);
		if (cache != null) {
			return cache;
		}
		final String value = context.get(INSTANCE);
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
		final DatabaseInfo emptyResult = new DatabaseInfo("Postgres", "", postgres, new HashMap<String, String>());
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
			if (lastDsl != null && lastDsl.length() > 0) {
				final Map<String, String> dslMap = DatabaseInfo.convertToMap(lastDsl, context);
				final DatabaseInfo result = new DatabaseInfo("Postgres", compiler, postgres, dslMap);
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
		final String connectionString = "jdbc:postgresql://" + context.get(INSTANCE);

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
			final long startAt = System.currentTimeMillis();
			stmt.execute(sql);
			final long endAt = System.currentTimeMillis();
			context.log("Script executed in " + (endAt - startAt) + "ms");
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
		final int questionIndex = connectionString.indexOf('?');
		if (questionIndex == -1) {
			return new HashMap<String, String>(0);
		}
		final String[] args = connectionString.substring(questionIndex + 1).split("&");
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
		final String connectionString = context.get(INSTANCE);
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
			if (dbDoesntExists && context.contains(Force.INSTANCE) && context.contains(ApplyMigration.INSTANCE)
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
					question = "Postgres username (" + user + "): ";
				} else {
					question = "Postgres username: ";
				}
				final String value = context.ask(question);
				if (value.length() > 0) {
					args.put("user", value);
				} else if (user == null) {
					context.error("Username not provided");
					throw new ExitException();
				}
			}
			final char[] pass = context.askSecret("Postgres password: ");
			args.put("password", new String(pass));
			final int questionIndex = connectionString.indexOf('?');
			final String newCs = questionIndex == -1
					? connectionString + "?"
					: connectionString.substring(0, questionIndex + 1);
			final StringBuilder csBuilder = new StringBuilder(newCs);
			for (final Map.Entry<String, String> kv : args.entrySet()) {
				csBuilder.append(kv.getKey()).append("=").append(kv.getValue());
				csBuilder.append("&");
			}
			context.put(INSTANCE, csBuilder.toString());
			return testConnection(context);
		}
		return true;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (!context.contains(INSTANCE)) {
			return true;
		}
		final String value = context.get(INSTANCE);
		if (value == null || !value.contains("/")) {
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
				"\tlocalhost/Database?user=postgres\n" +
				"\tserver:5432/DB?user=migration&password=secret&ssl=true\n" +
				"\n" +
				"More info about connection strings can be found on PostgreSQL JDBC site: http://jdbc.postgresql.org/documentation/93/connect.html\n" +
				"Connection string is defined without the jdbc:postgresql:// part";
	}
}
