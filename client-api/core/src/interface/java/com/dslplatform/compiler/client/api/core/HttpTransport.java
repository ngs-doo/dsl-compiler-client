package com.dslplatform.compiler.client.api.core;

import java.io.IOException;

public interface HttpTransport {
    public HttpResponse sendRequest(final HttpRequest request) throws IOException;
}
