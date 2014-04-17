package com.dslplatform.compiler.client.response;

public class UpgradeUnmanagedDatabaseResponse extends DatabaseConnectionResponse {
    public final boolean successfulUpgrade;

    public UpgradeUnmanagedDatabaseResponse(
            boolean databaseConnectionSuccessful,
            String databaseConnectionErrorMessage, boolean successfulUpgrade) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.successfulUpgrade = successfulUpgrade;
    }
    public static UpgradeUnmanagedDatabaseResponse error(final Exception e) {
        return new UpgradeUnmanagedDatabaseResponse(false, e.getMessage(), false);
    }

    public static UpgradeUnmanagedDatabaseResponse success() {
        return new UpgradeUnmanagedDatabaseResponse(true, null, true);
    }
}
