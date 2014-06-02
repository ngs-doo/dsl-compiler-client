package com.dslplatform.compiler.client.api.core.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.model.Migration;

public class UnmanagedDSLImpl implements UnmanagedDSL {
    @Override
    public boolean doesUnmanagedDSLExits(final DataSource dataSource) throws SQLException {
        return execute(dataSource, MigrationHistoryExist);
    }

    @Override
    public List<Migration> getAllUnmanagedDSL(final DataSource dataSource) throws SQLException {
        return execute(dataSource, AllMigrations);
    }

    @Override
    public Migration getLastUnmanagedDSL(final DataSource dataSource) throws SQLException {
        return execute(dataSource, LastMigration);
    }

    @Override
    public void inspectUnmanagedProjectChanges(
            final DataSource dataSource,
            final String version,
            final Map<String, String> dsl) {
        throw new RuntimeException("Implementation missing!");
    }

    @Override
    public void createUnmanagedProject(
            final String token,
            final DataSource dataSource,
            final String serverName,
            final String applicationName) {
        throw new RuntimeException("Implementation missing!");
    }

    @Override
    public void upgradeUnmanagedDatabase(
            final DataSource dataSource,
            final List<String> migration) throws SQLException {
        execute(dataSource, new Migrate(migration));
    }

    protected <T> T execute(final DataSource dataSource, final Queries<T> query)
            throws SQLException {
        final Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);
            final T result = query.execute(connection);
            connection.commit();
            return result;
        } catch (final SQLException t) {
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

    private static Migration readMigration(final ResultSet resultSet)
            throws SQLException {
        String version = null;
        try {
            version = resultSet.getString("version");
        } catch (SQLException e) {
        }
        return new Migration(
                resultSet.getInt("ordinal"),
                version,
                parseHStore(resultSet.getString("dsls")));
    }

    private static class Migrate extends Queries<Void> {
        private final List<String> migrations;

        public Migrate(
                final List<String> migrations) {
            this.migrations = migrations;
        }

        @Override
        protected Void performQueries(final Statement statement)
                throws SQLException {
            for (final String migration : migrations) {
                statement.execute(migration);
            }
            return (Void) null;
        }
    }

    private static final Queries<Boolean> MigrationHistoryExist =
            new Queries<Boolean>() {
                @Override
                protected Boolean performQueries(final Statement statement)
                        throws SQLException {
                    final ResultSet existQuery = statement
                            .executeQuery(
                                    "SELECT EXISTS(SELECT 1 FROM pg_tables WHERE schemaname = '-NGS-' AND tablename = 'database_migration');");
                    try {
                        existQuery.next();
                        return existQuery.getBoolean(1);
                    } finally {
                        existQuery.close();
                    }
                }
            };

    private static final Queries<Migration> LastMigration =
            new Queries<Migration>() {
                @Override
                protected Migration performQueries(final Statement statement)
                        throws SQLException {
                    final ResultSet migrationQuery = statement
                            .executeQuery(
                                    "SELECT * FROM \"-NGS-\".database_migration ORDER BY ordinal DESC LIMIT 1;");
                    try {
                        migrationQuery.next();
                        return readMigration(migrationQuery);
                    } finally {
                        migrationQuery.close();
                    }
                }
            };

    private static final Queries<List<Migration>> AllMigrations =
            new Queries<List<Migration>>() {
                @Override
                protected List<Migration> performQueries(final Statement statement)
                        throws SQLException {
                    final ResultSet migrationsQuery = statement
                            .executeQuery(
                                    "SELECT * FROM \"-NGS-\".database_migration ORDER BY ordinal;");
                    try {
                        final ArrayList<Migration> migrations =
                                new ArrayList<Migration>();
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
