package com.dslplatform.compiler.client.response;

import java.util.List;

public class UpdateManagedProjectResponse extends AuthorizationResponse {
    public final boolean updateSuccessful;

    public final List<Source> sources;

    public UpdateManagedProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean updateSuccessful,
            List<Source> sources) {
        super(authorized, authorizationErrorMessage);
        this.updateSuccessful = updateSuccessful;
        this.sources = sources;
    }
}
