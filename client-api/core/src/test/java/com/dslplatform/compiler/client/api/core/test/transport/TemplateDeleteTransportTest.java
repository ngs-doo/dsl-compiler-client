package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TemplateDeleteTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateDeleteRequestInvalidName() throws IOException {
        final HttpRequest templateDeleteRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final String templateName = " templateName ";
            templateDeleteRequest = httpRequestBuilder.templateDelete(token, templateName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateDeleteRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testTemplateDeleteRequestNameAbsent() throws IOException {
        final HttpRequest templateDeleteRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final String templateName = "";
            templateDeleteRequest = httpRequestBuilder.templateDelete(token, templateName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateDeleteRequest);
        assertEquals(201, response.code);
    }
}
