package com.dslplatform.compiler.client.api.core.mock.processor;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InspectManagedProjectChangesProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.PUT && request.path.startsWith("Alpha.svc/changes/");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        int state = success;

        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        final String requestbody = new String(request.body, "UTF-8");
        if (requestbody.contains("bad.dsl")) state = bad_dsl;

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("application/json"));
                body = "[{\"Type\":\"Create\",\"Definition\":\"A-C-i\",\"Description\":\"New property i will be created for C in A\"}]".getBytes("UTF-8");
                break;
            case bad_dsl:
                code = 400;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = "[{\"Type\":\"Create\",\"Definition\":\"A-C-i\",\"Description\":\"New property i will be created for C in A\"}]".getBytes("UTF-8");
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
