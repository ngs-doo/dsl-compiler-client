package com.dslplatform.compiler.client.response;

import java.util.Map;

public class CreateUnmanagedProjectResponse extends UpgradeUnmanagedServerResponse {

    // TODO - api binary
    public CreateUnmanagedProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            Map<String, Map<String, String>> serverSource) {
        super(authorized, authorizationErrorMessage, migration, serverSource);
    }
}
