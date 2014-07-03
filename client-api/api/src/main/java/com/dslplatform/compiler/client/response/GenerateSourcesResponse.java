package com.dslplatform.compiler.client.response;

import java.util.List;

public class GenerateSourcesResponse extends AuthorizationResponse {

    public final List<Source> sources;

    public final boolean generatedSuccess;

    public GenerateSourcesResponse(boolean authorized, String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        sources = null;
        generatedSuccess = false;
    }

    public GenerateSourcesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean generateSuccess,
            List<Source> sources) {
        super(authorized, authorizationErrorMessage);
        this.sources = sources;
        this.generatedSuccess = generateSuccess;
    }
}
