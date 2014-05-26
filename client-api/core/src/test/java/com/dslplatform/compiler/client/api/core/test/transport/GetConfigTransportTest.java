package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class GetConfigTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetConfigRequestValid() throws IOException {
        final HttpRequest getConfigRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            getConfigRequest = httpRequestBuilder.getConfig(auth, UUID.fromString(validId), targets, packageName, options);
        }

        final HttpResponse response = httpTransport.sendRequest(getConfigRequest);
        logger.info("Response: {}", new String(response.body, "UTF-8"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }
}
