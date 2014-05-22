package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class GetProjectByNameTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetProjectByNameRequest() throws IOException {
        final HttpRequest getProjectByNameRequest; {
            final String projectName = "RedRhino";
            getProjectByNameRequest = httpRequestBuilder.getProjectByName(token, projectName);
        }

        final HttpResponse response = httpTransport.sendRequest(getProjectByNameRequest);
        logger.info(new String(response.body, "UTF-8"));
        assertEquals(200, response.code);

        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
    }
}
