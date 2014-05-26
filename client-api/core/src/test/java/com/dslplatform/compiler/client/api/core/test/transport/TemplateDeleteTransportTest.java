package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TemplateDeleteTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateDeleteRequest() throws IOException {
        final HttpRequest templateDeleteRequest; {;
            final String templateName = "templateName";
            templateDeleteRequest = httpRequestBuilder.templateDelete(auth, validId, templateName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateDeleteRequest);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
    }

    @Test
    public void testTemplateDeleteRequestNameAbsent() throws IOException {
        final HttpRequest templateDeleteRequest; {
            final String templateName = "";
            templateDeleteRequest = httpRequestBuilder.templateDelete(auth, validId, templateName);
        }

        final HttpResponse response = httpTransport.sendRequest(templateDeleteRequest);
        assertEquals(201, response.code);
    }
}
