package com.dslplatform.compiler.client.api.core.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.model.Migration;

public class UnmanagedDSLMock {

    public static UnmanagedDSL mock_single_integrated = new UnmanagedDSL() {
        @Override
        public boolean doesUnmanagedDSLExits(final DataSource dataSource) {
            return true;
        }

        @Override
        public List<Migration> getAllUnmanagedDSL(final DataSource dataSource) {
            return Arrays.asList(MockData.migration_test_migration_single);
        }

        @Override
        public Migration getLastUnmanagedDSL(final DataSource dataSource) {
            // throw new RuntimeException("Getting last migration failed.");
            return MockData.migration_test_migration_single;
        }

        @Override
        public void inspectUnmanagedProjectChanges(DataSource dataSource, String version, Map<String, String> dsl) {

        }

        @Override
        public void createUnmanagedProject(
                String token,
                DataSource dataSource,
                String serverName,
                String applicationName) {

        }

        @Override
        public void upgradeUnmanagedDatabase(DataSource dataSource, List<String> migration) {

        }
    };

    public static UnmanagedDSL mock_complex = new UnmanagedDSL() {
        @Override
        public boolean doesUnmanagedDSLExits(final DataSource dataSource) {
            //throw new RuntimeException("Asserting does last migration exist failed.");
            return true;
        }

        @Override
        public List<Migration> getAllUnmanagedDSL(final DataSource dataSource) {
            //throw new RuntimeException("Getting all migration failed.");

            return Arrays.asList(
                    MockData.migration_1,
                    MockData.migration_2,
                    MockData.migration_3);
        }

        @Override
        public Migration getLastUnmanagedDSL(final DataSource dataSource) {
            // throw new RuntimeException("Getting last migration failed.");
            return MockData.migration_3;
        }

        @Override
        public void inspectUnmanagedProjectChanges(DataSource dataSource, String version, Map<String, String> dsl) {

        }

        @Override
        public void createUnmanagedProject(
                String token,
                DataSource dataSource,
                String serverName,
                String applicationName) {

        }

        @Override
        public void upgradeUnmanagedDatabase(DataSource dataSource, List<String> migration) {

        }
    };
}
