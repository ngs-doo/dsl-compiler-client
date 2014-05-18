package com.dslplatform.compiler.client.response;

public class InspectUnmanagedProjectChangesResponse extends DatabaseConnectionResponse {
    public final String diff;

    public InspectUnmanagedProjectChangesResponse(
            boolean databaseConnectionSuccessful,
            String databaseConnectionErrorMessage,
            String diff) {
        super(databaseConnectionSuccessful, databaseConnectionErrorMessage);
        this.diff = diff;
    }
}
