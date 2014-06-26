package com.dslplatform.compiler.client.response;

import java.util.List;

public class UpdateManagedProjectResponse extends AuthorizationResponse {
    public final boolean updateSuccessful;

    public final String unsuccessfulUpdateMessage;

    public final List<Source> sources;

    public UpdateManagedProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean updateSuccessful,
            List<Source> sources) {
        super(authorized, authorizationErrorMessage);
        this.updateSuccessful = updateSuccessful;
        this.sources = sources;
        this.unsuccessfulUpdateMessage = null;
    }

    public UpdateManagedProjectResponse(boolean authorized, String authorizationErrorMessage, boolean updateSuccessful, String unsuccessfulUpdateMessage) {
        super(authorized, authorizationErrorMessage);
        this.updateSuccessful = updateSuccessful;
        this.unsuccessfulUpdateMessage = unsuccessfulUpdateMessage;
        this.sources = null;
    }

    public UpdateManagedProjectResponse(boolean authorized, String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        this.updateSuccessful = false;
        this.unsuccessfulUpdateMessage = null;
        this.sources = null;
    }
}
