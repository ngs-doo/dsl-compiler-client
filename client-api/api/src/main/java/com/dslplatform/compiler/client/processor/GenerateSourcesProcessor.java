package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GenerateSourcesResponse;
import com.dslplatform.compiler.client.response.Source;

import java.nio.charset.Charset;
import java.util.List;

public class GenerateSourcesProcessor {
    public GenerateSourcesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String authResponseMessage = authSuccess ? null : new String(httpResponse.body, Charset.forName("UTF-8"));
        final boolean generateSuccess = (httpResponse.code == 200);
        final List<Source> sources = generateSuccess ? FromJson.orderedSources(httpResponse.body) : null;
        return new GenerateSourcesResponse(authSuccess, authResponseMessage, generateSuccess, sources);
    }
}
