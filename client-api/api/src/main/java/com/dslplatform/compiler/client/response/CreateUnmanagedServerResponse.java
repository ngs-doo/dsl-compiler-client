package com.dslplatform.compiler.client.response;

import java.util.List;

public class CreateUnmanagedServerResponse extends UpgradeUnmanagedServerResponse {

    public CreateUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            List<Source> serverSource) {
        super(authorized, authorizationErrorMessage, migration, serverSource);
    }

    public CreateUnmanagedServerResponse(
            boolean authorized,
            String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
    }
}
