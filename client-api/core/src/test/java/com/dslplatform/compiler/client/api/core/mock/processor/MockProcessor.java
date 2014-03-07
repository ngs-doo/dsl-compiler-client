package com.dslplatform.compiler.client.api.core.mock.processor;

import java.io.IOException;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;

public interface MockProcessor {
    public boolean isDefinedAt(final HttpRequest request);
    public HttpResponse apply(final HttpRequest request) throws IOException;
}
