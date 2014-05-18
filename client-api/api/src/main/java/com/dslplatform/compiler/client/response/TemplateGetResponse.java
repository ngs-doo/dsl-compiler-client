package com.dslplatform.compiler.client.response;

public class TemplateGetResponse extends AuthorizationResponse {

    public final byte[] templateContent;

    public TemplateGetResponse(boolean authorized, String authorizationErrorMessage, byte[] templateContent) {
        super(authorized, authorizationErrorMessage);
        this.templateContent = templateContent;
    }
}
