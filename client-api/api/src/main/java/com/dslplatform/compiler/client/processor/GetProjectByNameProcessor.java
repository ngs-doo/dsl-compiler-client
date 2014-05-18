package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.Project;
import com.dslplatform.compiler.client.api.model.json.ProjectJsonDeserialization;
import com.dslplatform.compiler.client.response.GetProjectByNameResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GetProjectByNameProcessor {
    public GetProjectByNameResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;

        final Project project = authSuccess ? readProject(httpResponse.body) : null;

        return new GetProjectByNameResponse(authSuccess, authResponseMessage, project);
    }

    private Project readProject(final byte [] responseBody) {
        JsonReader jsonReader =
                new JsonReader(new InputStreamReader(new ByteArrayInputStream(responseBody), Charset.forName("UTF-8")));
        try {
            return ProjectJsonDeserialization.fromJsonObject(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
