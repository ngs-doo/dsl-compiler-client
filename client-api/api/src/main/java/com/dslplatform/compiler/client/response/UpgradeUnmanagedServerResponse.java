package com.dslplatform.compiler.client.response;

import java.util.List;

public class UpgradeUnmanagedServerResponse extends AuthorizationResponse {
    public final String migration;

    public final List<Source>  serverSource;

    public UpgradeUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            List<Source> serverSource) {
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
