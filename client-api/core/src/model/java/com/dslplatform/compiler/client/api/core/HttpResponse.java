package com.dslplatform.compiler.client.api.core;

import java.util.Map;
import java.util.List;

public final class HttpResponse {
    public final int code;
    public final Map<String, List<String>> headers;
    public final byte[] body;

    public HttpResponse(
            final int code,
            final Map<String, List<String>> headers,
            final byte[] body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }
}
