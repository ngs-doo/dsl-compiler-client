package com.dslplatform.compiler.client.api.core.io;

import java.io.IOException;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;

public interface HttpTransport {
    public HttpResponse sendRequest(final HttpRequest request) throws IOException;
}
