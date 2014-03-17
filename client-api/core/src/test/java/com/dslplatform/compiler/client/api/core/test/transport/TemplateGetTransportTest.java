package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TemplateGetTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateGetRequestInvalidName() throws IOException {
        final HttpRequest templateGetRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String projectName = "!";
            templateGetRequest = httpRequestBuilder.templateGet(token, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateGetRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testTemplateGetRequestNameAbsent() throws IOException {
        final HttpRequest templateGetRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String projectName = "";
            templateGetRequest = httpRequestBuilder.templateGet(token, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateGetRequest);
        assertEquals(201, response.code);
    }
}
