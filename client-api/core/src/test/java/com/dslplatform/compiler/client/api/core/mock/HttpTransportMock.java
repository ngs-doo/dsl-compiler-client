package com.dslplatform.compiler.client.api.core.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.io.HttpTransport;
import com.dslplatform.compiler.client.api.core.mock.processor.*;

public class HttpTransportMock implements HttpTransport {
    private final List<MockProcessor> mockProcessors;

    public HttpTransportMock() {
        mockProcessors = new ArrayList<MockProcessor>();
        mockProcessors.add(new ParseProcessor());
        mockProcessors.add(new RenameProjectProcessor());
        mockProcessors.add(new RegisterUserProcessor());
        mockProcessors.add(new CreateProjectProcessor());


    }

    public HttpResponse sendRequest(final HttpRequest request) throws IOException {
        for (final MockProcessor mockProcessor : mockProcessors) {
            if (mockProcessor.isDefinedAt(request)) {
                return mockProcessor.apply(request);
            }
        }

        throw new UnsupportedOperationException("Could not locate mock processor for path: " + request.path);
    }
}
