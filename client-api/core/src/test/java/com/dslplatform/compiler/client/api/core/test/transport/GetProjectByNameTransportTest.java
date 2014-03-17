package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GetProjectByNameTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetProjectByNameRequestInvalidName() throws IOException {
        final HttpRequest getProjectByNameRequest; {
            final String token = userToken(validUser, validPassword);
            final String projectName = "!";
            getProjectByNameRequest = httpRequestBuilder.getProjectByName(token, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(getProjectByNameRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testGetProjectByNameRequestNameAbsent() throws IOException {
        final HttpRequest getProjectByNameRequest; {
            final String token = userToken(validUser, validPassword);
            final String projectName = "";
            getProjectByNameRequest = httpRequestBuilder.getProjectByName(token, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(getProjectByNameRequest);
        assertEquals(201, response.code);
    }
}
