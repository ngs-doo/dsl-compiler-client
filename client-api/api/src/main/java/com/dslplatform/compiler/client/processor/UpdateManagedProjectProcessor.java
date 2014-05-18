package com.dslplatform.compiler.client.processor;

import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.response.Source;
import com.dslplatform.compiler.client.response.UpdateManagedProjectResponse;

import java.nio.charset.Charset;
import java.util.List;

public class UpdateManagedProjectProcessor {
    public UpdateManagedProjectResponse process(final HttpResponse httpResponse) {
        final boolean authSuccess = httpResponse.code != 403;
        final String httpResponseString = new String(httpResponse.body, Charset.forName("UTF-8"));
        final String authResponseMessage = authSuccess ? null : httpResponseString;
        boolean updateSuccess = httpResponse.code == 201;
        final List<Source> sources = updateSuccess ? FromJson.orderedSources(httpResponse.body) : null;

        return new UpdateManagedProjectResponse(authSuccess, authResponseMessage, updateSuccess, sources);
    }
}
