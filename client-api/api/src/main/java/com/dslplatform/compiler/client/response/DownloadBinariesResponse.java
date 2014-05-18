package com.dslplatform.compiler.client.response;

import java.util.Map;

public class DownloadBinariesResponse extends AuthorizationResponse {

    public DownloadBinariesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            Map<String, byte[]> sources) {
        super(authorized, authorizationErrorMessage);
        this.sources = sources;
    }

    private final Map<String, byte[]> sources;

    public Map<String, byte[]> getSources() {
        return sources;
    }
}
