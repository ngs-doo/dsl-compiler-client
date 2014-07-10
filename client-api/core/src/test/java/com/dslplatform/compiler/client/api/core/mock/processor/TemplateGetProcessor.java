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

public class TemplateGetProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.GET && request.path.startsWith("Platform.svc/template/");
    }

    final int template_name_missing = 101;
    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        int state = success;
        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("application/json"));
                body = MockData.template_1;
                break;
            case template_name_missing:
                code = 400;
                headers.put("Content-Type", Arrays.asList("text/plain"));
                body = "Missing template name".getBytes(ENCODING);
                break;
            default:
                code = 400;
                body = "".getBytes(ENCODING);
                headers.put("Content-Type", Arrays.asList("text/plain"));
        }

        headers.put("Content-Length", Arrays.asList(String.valueOf(body.length)));
        return new HttpResponse(code, headers, body);
    }
}
