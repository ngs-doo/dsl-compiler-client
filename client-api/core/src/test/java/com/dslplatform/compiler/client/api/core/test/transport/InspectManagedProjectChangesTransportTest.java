package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class InspectManagedProjectChangesTransportTest extends HttpTransportImplTest {

    @Test
    public void testInspectManagedProjectChangesRequest() throws IOException {
        final HttpRequest inspectManagedProjectChangesRequest; {
            final String token = projectToken(validUser, validPassword, validId);
            final Map<String, String> dsl = MockData.dsl_changed_AB;
            inspectManagedProjectChangesRequest = httpRequestBuilder.inspectManagedProjectChanges(token, UUID.fromString(validId), dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(inspectManagedProjectChangesRequest);
        final String responseStr = new String(response.body, "UTF-8");
        logger.info("Response: {}", responseStr);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        assertTrue(responseStr.contains("Description"));
    }

    @Test
    public void testInspectManagedProjectChangesRequestInvalidDSL() throws IOException {
        final HttpRequest inspectManagedProjectChangesRequest; {
            final String token = projectToken(validUser, validPassword, validId);
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("bad.dsl", "module A { root B; root C{ B b;!}}");
            }};
            inspectManagedProjectChangesRequest = httpRequestBuilder.inspectManagedProjectChanges(token, UUID.fromString(validId), dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(inspectManagedProjectChangesRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
    }
}
