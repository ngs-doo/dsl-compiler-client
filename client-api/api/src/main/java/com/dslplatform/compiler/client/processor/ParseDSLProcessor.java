package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.ParseDSLResponse;

import java.nio.charset.Charset;

public class ParseDSLProcessor {
    public ParseDSLResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        final boolean parseDSLSuccess = (authSuccess && (httpResponse.code == 200));
        final String parseSuccessMessage = parseDSLSuccess ? "DSL parse successful" : httpResponseString;

        return new ParseDSLResponse(authSuccess, authResponseMessage, parseDSLSuccess, parseSuccessMessage);
    }
}
