package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class GetLastManagedDSLTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetLastManagedDSLRequest() throws IOException {
        final HttpRequest getLastManagedDSLRequest; {
            getLastManagedDSLRequest = httpRequestBuilder.getLastManagedDSL(auth, UUID.fromString(validId));
        }

        final HttpResponse response = httpTransport.sendRequest(getLastManagedDSLRequest);
        final String responseBodyStr = new String(response.body, "UTF-8");
        logger.info("Response: {}", responseBodyStr);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        assertTrue(new String(response.body, "UTF-8").contains("module"));
    }
}
