package com.dslplatform.compiler.client.response;

public class CleanProjectResponse extends AuthorizationResponse {

    final private boolean cleanSuccessful;
    public boolean isCleanSuccessful() {
        return cleanSuccessful;
    }

    public CleanProjectResponse(boolean authorized, String authorizationErrorMessage, boolean cleanSuccessful) {
        super(authorized, authorizationErrorMessage);
        this.cleanSuccessful = cleanSuccessful;
    }
}
