package com.dslplatform.compiler.client.response;

public class GenerateMigrationSQLResponse extends AuthorizationResponse {

    public final String migration;

    public final boolean migrationRequestSuccessful;

    public GenerateMigrationSQLResponse(boolean authorized, String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        this.migration = null;
        this.migrationRequestSuccessful = false;
    }

    public GenerateMigrationSQLResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean migrationRequestSuccessful,
            String migration) {
        super(authorized, authorizationErrorMessage);
        this.migration = migration;
        this.migrationRequestSuccessful = migrationRequestSuccessful;
    }
}
