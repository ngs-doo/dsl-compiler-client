package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.DownloadBinariesResponse;

import java.nio.charset.Charset;
import java.util.HashMap;

public class DownloadBinariesProcessor {
    public DownloadBinariesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        return new DownloadBinariesResponse(authSuccess, authResponseMessage, new HashMap<String, byte[]>());
    }
}
