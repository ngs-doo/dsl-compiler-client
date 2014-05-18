package com.dslplatform.compiler.client.response;

import java.util.List;

public class CreateUnmanagedProjectResponse extends UpgradeUnmanagedServerResponse {

    public CreateUnmanagedProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            String migration,
            List<Source> serverSource) {
        super(authorized, authorizationErrorMessage, migration, serverSource);
    }
}
