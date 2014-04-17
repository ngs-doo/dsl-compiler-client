package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GenerateUnmanagedSourcesResponse;

import java.nio.charset.Charset;
import java.util.Map;

public class GenerateUnmanagedSourcesProcessor {
    public GenerateUnmanagedSourcesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final boolean generationSuccessful = httpResponse.code == 200;
        final String authResponseMessage = generationSuccessful ? null : new String(httpResponse.body, Charset.forName("UTF-8"));
        final Map<String, Map<String, String>> generatedSources = generationSuccessful ? FromJson.orderedSources(httpResponse.body) : null;

        return new GenerateUnmanagedSourcesResponse(authSuccess, authResponseMessage, generationSuccessful, generatedSources);
    }
}
