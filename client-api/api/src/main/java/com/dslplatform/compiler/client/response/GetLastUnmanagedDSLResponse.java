package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.model.Migration;

public class GetLastUnmanagedDSLResponse extends DatabaseConnectionResponse {
    public final Migration lastMigration;

    public GetLastUnmanagedDSLResponse(
            final boolean databaseConnectionSuccessful,
            final String databaseConnectionErrorMessage,
            final Migration lastMigration) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.lastMigration = lastMigration;
    }
}
