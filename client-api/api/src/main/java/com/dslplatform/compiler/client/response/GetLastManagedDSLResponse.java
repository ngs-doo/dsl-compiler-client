package com.dslplatform.compiler.client.response;

import java.util.Map;

public class GetLastManagedDSLResponse extends AuthorizationResponse {
    public final Map<String, String> dsls;

    public GetLastManagedDSLResponse(
            boolean authorized,
            String authorizationErrorMessage,
            Map<String, String> dsls) {
        super(authorized, authorizationErrorMessage);
        this.dsls = dsls;
    }
}
