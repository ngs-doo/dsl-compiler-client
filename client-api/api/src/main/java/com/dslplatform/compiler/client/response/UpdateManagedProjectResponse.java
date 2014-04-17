package com.dslplatform.compiler.client.response;

import java.util.Map;

public class UpdateManagedProjectResponse extends AuthorizationResponse {
    public final boolean updateSuccessful;

    public final Map<String, Map<String, String>> sources;

    public UpdateManagedProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean updateSuccessful,
            Map<String, Map<String, String>> sources) {
        super(authorized, authorizationErrorMessage);
        this.updateSuccessful = updateSuccessful;
        this.sources = sources;
    }
}
