package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.apache.commons.codec.Charsets;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ParseDSLTransportTest extends HttpTransportImplTest {

    @Test
    public void testParseDsl() throws IOException {

        final HttpRequest parseRequest;
        {
            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("model.dsl", "module Foo {\n" +
                    "\taggregate Bar { String baz; }\n" +
                    "}");

            parseRequest = httpRequestBuilder.parseDSL(auth, dsl);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(parseRequest);
        logger.info(new String(parseResponse.body, Charsets.UTF_8));
        assertEquals(200, parseResponse.code);
        assertEquals(0, parseResponse.body.length);
    }

    @Test
    public void testParseDslError() throws IOException {

        final HttpRequest parseRequest;
        {
            final Map<String, String> dsl = new HashMap<String, String>();
            dsl.put("bad.dsl", "module Foo!");

            parseRequest = httpRequestBuilder.parseDSL(auth, dsl);
        }

        final HttpResponse parseResponse = httpTransport.sendRequest(parseRequest);
        assertEquals(400, parseResponse.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), parseResponse.headers.get("Content-Type"));
        assertTrue(parseResponse.body.length > 0);
        assertEquals(Arrays.asList(String.valueOf(parseResponse.body.length)), parseResponse.headers.get("Content-Length"));
        assertArrayEquals(parseResponse.body, new String(parseResponse.body, ENCODING).getBytes(ENCODING));
    }
}
