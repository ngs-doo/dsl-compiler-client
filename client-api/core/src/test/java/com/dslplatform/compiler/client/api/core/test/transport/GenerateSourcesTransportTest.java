package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GenerateSourcesTransportTest extends HttpTransportImplTest {

    @Test
    public void testGenerateSourcesRequest() throws IOException {
        final HttpRequest generateSourcesRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
            }};
            final String packageName = "namespace";
            generateSourcesRequest = httpRequestBuilder.generateSources(token, UUID.fromString(validID), targets, packageName, options);
        }

        final HttpResponse response = httpTransport.sendRequest(generateSourcesRequest);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));

    }

    @Test
    public void testGenerateSourcesRequestUnknownLanguage() throws IOException {
        final HttpRequest generateSourcesRequest; {
            final String token = projectToken(validUser, validPassword, inValidID);
            final Set<String> targets = new HashSet<String>() {{
                add("fantom");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            generateSourcesRequest = httpRequestBuilder.generateSources(token, UUID.fromString(validID), targets, packageName, options);
        }

        final HttpResponse response = httpTransport.sendRequest(generateSourcesRequest);

        assertEquals(400, response.code);
        assertArrayEquals("Unknown language specified".getBytes(ENCODING), response.body);
    }
}
