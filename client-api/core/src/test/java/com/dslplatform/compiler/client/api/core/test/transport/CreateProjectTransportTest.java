package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class CreateProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testCreateProjectRequest() throws IOException {
        final HttpRequest createTestProjectRequest; {
            final String projectName = "SomeProjectName" + new Random().nextInt();
            createTestProjectRequest = httpRequestBuilder.createTestProject(auth, projectName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(createTestProjectRequest);
        logger.info(new String(parseResponse.body));
        assertEquals(201, parseResponse.code);
        assertEquals(Arrays.asList("application/json"), parseResponse.headers.get("Content-Type"));
    }

    @Test
    public void testCreateProjectRequestNameAbsent() throws IOException {
        final HttpRequest createTestProjectRequest; {
            final String projectName = "";
            createTestProjectRequest = httpRequestBuilder.createTestProject(auth, projectName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(createTestProjectRequest);
        assertEquals(201, parseResponse.code); // TODO - This should be 400+
    }
}
