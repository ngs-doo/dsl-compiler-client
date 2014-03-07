package com.dslplatform.compiler.client.api.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;

public class HttpRequestBuilderImplTest {
    private static final Charset ENCODING = Charset.forName("UTF-8");

    private static HttpRequestBuilder httpRequestBuilder;

    @BeforeClass
    public static void createHttpRequestBuilder() {
        httpRequestBuilder = new HttpRequestBuilderImpl();
    }

    @Test
    @SuppressWarnings("serial")
    public void testParseDslBuilder() throws IOException {
        final HttpRequest parseRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("model.dsl", "module Foo {\n" +
                                   "\taggregate Bar { String baz; }\n" +
                                 "}");
            parseRequest = httpRequestBuilder.parseDsl(token, dsl);
        }

        assertEquals(HttpRequest.Method.PUT, parseRequest.method);
        assertEquals("Alpha.svc/parse", parseRequest.path);
        assertEquals(parseRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING))));
        }});

        assertArrayEquals(
                "{\"model.dsl\":\"module Foo {\\n\\taggregate Bar { String baz; }\\n}\"}".getBytes(ENCODING),
                parseRequest.body);
    }

    @Test
    @SuppressWarnings("serial")
    public void testRenameProjectBuilder() throws IOException {
        final HttpRequest renameRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "GreenLeopard";
            final String newName = "GreenLion";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        assertEquals(renameRequest.method, HttpRequest.Method.POST);
        assertEquals(renameRequest.path, "Domain.svc/submit/Client.RenameProject");
        assertEquals(renameRequest.headers, new HashMap<String, List<String>>() {{
            put("Content-Type", Arrays.asList("application/json"));
            put("Accept", Arrays.asList("application/json"));
            put("Authorization", Arrays.asList("Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING))));
        }});
        assertArrayEquals(
                "{\"OldName\":\"GreenLeopard\",\"NewName\":\"GreenLion\"}".getBytes(ENCODING),
                renameRequest.body);
    }}
