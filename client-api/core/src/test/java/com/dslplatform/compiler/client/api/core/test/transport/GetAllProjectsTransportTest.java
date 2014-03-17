package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetAllProjectsTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetAllProjectsRequest() throws IOException {
        final HttpRequest getAllProjectsRequest; {
            final String token = userToken(validUser, validPassword);
            getAllProjectsRequest = httpRequestBuilder.getAllProjects(token);
        }

        final HttpResponse response = httpTransport.sendRequest(getAllProjectsRequest);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        final String responseStr = new String(response.body, ENCODING);
        assertTrue(responseStr.contains("CreatedAt"));
        assertTrue(responseStr.contains("UserURI"));
    }

    @Test
    public void testGetAllProjectsRequestInvalidUser() throws IOException {
        final HttpRequest getAllProjectsRequest; {
            final String token = userToken(validUser, inValidPassword);
            getAllProjectsRequest = httpRequestBuilder.getAllProjects(token);
        }

        final HttpResponse response = httpTransport.sendRequest(getAllProjectsRequest);
        assertEquals(201, response.code);
    }
}
