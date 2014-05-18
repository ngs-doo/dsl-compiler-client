package com.dslplatform.compiler.client.response;

public class DatabaseConnectionResponse {
    public final boolean databaseConnectionSuccessful;
    public final String databaseConnectionErrorMessage;

    protected DatabaseConnectionResponse(boolean databaseConnectionSuccessful, String databaseConnectionErrorMessage) {
        this.databaseConnectionSuccessful = databaseConnectionSuccessful;
        this.databaseConnectionErrorMessage = databaseConnectionErrorMessage;
    }
}
