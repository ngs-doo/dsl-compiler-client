package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.model.Migration;

import java.util.List;

public class GetAllUnmanagedDSLResponse extends DatabaseConnectionResponse {
    public final List<Migration> allMigrations;

    public GetAllUnmanagedDSLResponse(
            final boolean databaseConnectionSuccessful,
            final String errorMessage,
            final List<Migration> allMigrations) {
        super(databaseConnectionSuccessful, errorMessage);
        this.allMigrations = allMigrations;
    }
}
