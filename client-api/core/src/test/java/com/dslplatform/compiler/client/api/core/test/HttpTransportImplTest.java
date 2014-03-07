package com.dslplatform.compiler.client.api.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.io.HttpTransport;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;

public class HttpTransportImplTest {
    private static HttpRequestBuilder httpRequestBuilder;
    private static HttpTransport httpTransport;

    @BeforeClass
    public static void createHttpRequestBuilder() {
        httpRequestBuilder = new HttpRequestBuilderImpl();
    }

    @BeforeClass
    public static void createHttpTransport() throws IOException {
//        final Logger logger = org.slf4j.LoggerFactory.getLogger("core-test");
//        final PathExpander pathExpander = new PathExpander(logger);
//        final StreamLoader streamLoader = new StreamLoader(logger, pathExpander);
//        final PropertyLoader propertyLoader = new PropertyLoader(logger, streamLoader);
//
//        final ClientConfigurationFactory ccf = new PropertyClientConfigurationFactory(logger, propertyLoader, "/api.properties");
//        final ClientConfiguration clientConfiguration = ccf.getClientConfiguration();
//
//        httpTransport = new HttpTransportImpl(logger, clientConfiguration, streamLoader);

        httpTransport = new HttpTransportMock();
    }

    private static final Charset ENCODING = Charset.forName("UTF-8");

    @Test
    public void testParseDslSuccess() throws IOException {

        final HttpRequest parseRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));

            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("model.dsl", "module Foo {\n" +
                                 "\taggregate Bar { String baz; }\n" +
                                 "}");

            parseRequest = httpRequestBuilder.parseDsl(token, dsl);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(parseRequest);
        assertEquals(200, parseResponse.code);
        assertEquals(Arrays.asList("0"), parseResponse.headers.get("Content-Length"));
        assertEquals(0, parseResponse.body.length);
    }

    @Test
    public void testParseDslError() throws IOException {

        final HttpRequest parseRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));

            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("model.dsl", "module Foo!");

            parseRequest = httpRequestBuilder.parseDsl(token, dsl);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(parseRequest);
        assertEquals(400, parseResponse.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), parseResponse.headers.get("Content-Type"));
        assertTrue(parseResponse.body.length > 0);
        assertEquals(Arrays.asList(String.valueOf(parseResponse.body.length)), parseResponse.headers.get("Content-Length"));
        // body must be present and UTF-8 decodeable
        assertArrayEquals(parseResponse.body, new String(parseResponse.body, ENCODING).getBytes(ENCODING));
    }

    @Test
    public void testRenameProjectBuilderInvalidName() throws IOException {
        final HttpRequest renameRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "GreenLion";
            final String newName = "!";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(renameRequest);
        assertEquals(400, parseResponse.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), parseResponse.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), parseResponse.body);
    }

    @Test
    public void testRenameProjectBuilderNotFound() throws IOException {
        final HttpRequest renameRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "?";
            final String newName = "!";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(renameRequest);
        assertEquals(400, parseResponse.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), parseResponse.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), parseResponse.body);
    }

    @Test
    public void testRenameProjectBuilder() throws IOException {
        final HttpRequest renameRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "GreenLion";
            final String newName = "GreenLion2";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(renameRequest);
        assertEquals(201, parseResponse.code);
    }
}
