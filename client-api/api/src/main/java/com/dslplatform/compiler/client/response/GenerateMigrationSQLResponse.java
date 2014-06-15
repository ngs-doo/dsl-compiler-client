package com.dslplatform.compiler.client.response;

import static com.dslplatform.compiler.client.diff.MigrationStrip.*;

public class GenerateMigrationSQLResponse extends AuthorizationResponse {

    public final String migration;
    public final String migrationInformation;
    public final boolean isMigrationDestructive;

    public final boolean migrationRequestSuccessful;

    public GenerateMigrationSQLResponse(boolean authorized, String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        this.migration = null;
        this.migrationInformation = null;
        this.migrationRequestSuccessful = false;
        this.isMigrationDestructive = true;
    }

    public GenerateMigrationSQLResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean migrationRequestSuccessful,
            String migration) {
        super(authorized, authorizationErrorMessage);
        this.migration = migration;
        this.migrationInformation = stripInformationComents(migration);
        this.migrationRequestSuccessful = migrationRequestSuccessful;
        this.isMigrationDestructive = findDestructive(migrationInformation);
    }

}
