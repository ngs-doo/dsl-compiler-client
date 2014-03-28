package com.dslplatform.compiler.client.response;

public class AuthorizationResponse {
    private final boolean authorized;
    private final String authorizationErrorMessage;

    protected AuthorizationResponse(boolean authorized, String authorizationErrorMessage) {
        this.authorized = authorized;
        this.authorizationErrorMessage = authorizationErrorMessage;
    }

    public String getAuthorizationErrorMessage() {
        return authorizationErrorMessage;
    }

    public boolean isAuthorized() {
        return authorized;
    }
}
