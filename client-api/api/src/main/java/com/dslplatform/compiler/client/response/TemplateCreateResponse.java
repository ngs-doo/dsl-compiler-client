package com.dslplatform.compiler.client.response;

public class TemplateCreateResponse extends AuthorizationResponse {

    public final boolean successful;

    public TemplateCreateResponse(boolean authorized, String authorizationErrorMessage, boolean successful) {
        super(authorized, authorizationErrorMessage);
        this.successful = successful;
    }
}
