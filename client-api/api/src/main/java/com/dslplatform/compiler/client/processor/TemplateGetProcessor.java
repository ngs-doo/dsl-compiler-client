package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.TemplateGetResponse;

import java.nio.charset.Charset;

public class TemplateGetProcessor {
    public TemplateGetResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        final byte[] content = httpResponse.code == 200 ? httpResponse.body : null;
        return new TemplateGetResponse(authSuccess, authResponseMessage, content);
    }
}
