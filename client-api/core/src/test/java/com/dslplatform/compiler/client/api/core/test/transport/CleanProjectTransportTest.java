/*
  TODO - "Can't find domain object: Client.CleanProject"
package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CleanProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testCleanProjectRequestInvalidId() throws IOException {
        final HttpRequest cleanProjectRequest; {
            final String token = projectToken(validUser, validPassword, inValidID);
            cleanProjectRequest = httpRequestBuilder.cleanProject(token);
        }

        final HttpResponse response = httpTransport.sendRequest(cleanProjectRequest);
        logger.info(new String(response.body, "UTF-8"));
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        assertArrayEquals("Project ...".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testCleanProjectRequestValid() throws IOException {
        final HttpRequest cleanProjectRequest; {
            final String token = projectToken(validUser, validPassword, validId);
            cleanProjectRequest = httpRequestBuilder.cleanProject(token);
        }

        final HttpResponse response = httpTransport.sendRequest(cleanProjectRequest);
        assertEquals(201, response.code);
    }
}
    */
