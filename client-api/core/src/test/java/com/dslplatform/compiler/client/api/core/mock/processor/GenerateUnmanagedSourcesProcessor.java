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

public class GenerateUnmanagedSourcesProcessor extends TestProcesorContext implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.PUT && request.path.startsWith("Alpha.svc/unmanaged/source");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        final int code;
        final byte[] body;

        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        int state = success;
        final List<String> targets = request.query.get("targets");
        if (!supportedLanguagesUnmanaged.containsAll(targets)) {
            state = unknown_language;
        }

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("application/json"));
                final String requestBody = new String(request.body, "UTF-8");
                if (requestBody.contains("2.dsl"))
                    body = getBodyFor(targets, 2);
                else if (requestBody.contains("1.dsl"))
                    body = getBodyFor(targets, 1);
                else body = "".getBytes();
                break;
            case unknown_language:
                code = 400;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = ("Unknown language specified " + mkString(targets)).getBytes(ENCODING);
                break;
            default:
                code = 400;
                body = "".getBytes(ENCODING);
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
        }

        headers.put("Content-Length", Arrays.asList(String.valueOf(body.length)));
        return new HttpResponse(code, headers, body);
    }

    private byte[] getBodyFor(List<String> targets, int iteration) {
        return MockData.getBodyFor("/test_migration_sql_simple/ServerSource", targets, iteration);
    }
}
