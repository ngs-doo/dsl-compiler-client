package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.model.Change;
import com.dslplatform.compiler.client.api.model.json.SchemaChangeJsonDeserialization;
import com.dslplatform.compiler.client.response.InspectManagedProjectChangesResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

public class InspectManagedProjectChangesProcessor {
    public InspectManagedProjectChangesResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;
        final boolean parseSuccess = httpResponse.code == 200;
        final List<Change> schemaChanges = parseSuccess ? readChanges(httpResponse.body) : null;

        return new InspectManagedProjectChangesResponse(authSuccess, authResponseMessage, schemaChanges);
    }

    private List<Change> readChanges(final byte[] responseBody) {
        JsonReader jsonReader =
                new JsonReader(new InputStreamReader(new ByteArrayInputStream(responseBody), Charset.forName("UTF-8")));
        try {
            return SchemaChangeJsonDeserialization.fromJsonArray(jsonReader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
