package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TemplateCreateTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateCreateRequest() throws IOException {
        final HttpRequest templateCreateRequest; {
            final String templateName = "templateName";
            final byte [] templateContent = "templateContent".getBytes("UTF-8");
            templateCreateRequest = httpRequestBuilder.templateCreate(token, validId, templateName, templateContent);
        }

        final HttpResponse response = httpTransport.sendRequest(templateCreateRequest);
        logger.info(new String(response.body, "UTF-8"));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
    }

    @Test
    public void testTemplateCreateRequestInvalidName() throws IOException {
        final HttpRequest templateCreateRequest; {
            final String templateName = "~_? templateName ";
            final byte [] templateContent = "templateContent".getBytes("UTF-8");
            templateCreateRequest = httpRequestBuilder.templateCreate(token, validId, templateName, templateContent);
        }

        final HttpResponse response = httpTransport.sendRequest(templateCreateRequest);
        assertEquals(400, response.code);
        logger.info(new String(response.body, "UTF-8"));
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Invalid template name.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testTemplateCreateRequestNameAbsent() throws IOException {
        final HttpRequest templateCreateRequest; {
            final String templateName = "";
            final byte [] templateContent = "templateContent".getBytes("UTF-8");
            templateCreateRequest = httpRequestBuilder.templateCreate(token, validId, templateName, templateContent);
        }

        final HttpResponse response = httpTransport.sendRequest(templateCreateRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Invalid template name.".getBytes("UTF-8"), response.body);
    }
}
