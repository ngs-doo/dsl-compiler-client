package com.dslplatform.compiler.client.response;

import java.util.Map;

public class DownloadGeneratedModelResponse extends AuthorizationResponse {

    public DownloadGeneratedModelResponse(
            boolean authorized,
            String authorizationErrorMessage,
            Map<String, String> sources) {
        super(authorized, authorizationErrorMessage);
        this.sources = sources;
    }

    private final Map<String, String> sources;

    public Map<String, String> getSources() {
        return sources;
    }
}
