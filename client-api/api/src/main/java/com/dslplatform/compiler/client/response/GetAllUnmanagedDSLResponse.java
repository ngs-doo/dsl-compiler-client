package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.core.Migration;
import java.util.List;

public class GetAllUnmanagedDSLResponse extends DatabaseConnectionResponse{

    public List<Migration> getAllMigrations() {
        return allMigrations;
    }

    final private List<Migration> allMigrations;

    public GetAllUnmanagedDSLResponse(
            final boolean databaseConnectionSuccessful,
            final String errorMessage,
            final List<Migration> allMigrations) {
        super(databaseConnectionSuccessful, errorMessage);
        this.allMigrations = allMigrations;
    }
}
