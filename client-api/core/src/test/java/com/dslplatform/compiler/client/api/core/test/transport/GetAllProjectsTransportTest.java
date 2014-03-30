package com.dslplatform.compiler.client.api.core.test.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.Project;
import com.dslplatform.compiler.client.api.model.json.ProjectJsonDeserialization;

public class GetAllProjectsTransportTest extends HttpTransportImplTest {

    @Test
    public void testGetAllProjectsRequest() throws IOException {
        final HttpRequest getAllProjectsRequest; {
            final String token = userToken(validUser, validPassword);
            getAllProjectsRequest = httpRequestBuilder.getAllProjects(token);
        }

        final HttpResponse response = httpTransport.sendRequest(getAllProjectsRequest);
        assertEquals(200, response.code);
        assertEquals(Arrays.asList("application/json"), response.headers.get("Content-Type"));
        final String responseStr = new String(response.body, ENCODING);

        assertTrue(responseStr.contains("CreatedAt"));
        assertTrue(responseStr.contains("UserURI"));

        final List<Project> projects = ProjectJsonDeserialization.fromJsonArray(new JsonReader(new StringReader(responseStr)));
        assertTrue(projects.size() > 0);
    }

    @Test
    public void testGetAllProjectsRequestInvalidUser() throws IOException {
        final HttpRequest getAllProjectsRequest; {
            final String token = userToken(validUser, inValidPassword);
            getAllProjectsRequest = httpRequestBuilder.getAllProjects(token);
        }

        final HttpResponse response = httpTransport.sendRequest(getAllProjectsRequest);
        assertEquals(403, response.code);
    }
}
