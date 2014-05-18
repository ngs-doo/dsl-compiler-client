package com.dslplatform.compiler.client.response;

public class DoesUnmanagedDSLExitsResponse extends DatabaseConnectionResponse {

    public final boolean databaseExists;

    public DoesUnmanagedDSLExitsResponse(
            boolean databaseConnectionSuccessful,
            String databaseConnectionErrorMessage, boolean databaseExists) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.databaseExists = databaseExists;
    }
}
