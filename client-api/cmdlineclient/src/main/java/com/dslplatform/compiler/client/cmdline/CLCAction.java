package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.response.GenerateMigrationSQLResponse;

public interface CLCAction {

    /**
     * Informs if current DSL is syntactically correct.
     */
    public boolean parseDSL();

    /**
     * Outputs last dsl to the standard output.
     */
    public void lastDSL();

    /**
     * Informs a user of the changes made to the DSL.
     */
    public void getChanges();

    /**
     * Upgrades the managed project with a given dsl.
     *
     * @return true if successful
     */
    public boolean upgrade();

    /**
     * Generates client source for connecting to the managed revenj instance
     */
    public boolean generateSources();

    /**
     * Requests for Unmanaged source.
     *
     * @return is operation successful
     */
    public boolean unmanagedSource();

    /**
     * Compiles C# sources provided at {@value }
     *
     * @return is operation successful
     */
    public boolean compileCSServer();

    /**
     * Requests a migration based on the last migration in the provided database at the moment, or null if database is new, and the dsl provided in the parameters.
     * Will output migration to a file.
     *
     * @return migration or null if failed.
     */
    public GenerateMigrationSQLResponse sqlMigration();

    /**
     * Applies a migration sql to the database
     * migrationSQL is read from the disk if existing, otherwise user is prompted to request it.
     */
    public boolean upgradeUnmanagedDatabase();

    /**
     * Aggregation of all tasks will perform following:
     * Parse and diff DSL - display information to user
     * <p>
     * Get Migration SQL - prompt user should it continue in case migration is destructive
     * Apply migration SQL
     * <p>
     * Get CS Sources and compile them.
     * <p>
     * Deploy to mono service.
     */
    public boolean deployUnmanagedServer();
}
