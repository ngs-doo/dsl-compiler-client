package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CreateProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testCreateProjectRequest() throws IOException {
        final HttpRequest createTestProjectRequest; {
            final String token = userToken(validUser, validPassword);
            final String projectName = "SomeProjectName2";
            createTestProjectRequest = httpRequestBuilder.createTestProject(token, projectName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(createTestProjectRequest);
        logger.info(new String(parseResponse.body));
        assertEquals(201, parseResponse.code);
        assertEquals(Arrays.asList("application/json"), parseResponse.headers.get("Content-Type"));
    }

    @Test
    public void testCreateProjectRequestNameAbsent() throws IOException {
        final HttpRequest createTestProjectRequest; {
            final String token = userToken(validUser, validPassword);
            final String projectName = "";
            createTestProjectRequest = httpRequestBuilder.createTestProject(token, projectName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(createTestProjectRequest);
        assertEquals(201, parseResponse.code); // TODO - This should be 400+
    }
}
