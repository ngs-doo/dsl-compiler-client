package com.dslplatform.compiler.client.api.core.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.mock.processor.*;

public class HttpTransportMock implements HttpTransport {
    private final List<MockProcessor> mockProcessors;

    public HttpTransportMock() {
        mockProcessors = new ArrayList<MockProcessor>();
        mockProcessors.add(new ParseDSLProcessor());
        mockProcessors.add(new RenameProjectProcessor());
        mockProcessors.add(new RegisterUserProcessor());
        mockProcessors.add(new CreateProjectProcessor());
        mockProcessors.add(new CreateExternalProjectProcessor());
        mockProcessors.add(new DownloadGeneratedModelProcessor());
        mockProcessors.add(new DownloadBinariesProcessor());
        mockProcessors.add(new InspectManagedProjectChangesProcessor());
        mockProcessors.add(new GetLastManagedDSLProcessor());
        mockProcessors.add(new GetConfigProcessor());
        mockProcessors.add(new UpdateManagedProjectProcessor());
        mockProcessors.add(new GenerateMigrationSQLProcessor());
        mockProcessors.add(new GenerateSourcesProcessor());
        mockProcessors.add(new GenerateUnmanagedSourcesProcessor());
        mockProcessors.add(new GetProjectByNameProcessor());
        mockProcessors.add(new GetAllProjectsProcessor());
        mockProcessors.add(new RenameProjectProcessor());
        mockProcessors.add(new CleanProjectProcessor());
        mockProcessors.add(new TemplateGetProcessor());
        mockProcessors.add(new TemplateCreateProcessor());
        mockProcessors.add(new TemplateListAllProcessor());
        mockProcessors.add(new TemplateDeleteProcessor());
    }

    public HttpResponse sendRequest(final HttpRequest request) throws IOException {
        for (final MockProcessor mockProcessor : mockProcessors) {
            if (mockProcessor.isDefinedAt(request)) {
                return mockProcessor.apply(request);
            }
        }

        throw new UnsupportedOperationException("Could not locate mock processor for " + request.method.name() + " to path: " + request.path);
    }
}
