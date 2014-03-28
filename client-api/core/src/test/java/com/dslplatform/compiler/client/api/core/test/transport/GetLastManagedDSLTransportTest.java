package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GetLastManagedDSLTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetLastManagedDSLRequest() throws IOException {
        final HttpRequest getLastManagedDSLRequest; {
            final String token = projectToken(validUser, validPassword, validID);

            getLastManagedDSLRequest = httpRequestBuilder.getLastManagedDSL(token, UUID.fromString(validID));
        }

        final HttpResponse response = httpTransport.sendRequest(getLastManagedDSLRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }
}
