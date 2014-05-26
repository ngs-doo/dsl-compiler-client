package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenerateUnmanagedSourcesTransportTest extends HttpTransportImplTest {

    @Test
    public void testGenerateUnmanagedSourcesRequest_CSharpServer() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("CSharpServer");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = new LinkedHashMap<String, String>(){{
                put("2.dsl", MockData.test_migration_sql_simple_2);
            }};
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(auth, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        logger.info("response" + new String(response.body));
        assertTrue(new String(response.body).contains("postgres"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateUnmanagedSourcesRequest_CS_J() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("CSharpServer");
                add("Java");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = MockData.dsl_test_migration_single_2;
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(auth, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        logger.info(new String(response.body));
        assertTrue(new String(response.body).contains("postgres"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateUnmanagedSourcesRequest_CS_S() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("CSharpServer");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = MockData.dsl_test_migration_single_2;
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(auth, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        logger.info(new String(response.body));
        assertTrue(new String(response.body).contains("postgres"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateUnmanagedSourcesRequest_WithLast() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("ScalaServer");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("with-active-record");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = MockData.migrate_with;
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(auth, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        logger.info(new String(response.body));
        assertTrue(new String(response.body).contains("postgres"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateUnmanagedSourcesRequestBadOptions() throws IOException {
        final HttpRequest generateUnmanagedSourcesRequest; {
            final Set<String> targets = new HashSet<String>() {{
                add("Java");
                add("Scala");
            }};
            final Set<String> options = new HashSet<String>() {{
                add("opt1");
                add("opt2");
            }};
            final String packageName = "namespace";
            final Map<String, String> dsl = MockData.dsl_test_migration_single_2;
            generateUnmanagedSourcesRequest = httpRequestBuilder.generateUnmanagedSources(auth, packageName, targets, options, dsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateUnmanagedSourcesRequest);
        assertEquals(200, response.code);
    }
}
