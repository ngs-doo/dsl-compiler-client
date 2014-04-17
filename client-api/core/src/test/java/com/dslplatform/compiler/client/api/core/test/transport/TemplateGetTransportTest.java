package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TemplateGetTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateGetRequest() throws IOException {
        final HttpRequest templateGetRequest; {
            final String token = projectToken(validUser, validPassword, validId);
            templateGetRequest = httpRequestBuilder.templateGet(token, validId, "agreggated-report.docx");
        }

        final HttpResponse response = httpTransport.sendRequest(templateGetRequest);
        logger.info("response {}", new String(response.body));
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        assertTrue(response.body.length > 0);
    }

    @Test
    public void testTemplateGetRequestNameAbsent() throws IOException {
        final HttpRequest templateGetRequest; {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String projectName = "";
            templateGetRequest = httpRequestBuilder.templateGet(token, validId, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateGetRequest);
        assertEquals(201, response.code);
    }
}
