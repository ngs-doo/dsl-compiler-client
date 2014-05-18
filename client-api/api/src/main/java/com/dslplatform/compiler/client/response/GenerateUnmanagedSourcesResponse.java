package com.dslplatform.compiler.client.response;

import java.util.List;

public class GenerateUnmanagedSourcesResponse extends AuthorizationResponse {
    public final List<Source> sources;

    public final boolean generationSuccessful;

    public GenerateUnmanagedSourcesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean generationSuccessful,
            List<Source> sources) {
        super(authorized, authorizationErrorMessage);
        this.sources = sources;
        this.generationSuccessful = generationSuccessful;
    }

    public GenerateUnmanagedSourcesResponse(
            boolean authorized,
            String authorizationErrorMessage) {
        super(authorized, authorizationErrorMessage);
        sources = null;
        this.generationSuccessful = false;
    }
}
