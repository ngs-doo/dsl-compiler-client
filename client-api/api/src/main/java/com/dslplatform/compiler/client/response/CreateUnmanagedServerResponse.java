package com.dslplatform.compiler.client.response;

import java.util.Map;

public class CreateUnmanagedServerResponse extends UpgradeUnmanagedServerResponse {

    public CreateUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            Map<String, Map<String, String>> serverSource) {
        super(authorized, authorizationErrorMessage, migration, serverSource);
    }

    public CreateUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
    }
}
