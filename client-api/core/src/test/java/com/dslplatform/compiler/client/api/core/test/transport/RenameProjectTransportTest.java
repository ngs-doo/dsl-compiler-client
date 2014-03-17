package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RenameProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testRenameProjectRequestInvalidName() throws IOException {
        final HttpRequest renameRequest;
        {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "nick";
            final String newName = "";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse response = httpTransport.sendRequest(renameRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project name not provided.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testRenameProjectRequestNotFound() throws IOException {
        final HttpRequest renameRequest;
        {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "?";
            final String newName = "!";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse response = httpTransport.sendRequest(renameRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testRenameProjectRequest() throws IOException {
        final HttpRequest renameRequest;
        {
            final String token = "Basic " + DatatypeConverter.printBase64Binary("ocd@dsl-platform.com:xxx".getBytes(ENCODING));
            final String oldName = "nick";
            final String newName = "GreenLion1";
            renameRequest = httpRequestBuilder.renameProject(token, oldName, newName);
        }

        final HttpResponse response = httpTransport.sendRequest(renameRequest);
        assertEquals(201, response.code);
    }
}
