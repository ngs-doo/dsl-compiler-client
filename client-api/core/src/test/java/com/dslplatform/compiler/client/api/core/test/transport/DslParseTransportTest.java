package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DslParseTransportTest extends HttpTransportImplTest {

    @Test
    public void testParseDsl() throws IOException {

        final HttpRequest parseRequest;
        {
            final String token = userToken(validUser, validPassword);

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

        final HttpRequest parseRequest;
        {
            final String token = userToken(validUser, inValidPassword);

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
}
