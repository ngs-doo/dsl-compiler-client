package com.dslplatform.compiler.client.api.core.impl;

import com.dslplatform.compiler.client.api.core.Migration;
import com.dslplatform.compiler.client.api.core.UnmanagedDSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnmanagedDSLImpl implements UnmanagedDSL {
    @Override
    public boolean doesUnmanagedDSLExits(final DataSource dataSource) {
        try {
            return execute(dataSource, MigrationHistoryExist);
        } catch (SQLException e) {
            throw new RuntimeException("Asserting does last migration exist failed.");
        }
    }

    @Override
    public final List<Migration> getAllUnmanagedDSL(final DataSource dataSource) {
        try {
            return execute(dataSource, AllMigrations);
        } catch (SQLException e) {
            throw new RuntimeException("Getting all migration failed.");
        }
    }

    @Override
    public Migration getLastUnmanagedDSL(final DataSource dataSource) {
        try {
            return execute(dataSource, LastMigration);
        } catch (SQLException e) {
            throw new RuntimeException("Getting last migration failed.");
        }
    }

    @Override
    public void inspectUnmanagedProjectChanges(final DataSource dataSource, final String version, final Map<String, String> dsl) {

    }

    @Override
    public void createUnmanagedProject(String token, DataSource dataSource, String serverName, String applicationName) {

    }

    @Override
    public void upgradeUnmanagedDatabase(final DataSource dataSource, final String version, final List<String> migration) {

    }

    protected <T> T execute(final DataSource dataSource, final Queries<T> query) throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            final T result = query.execute(connection);
            connection.commit();
            return result;
        } catch (final Throwable t) {
            connection.rollback();
            throw t;
        } finally {
            connection.close();
        }
    }

    protected static abstract class Queries<T> {
        T execute(final Connection connection) throws SQLException {
            final Statement statement = connection.createStatement();
            try {
                return performQueries(statement);
            } catch (final SQLException e) {
                throw e;
            } finally {
                statement.close();
            }
        }

        protected abstract T performQueries(final Statement statement)
                throws SQLException;
    }

    private static Migration readMigration(final ResultSet resultSet) throws SQLException {
        return new Migration(
                resultSet.getInt("ordinal"),
                resultSet.getString("version"),
                parseHStore(resultSet.getString("dsls")));
    }

    private static final Queries<Boolean> MigrationHistoryExist = new Queries<Boolean>() {
        @Override
        protected Boolean performQueries(final Statement statement)
                throws SQLException {
            final ResultSet existQuery = statement
                    .executeQuery("SELECT EXISTS(SELECT 1 FROM pg_tables WHERE schemaname = '-NGS-' AND tablename = 'database_migration');");
            try {
                existQuery.next();
                return existQuery.getBoolean(1);
            } finally {
                existQuery.close();
            }
        }
    };

    private static final Queries<Migration> LastMigration = new Queries<Migration>() {
        @Override
        protected Migration performQueries(final Statement statement)
                throws SQLException {
            final ResultSet migrationQuery = statement
                    .executeQuery("SELECT ordinal, version, dsls FROM \"-NGS-\".database_migration ORDER BY ordinal DESC LIMIT 1;");
            try {
                migrationQuery.next();
                return readMigration(migrationQuery);
            } finally {
                migrationQuery.close();
            }
        }
    };

    private static final Queries<List<Migration>> AllMigrations = new Queries<List<Migration>>() {
        @Override
        protected List<Migration> performQueries(final Statement statement)
                throws SQLException {
            final ResultSet migrationsQuery = statement
                    .executeQuery("SELECT ordinal, version, dsls FROM \"-NGS-\".database_migration ORDER BY ordinal;");
            try {
                final ArrayList<Migration> migrations = new ArrayList<Migration>();
                while (migrationsQuery.next()) {
                    migrations.add(readMigration(migrationsQuery));
                }
                return migrations;
            } finally {
                migrationsQuery.close();
            }
        }
    };

    private static String unescape(final String element) {
        return element.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static Map<String, String> parseHStore(final String m) {
        if (m == null) {
            throw new IllegalArgumentException("Could not parse null string");
        }

        final Map<String, String> tuples = new LinkedHashMap<String, String>();
        if (m.isEmpty()) return tuples;

        final int endLength = m.length() - 1;
        if (m.charAt(0) != '"' || m.charAt(endLength) != '"') {
            throw new IllegalArgumentException(
                    "Could not parse due to missing quotes");
        }

        final String[] pairs = m.substring(1, endLength).split("\", ?\"", -1);
        for (final String pair : pairs) {
            final String[] kv = pair.split("\"=>\"", -1);

            if (kv.length != 2) {
                throw new IllegalArgumentException(
                        "Error parsing key-value pair: " + pair);
            }

            tuples.put(unescape(kv[0]), unescape(kv[1]));
        }
        return tuples;
    }
}
