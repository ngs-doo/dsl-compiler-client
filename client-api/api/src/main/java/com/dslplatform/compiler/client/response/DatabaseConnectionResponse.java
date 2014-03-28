package com.dslplatform.compiler.client.response;

public class DatabaseConnectionResponse {
    private final boolean databaseConnectionSuccessful;
    private final String databaseConnectionErrorMessage;

    protected DatabaseConnectionResponse(boolean databaseConnectionSuccessful, String databaseConnectionErrorMessage) {
        this.databaseConnectionSuccessful = databaseConnectionSuccessful;
        this.databaseConnectionErrorMessage = databaseConnectionErrorMessage;
    }

    public String getDatabaseConnectionErrorMessage() {
        return databaseConnectionErrorMessage;
    }

    public boolean isDatabaseConnectionSuccessful() {

        return databaseConnectionSuccessful;
    }
}
