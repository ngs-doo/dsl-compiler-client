package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import org.postgresql.*;

import java.sql.*;
import java.sql.Driver;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
		try {
			final Connection conn = DriverManager.getConnection("jdbc:postgresql://" + value);
			final Statement stmt = conn.createStatement();
			stmt.execute(";");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error connecting to the database.");
			System.out.println(e.getMessage());
			System.exit(0);
		}
		return true;
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
		return null;
	}
}
