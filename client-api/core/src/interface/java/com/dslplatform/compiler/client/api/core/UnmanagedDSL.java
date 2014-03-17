package com.dslplatform.compiler.client.api.core;

import javax.sql.DataSource;
import java.util.Map;
import java.util.List;

public interface UnmanagedDSL {
    public boolean doesUnmanagedDSLExits(final DataSource dataSource);

    /**
     * Retrieve all DSLs for an unmanaged project
     */
    public List<Migration> getAllUnmanagedDSL(final DataSource dataSource);

    /**
     * Retrieve the last DSL for an unmanaged project
     */
    public Migration getLastUnmanagedDSL(final DataSource dataSource);

    /**
     * Compare new DSL with the old one, retrieved from the unamanaged database.
     */
    public void inspectUnmanagedProjectChanges(final DataSource dataSource, final String version, final Map<String, String> dsl);

    /**
     * Creates an unmanaged project
     */
    public void createUnmanagedProject(
            final String token,
            final DataSource dataSource,
            final String serverName,
            final String applicationName);

    /**
     * Compare new DSL with the old one, retrieved from the unamanaged database.
     */
    public void upgradeUnmanagedDatabase(final DataSource dataSource, final String version, final List<String> migration);
}
