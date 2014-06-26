package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GenerateSourcesResponse;
import com.dslplatform.compiler.client.response.Source;

import java.nio.charset.Charset;
import java.util.List;

public class GenerateUnmanagedSourcesProcessor {
    public GenerateSourcesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final boolean generationSuccessful = httpResponse.code == 200;
        final String authResponseMessage = generationSuccessful ? null : new String(httpResponse.body, Charset.forName("UTF-8"));
        final List<Source> generatedSources = generationSuccessful ? FromJson.orderedSources(httpResponse.body) : null;

        return new GenerateSourcesResponse(authSuccess, authResponseMessage, generationSuccessful, generatedSources);
    }
}
