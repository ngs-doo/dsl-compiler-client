package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GetConfigResponse;

import java.nio.charset.Charset;
import java.util.Map;

public class GetConfigProcessor {

    public GetConfigResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;
        final Map<String, String> configs = !authSuccess ? FromJson.map(httpResponse.body) : null;

        return new GetConfigResponse(authSuccess, authResponseMessage, configs);
    }
}
