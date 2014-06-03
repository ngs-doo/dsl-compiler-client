package com.dslplatform.compiler.client.api.core.mock.processor;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import org.apache.commons.codec.Charsets;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetLastManagedDSLProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.GET && request.path.startsWith("Alpha.svc/dsl/");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        int state = success;

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("application/json"));
                body = ("{\"AB.dsl\":\"" + MockData.resourceToString("/test_managed_AB/AB_1.dsl") + "\"}").getBytes(Charsets.UTF_8);
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
