package com.dslplatform.compiler.client.api.core.mock.processor;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;

import java.io.IOException;
import java.util.*;

public class GetProjectByNameProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.PUT && request.path.startsWith("Domain.svc/search/Client.Project");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {
        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        int state = success;

        final byte[] mockProject = "{\"ID\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-04-01T13:13:57.480884+02:00\",\"DatabaseServer\":\"someExternalPlace\",\"DatabasePort\":5432,\"DatabaseName\":\"DatabaseNameValue\",\"ApplicationServer\":\"test.dsl-platform.com\",\"ApplicationName\":\"ApplicationNameValue0\",\"ApplicationPoolName\":\"ApplicationPoolName0\",\"Nick\":\"BrownBison\",\"URI\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserURI\":\"user@domain.omm\"}".getBytes("UTF-8");

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("application/json"));
                body = mockProject;
                break;
            default:
                code = 400;
                body = "".getBytes(ENCODING);
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
        }

        headers.put("Content-Length", Arrays.asList(String.valueOf(body.length)));
        return new HttpResponse(code, headers, body);
    }
}
