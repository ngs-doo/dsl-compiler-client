package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.HttpResponse;

public interface ResponseProcessor {
    public void processResponse(final HttpResponse httpResponse);
}
