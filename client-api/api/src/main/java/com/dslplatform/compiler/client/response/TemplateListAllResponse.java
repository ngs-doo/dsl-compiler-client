package com.dslplatform.compiler.client.response;

import java.util.List;

public class TemplateListAllResponse extends AuthorizationResponse {

    public final List<String> allTemplateNames;

    public TemplateListAllResponse(
            boolean authorized,
            String authorizationErrorMessage,
            List<String> allTemplateNames) {
        super(authorized, authorizationErrorMessage);
        this.allTemplateNames = allTemplateNames;
    }
}
