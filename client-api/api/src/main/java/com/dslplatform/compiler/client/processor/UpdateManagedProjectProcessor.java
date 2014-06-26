package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.Source;
import com.dslplatform.compiler.client.response.UpdateManagedProjectResponse;
import org.apache.commons.codec.Charsets;

import java.util.List;

public class UpdateManagedProjectProcessor {
    public UpdateManagedProjectResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        if (authSuccess) {
            return processAuthSuccess(httpResponse);
        } else {
            final String authResponseMessage = new String(httpResponse.body, Charsets.UTF_8);
            return new UpdateManagedProjectResponse(authSuccess, authResponseMessage);
        }
    }

    private UpdateManagedProjectResponse processAuthSuccess(final HttpResponse httpResponse) {
        if (httpResponse.code == 201) {
            final List<Source> sources = FromJson.orderedSources(httpResponse.body);
            return new UpdateManagedProjectResponse(true, null, true, sources);
        } else {
            final String updateFailedMessage = new String(httpResponse.body, Charsets.UTF_8);
            return new UpdateManagedProjectResponse(true, null, false, updateFailedMessage);
        }
    }
}
