package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DownloadGeneratedModelTransportTest extends HttpTransportImplTest {

    @Test
    public void testDownloadGeneratedModelTransport() throws IOException {
        final HttpRequest generateSourcesRequest; {
            final String token = projectToken(validUser, validPassword, validId);
            generateSourcesRequest = httpRequestBuilder.downloadGeneratedModel(token, java.util.UUID.fromString(validId));
        }

        final HttpResponse response = httpTransport.sendRequest(generateSourcesRequest);
        assertEquals(200, response.code);
        assertEquals(java.util.Arrays.asList("application/octet-stream"), response.headers.get("Content-Type"));
    }
}
