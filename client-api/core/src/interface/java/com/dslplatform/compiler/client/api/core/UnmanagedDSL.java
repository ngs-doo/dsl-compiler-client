package com.dslplatform.compiler.client.api.core;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.dslplatform.compiler.client.api.model.Migration;

public interface UnmanagedDSL {
    public boolean doesUnmanagedDSLExits(final DataSource dataSource) throws SQLException;

    /**
     * Retrieve all DSLs for an unmanaged project
     */
    public List<Migration> getAllUnmanagedDSL(final DataSource dataSource) throws SQLException;

    /**
     * Retrieve the last DSL for an unmanaged project
     */
    public Migration getLastUnmanagedDSL(final DataSource dataSource) throws SQLException;

    /**
     * Compare new DSL with the old one, retrieved from the unmanaged database.
     */

    public void upgradeUnmanagedDatabase(final DataSource dataSource, final List<String> migration) throws SQLException;
    /**
     * Compare new DSL with the old one, retrieved from the unmanaged database.
     */
    public void inspectUnmanagedProjectChanges(
            final DataSource dataSource,
            final String version,
            final Map<String, String> dsl);

    /**
     * Creates an unmanaged project
     */
    public void createUnmanagedProject(
            final String token,
            final DataSource dataSource,
            final String serverName,
            final String applicationName);
}
