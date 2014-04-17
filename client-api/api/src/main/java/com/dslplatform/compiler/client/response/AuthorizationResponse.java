package com.dslplatform.compiler.client.response;

public class AuthorizationResponse {
    public final boolean authorized;
    public final String authorizationErrorMessage;

    protected AuthorizationResponse(boolean authorized, String authorizationErrorMessage) {
        this.authorized = authorized;
        this.authorizationErrorMessage = authorizationErrorMessage;
    }
}
