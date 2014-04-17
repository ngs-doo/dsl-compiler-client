package com.dslplatform.compiler.client.response;

import java.util.Map;

public class GenerateSourcesResponse extends AuthorizationResponse {

    public final Map<String, Map<String, String>> sources;

    public final boolean generatedSuccess;

    public GenerateSourcesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean generateSuccess,
            Map<String, Map<String, String>> sources) {
        super(authorized, authorizationErrorMessage);
        this.sources = sources;
        this.generatedSuccess = generateSuccess;
    }
}
