package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GenerateSourcesResponse;

import java.nio.charset.Charset;
import java.util.Map;

public class GenerateSourcesProcessor {
    public GenerateSourcesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String authResponseMessage = authSuccess ? null : new String(httpResponse.body, Charset.forName("UTF-8"));
        final boolean generateSuccess = (httpResponse.code == 200);
        final Map<String, Map<String, String>> sources = generateSuccess ? FromJson.orderedSources(httpResponse.body) : null;
        return new GenerateSourcesResponse(authSuccess, authResponseMessage, generateSuccess, sources);
    }
}
