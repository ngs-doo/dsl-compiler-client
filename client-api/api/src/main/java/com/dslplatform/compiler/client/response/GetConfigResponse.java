package com.dslplatform.compiler.client.response;

import java.util.Map;

public class GetConfigResponse extends AuthorizationResponse {
    final Map<String, String> config;

    public GetConfigResponse(
            boolean authorized,
            String authorizationErrorMessage,
            Map<String, String> config) {
        super(authorized, authorizationErrorMessage);
        this.config = config;
    }
}
