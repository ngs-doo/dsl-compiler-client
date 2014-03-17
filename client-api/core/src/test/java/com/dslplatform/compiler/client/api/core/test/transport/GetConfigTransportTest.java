package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GetConfigTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetConfigRequestValid() throws IOException {
        final HttpRequest getConfigRequest; {
            final String token = projectToken(validUser, inValidPassword, validID);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};

            final String packageName = "namespace";
            getConfigRequest = httpRequestBuilder.getConfig(token, UUID.fromString(validID), targets, packageName, options);
        }

        final HttpResponse response = httpTransport.sendRequest(getConfigRequest);
        assertEquals(201, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }
}
