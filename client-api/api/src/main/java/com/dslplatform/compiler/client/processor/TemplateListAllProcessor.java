package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.TemplateListAllResponse;

import java.nio.charset.Charset;
import java.util.List;

public class TemplateListAllProcessor {
    public TemplateListAllResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;
        final List<String> templateNames = httpResponse.code == 200 ? FromJson.array(httpResponse.body) : null;

        return new TemplateListAllResponse(authSuccess, authResponseMessage, templateNames);
    }
}
