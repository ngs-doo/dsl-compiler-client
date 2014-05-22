package com.dslplatform.compiler.client.response;

public class UpgradeUnmanagedServerAndDatabaseResponse extends DatabaseConnectionResponse {
    final boolean upgradeSuccessfull;
    final boolean csdeploySuccessfull;

    public UpgradeUnmanagedServerAndDatabaseResponse(
            boolean databaseConnectionSuccessful,
            String databaseConnectionErrorMessage) {
        this(databaseConnectionSuccessful, databaseConnectionErrorMessage, false, false);
    }

    public UpgradeUnmanagedServerAndDatabaseResponse(
            boolean databaseConnectionSuccessful,
            String databaseConnectionErrorMessage,
            boolean upgradeSuccessfull,
            boolean csdeploySuccessfull) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.upgradeSuccessfull = upgradeSuccessfull;
        this.csdeploySuccessfull = csdeploySuccessfull;
    }
}
