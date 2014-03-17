package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InspectManagedProjectChangesTransportTest extends HttpTransportImplTest {

    @Test
    public void testInspectManagedProjectChangesRequestInvalidDSL() throws IOException {
        final HttpRequest inspectManagedProjectChangesRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;!}}");
            }};
            inspectManagedProjectChangesRequest = httpRequestBuilder.inspectManagedProjectChanges(token, UUID.fromString(validID), dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(inspectManagedProjectChangesRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("DSL has misstakess.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testInspectManagedProjectChangesRequest() throws IOException {
        final HttpRequest inspectManagedProjectChangesRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            inspectManagedProjectChangesRequest = httpRequestBuilder.inspectManagedProjectChanges(token, UUID.fromString(validID), dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(inspectManagedProjectChangesRequest);
        assertEquals(201, response.code);
    }
}
