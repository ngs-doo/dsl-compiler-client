package com.dslplatform.compiler.client.response;

import java.util.Map;

public class GenerateUnmanagedSourcesResponse extends AuthorizationResponse {
    public final Map<String, Map<String, String>> sources; // TODO - response here is too raw, but for now it will be good.

    public final boolean generationSuccessful;

    public GenerateUnmanagedSourcesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean generationSuccessful,
            Map<String, Map<String, String>> sources) {
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
