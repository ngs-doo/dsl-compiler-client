package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.GetLastManagedDSLResponse;

import java.nio.charset.Charset;
import java.util.Map;

public class GetLastManagedDSLProcessor {
    public GetLastManagedDSLResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String authResponseMessage = authSuccess ? null : new String(httpResponse.body, Charset.forName("UTF-8"));
        final Map<String, String> dsls = FromJson.map(httpResponse.body);

        return new GetLastManagedDSLResponse(authSuccess, authResponseMessage, dsls);
    }
}
