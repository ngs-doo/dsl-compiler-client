/*
package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CreateExternalProjectTransportTest extends HttpTransportImplTest {

    @Test
    public void testCreateExernalProjectRequest() throws IOException {
        final HttpRequest createTestProjectRequest;
        {
            final String token = Tokenizer.tokenHeader(validUser, validPassword);
            final String projectName = "NewProjectName";
            final String serverName = "someServerName";
            final String applicationName = "someApplicationName";
            final Map<String, Object> databaseConnection =
                    new LinkedHashMap<String, Object>();
            databaseConnection.put("Server", "someServer");
            databaseConnection.put("Port", 4);
            databaseConnection.put("Database", "someDatabase");
            databaseConnection.put("Username", "someUsername");
            databaseConnection.put("Password", "somePassword");

            createTestProjectRequest = httpRequestBuilder
                    .createExternalProject(token, projectName,
                            serverName, applicationName,
                            databaseConnection);
        }

        final HttpResponse response =
                httpTransport.sendRequest(createTestProjectRequest);
        assertEquals(400, response.code);
        assertEquals(Arrays.asList("application/json"),
                response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"),
                response.body);
    }

    @Test
    public void testCreateExernalProjectRequestNameAbsent()
            throws IOException {
        final HttpRequest createTestProjectRequest;
        {
            final String token =
                    Tokenizer.tokenHeader(validUser, inValidPassword);
            final String projectName = "";
            final String serverName = "someServerName";
            final String applicationName = "someApplicationName";
            final Map<String, Object> databaseConnection =
                    new LinkedHashMap<String, Object>();
            databaseConnection.put("Server", "someServer");
            databaseConnection.put("Port", 4);
            databaseConnection.put("Database", "someDatabase");
            databaseConnection.put("Username", "someUsername");
            databaseConnection.put("Password", "somePassword");

            createTestProjectRequest = httpRequestBuilder
                    .createExternalProject(token, projectName,
                            serverName, applicationName,
                            databaseConnection);
        }

        final HttpResponse response =
                httpTransport.sendRequest(createTestProjectRequest);
        assertEquals(201, response.code);
    }
}
*/
