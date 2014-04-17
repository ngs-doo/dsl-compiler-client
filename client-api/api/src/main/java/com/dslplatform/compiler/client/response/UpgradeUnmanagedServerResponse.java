package com.dslplatform.compiler.client.response;

import java.util.Map;

public class UpgradeUnmanagedServerResponse extends AuthorizationResponse {
    public final String migration;

    public final Map<String, Map<String, String>>  serverSource;

    public UpgradeUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            Map<String, Map<String, String>> serverSource) {
        super(authorized, authorizationErrorMessage);
        this.migration = migration;
        this.serverSource = serverSource;
    }

    public UpgradeUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        this.migration = null;
        this.serverSource = null;
    }
}
