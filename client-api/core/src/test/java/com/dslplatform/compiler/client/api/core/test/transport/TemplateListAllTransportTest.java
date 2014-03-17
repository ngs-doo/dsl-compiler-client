package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;


import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TemplateListAllTransportTest extends HttpTransportImplTest {

    @Test
    public void testTemplateListAllRequest() throws IOException {
        final HttpRequest templateListAllRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            final String projectName = "!";
            templateListAllRequest = httpRequestBuilder.templateListAll(token, UUID.fromString(validID));
        }

        final HttpResponse response = httpTransport.sendRequest(templateListAllRequest);
        assertEquals(201, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
    }

    @Test
    public void testTemplateListAllRequestWringProject() throws IOException {
        final HttpRequest templateListAllRequest; {
            final String token = projectToken(validUser, validPassword, validID);
            templateListAllRequest = httpRequestBuilder.templateListAll(token, UUID.fromString(inValidID));
        }

        final HttpResponse response = httpTransport.sendRequest(templateListAllRequest);
        assertEquals(201, response.code);
    }
}
