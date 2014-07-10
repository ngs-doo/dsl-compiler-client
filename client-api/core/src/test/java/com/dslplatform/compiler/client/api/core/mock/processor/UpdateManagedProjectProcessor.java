package com.dslplatform.compiler.client.api.core.mock.processor;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateManagedProjectProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.POST && request.path.startsWith("Platform.svc/update/");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        int state = success;

        final int code;
        final byte[] body;

        final List<String> targets = request.query.get("targets");

        if (targets == null) state = unknown_language;
        else if (!supportedLanguages.containsAll(targets)) {
            state = unknown_language;
        }

        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        switch (state) {
            case success:
                code = 201;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                final String requestBody = new String(request.body, "UTF-8");
                int iteration = (requestBody.contains("2.dsl")) ? 2 : 1;
                body = MockData.getBodyFor("/test_managed_AB/ClientSource", targets, iteration);
                break;
            case unknown_language:
                code = 400;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = "Unknown language specified".getBytes(ENCODING);
                break;
            case name_missing:
                code = 400;
                body = "Project name not provided.".getBytes(ENCODING);
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                break;
            case name_invalid:
                code = 403;
                body = "Parse error - will no happen yet!".getBytes(ENCODING);
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
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
