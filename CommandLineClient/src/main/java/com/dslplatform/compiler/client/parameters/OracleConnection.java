package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum OracleConnection implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "oracle";
	}

	@Override
	public String getUsage() {
		return "connection_string";
	}

	private static final String CACHE_NAME = "oracle_dsl_cache";
	private static final String ORACLE_CUSTOM_DRIVER = "oracle_jdbc_driver";

	public static Map<String, String> getDatabaseDsl(final Context context) throws ExitException {
		return getDatabaseDslAndVersion(context).dsl;
	}

	static String extractOracleVersion(final String version, final Context context) {
		final Matcher matcher = Pattern.compile("^\\w+\\s+(\\d+\\.\\d+)").matcher(version);
		if (!matcher.find()) {
			context.warning("Unable to detect Oracle version. Found version info: " + version);
			return "";
		}
		return matcher.group(1);
	}

	private static Connection getConnection(final Context context, final String url) throws SQLException {
		final Driver driver = context.load(ORACLE_CUSTOM_DRIVER);
		return driver == null
				? DriverManager.getConnection(url)
				: driver.connect(url, null);
	}

	public static DatabaseInfo getDatabaseDslAndVersion(final Context context) throws ExitException {
		final DatabaseInfo cache = context.load(CACHE_NAME);
		if (cache != null) {
			return cache;
		}
		final String value = context.get(INSTANCE);
		final String connectionString = "jdbc:oracle:thin:" + value;
		Connection conn;
		Statement stmt;
		final String oracle;
		try {
			conn = getConnection(context, connectionString);
			stmt = conn.createStatement();
			final ResultSet dbVersion = stmt.executeQuery("SELECT * FROM V$VERSION where banner LIKE 'CORE%'");
			if (dbVersion.next()) {
				oracle = extractOracleVersion(dbVersion.getString(1), context);
			} else {
				oracle = "";
			}
			dbVersion.close();
		} catch (SQLException e) {
			context.error("Error opening connection to " + connectionString);
			context.error(e);
			throw new ExitException();
		}
		final DatabaseInfo emptyResult = new DatabaseInfo("Oracle", "", oracle, new HashMap<String, String>());
		try {
			final ResultSet migrationExist =
					stmt.executeQuery(
							"SELECT COUNT(*) FROM sys.all_tables t\n" +
									"WHERE t.OWNER = '-NGS-' AND t.TABLE_NAME = 'DATABASE_MIGRATION'");
			final boolean hasTable = migrationExist.next() && migrationExist.getLong(1) > 0;
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
					stmt.executeQuery("SELECT sq.Dsls, sq.Version\n" +
							"FROM (SELECT m.Dsls, m.Version FROM \"-NGS-\".Database_Migration m ORDER BY m.Ordinal DESC) sq\n" +
							"WHERE RowNum = 1");
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
				final DatabaseInfo result = new DatabaseInfo("Oracle", compiler, oracle, dslMap);
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
		final String value = context.get(INSTANCE);
		final String connectionString = "jdbc:oracle:thin:" + value;
		Connection conn;
		Statement stmt;
		try {
			conn = getConnection(context, connectionString);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			context.error("Error opening connection to " + connectionString);
			context.error(e);
			throw new ExitException();
		}
		final int migrationEnds = sql.indexOf("MIGRATION_DESCRIPTION*/");
		final String rawSql = migrationEnds == -1 ? sql : sql.substring(migrationEnds + "MIGRATION_DESCRIPTION*/".length());
		final String[] parts = rawSql.contains("\r\n") ? rawSql.split("\r\n/\r\n") : rawSql.split("\n/\n");
		try {
			for (final String part : parts) {
				final String trimmed = part.trim();
				if (trimmed.length() > 0) {
					context.log(trimmed);
					stmt.execute(trimmed);
				}
			}
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			context.error("Error executing SQL script");
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

	static class Credentials {
		public final String user;
		public final String password;

		public Credentials(String user, String password) {
			this.user = user;
			this.password = password;
		}
	}

	private static Credentials parse(final String connectionString) {
		final String[] args = connectionString.substring(0, connectionString.indexOf('@')).split("/");
		if (args.length == 2) {
			return new Credentials(args[0], args[1]);
		}
		return null;
	}

	private static boolean testConnection(final Context context) throws ExitException {
		final String connectionString = context.get(INSTANCE);
		try {
			final Connection conn = getConnection(context, "jdbc:oracle:thin:" + connectionString);
			final Statement stmt = conn.createStatement();
			stmt.execute("SELECT 1 FROM dual");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			context.warning("Error connecting to the database.");
			context.warning(e);
			if (e.getErrorCode() == 12514) {
				context.error("Oracle database not found. Please create database before using clc or check if correct connection string was provided");
				return false;
			}
			if (e.getErrorCode() != 1017) {
				return false;
			}
			if (!context.canInteract()) {
				context.show();
				context.error("Please provide correct password to access Oracle database.");
				throw new ExitException();
			}
			final Credentials args = parse(connectionString);
			String user = args != null ? args.user : "";
			String password = args != null ? args.password : "";
			if (password.length() != 0) {
				final String answer = context.ask("Retry database connection with different credentials (y/N):");
				if (!"y".equalsIgnoreCase(answer)) {
					return false;
				}
			} else {
				final String question;
				if (user.length() != 0) {
					question = "Oracle username (" + user + "): ";
				} else {
					question = "Oracle username: ";
				}
				final String value = context.ask(question);
				if (value.length() > 0) {
					user = value;
				} else if (user.length() == 0) {
					context.error("Username not provided");
					throw new ExitException();
				}
			}
			final char[] pass = context.askSecret("Oracle password: ");
			password = new String(pass);
			final int questionIndex = connectionString.indexOf('@');
			final String newCs = user + "/" + password + connectionString.substring(questionIndex);
			context.put(INSTANCE, newCs);
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
		if (value == null || !value.contains("@") || !value.contains(":")) {
			context.error("Invalid connection string defined. An example: @localhost:1521/DbRevenj");
			throw new ExitException();
		}
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException ex) {
			context.warning("Error loading Oracle driver (oracle.jdbc.OracleDriver). Will look into alternative locations ...");
			final File loc = new File(".");
			final File[] foundFiles = loc.listFiles(new FileFilter() {
				public boolean accept(File file) {
					final String name = file.getName().toLowerCase();
					return name.startsWith("ojdbc") && name.endsWith(".jar");
				}
			});
			final List<File> jars = foundFiles == null ? new ArrayList<File>(0) : new ArrayList<File>(Arrays.asList(foundFiles));
			if (jars.size() == 0) {
				final String envOH = System.getenv("ORACLE_HOME");
				if (envOH != null) {
					context.log("Found ORACLE_HOME environment variable: " + envOH);
					final File jdbcRootFile = new File(new File(envOH), "ojdbc6.jar");
					final File jdbcLibFile = new File(new File(new File(new File(envOH), "jdbc"), "lib"), "ojdbc6.jar");
					if (jdbcRootFile.exists()) {
						jars.add(jdbcRootFile.getAbsoluteFile());
					} else if (jdbcLibFile.exists()) {
						jars.add(jdbcLibFile.getAbsoluteFile());
					} else {
						context.warning("Found ORACLE_HOME environment variable, but jar driver is missing from: "
								+ jdbcRootFile.getAbsolutePath() + " and " + jdbcLibFile.getAbsolutePath());
					}
				} else {
					context.warning("ORACLE_HOME environment variable not set");
				}
			}
			if (jars.size() == 0) {
				context.error("Try downloading ojdbc6.jar from Oracle: http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html " +
						"and place it in: " + new File(".").getAbsolutePath() + "\n" +
						"Alternatively, try adding thin ojdbc to the classpath or set correct ORACLE_HOME environment variable.");
				throw new ExitException();
			}
			final URL[] urls = new URL[jars.size()];
			for (int i = 0; i < jars.size(); i++) {
				try {
					urls[i] = jars.get(i).toURI().toURL();
				} catch (MalformedURLException mex) {
					context.error(mex);
				}
			}
			final URLClassLoader ucl = new URLClassLoader(urls);
			final ServiceLoader<java.sql.Driver> drivers = ServiceLoader.load(java.sql.Driver.class, ucl);
			for (final java.sql.Driver d : drivers) {
				context.log("Found: " + d);
				if ("oracle.jdbc.OracleDriver".equals(d.getClass().getName())) {
					if (urls.length == 1) {
						context.show("Found Oracle driver in: " + urls[0].getPath());
					} else {
						context.show("Found Oracle driver");
					}
					try {
						java.sql.Driver driver = d.getClass().newInstance();
						context.cache(ORACLE_CUSTOM_DRIVER, driver);
					} catch (Exception de) {
						context.error(de);
					}
				}
			}
			try {
				Class.forName("oracle.jdbc.OracleDriver");
			} catch (ClassNotFoundException oex) {
				if (context.load(ORACLE_CUSTOM_DRIVER) == null) {
					context.error("Error trying to load Oracle driver using fallback method. Add thin ojdbc to the classpath.");
					throw new ExitException();
				}
			}
		}
		return testConnection(context);
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Connection string to Oracle database. To create an SQL migration a database with previous DSL must be provided";
	}

	@Override
	public String getDetailedDescription() {
		return "Previous version of DSL is required for various actions, such as diff and SQL migration.\n" +
				"Connection string can be passed from the properties file or as command argument.\n" +
				"If password is not defined in the connection string and console is available, it will prompt for database credentials.\n" +
				"\n" +
				"Example connection strings:\n" +
				"\n" +
				"\t@localhost:1521/ServiceName\n" +
				"\tuser/pass@server:1521/DB\n" +
				"\n" +
				"More info about connection strings can be found on Oracle JDBC site: http://docs.oracle.com/cd/E11882_01/appdev.112/e13995/oracle/jdbc/OracleDriver.html\n" +
				"Connection string is defined without the jdbc:oracle:thin: part";
	}
}
