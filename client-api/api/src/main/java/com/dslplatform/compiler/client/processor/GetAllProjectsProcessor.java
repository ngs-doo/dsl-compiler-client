package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.Project;
import com.dslplatform.compiler.client.api.model.json.ProjectJsonDeserialization;
import com.dslplatform.compiler.client.response.GetAllProjectsResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

public class GetAllProjectsProcessor {
    public GetAllProjectsResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        final List<Project> projects = authSuccess ? readProjects(httpResponse.body) : null;

        return new GetAllProjectsResponse(authSuccess, authResponseMessage, projects);
    }

    private List<Project> readProjects(final byte[] responseBody){
        JsonReader jsonReader =
                new JsonReader(new InputStreamReader(new ByteArrayInputStream(responseBody), Charset.forName("UTF-8")));
        try {
            return ProjectJsonDeserialization.fromJsonArray(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
