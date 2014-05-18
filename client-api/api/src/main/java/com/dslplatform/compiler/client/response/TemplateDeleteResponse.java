package com.dslplatform.compiler.client.response;

public class TemplateDeleteResponse extends AuthorizationResponse {

    public final boolean successful;

    public TemplateDeleteResponse(boolean authorized, String authorizationErrorMessage, boolean successful) {
        super(authorized, authorizationErrorMessage);
        this.successful = successful;
    }
}
