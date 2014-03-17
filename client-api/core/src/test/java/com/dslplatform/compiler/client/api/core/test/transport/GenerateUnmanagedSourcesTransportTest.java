package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenerateUnmanagedSourcesTransportTest extends HttpTransportImplTest {

    @Test
    public void testGenerateUnmanagedSourcesRequest() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final String token = userToken(validUser, validPassword);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B *b;}}");
            }};
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(token, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testGenerateUnmanagedSourcesRequestBadOptions() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final String token = userToken(validUser, validPassword);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B *b;}}");
            }};
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(token, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        assertEquals(200, response.code);
    }

    @Test
    public void testGenerateUnmanagedSourcesRequestBadLanguages() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final String token = userToken(validUser, validPassword);
            final Set<String> targets = new HashSet<String>() {{
                add("fantom");
            }};
            final Set<String> options = new HashSet<String>() {{}};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B *b;}}");
            }};
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(token, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Unknown language specified".getBytes("UTF-8"), response.body);
        assertEquals(400, response.code);
    }

    @Test
    public void testGenerateUnmanagedSourcesRequestBadDSL() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final String token = userToken(validUser, validPassword);
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{}};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("only", "module A { root B; root C{ B b;}}");
            }};
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(token, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        assertEquals(400, response.code);
        final String responseStr = new String(response.body, ENCODING);
        assertTrue(responseStr.contains("Aggregate root must be referenced with *"));
    }
}
