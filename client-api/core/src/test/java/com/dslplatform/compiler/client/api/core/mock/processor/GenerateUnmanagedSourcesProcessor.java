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

public class GenerateUnmanagedSourcesProcessor implements MockProcessor {
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
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                final String requestbody = new String(request.body, "UTF-8");
                System.out.println(requestbody);
                if (requestbody.contains("2.dsl"))
                    body = getBodyFor(targets, 2);
                else if (requestbody.contains("1.dsl"))
                    body = getBodyFor(targets, 1);
                else body = "".getBytes();
                break;
            case unknown_language:
                code = 400;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = "Unknown language specified".getBytes(ENCODING);
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
        StringBuilder sb = new StringBuilder("/test_migration_sql_simple/ServerSource_");

        if (targets.contains("CSharpServer")) sb.append("CS_");
        if (targets.contains("ScalaServer")) sb.append("ScalaServer_");
        if (targets.contains("Scala")) sb.append("S_");
        if (targets.contains("Java")) sb.append("J_");

        String source = sb.append(iteration).append(".response").toString();
        return MockData.resourceToBytes(source);
    }
}
