package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.core.Migration;

public class GetLastUnmanagedDSLResponse extends DatabaseConnectionResponse {

    private final Migration lastMigration;

    public Migration getLastMigration() {
        return lastMigration;
    }

    public GetLastUnmanagedDSLResponse(
            final boolean databaseConnectionSuccessful,
            final String databaseConnectionErrorMessage,
            final Migration lastMigration) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.lastMigration = lastMigration;
    }
}
