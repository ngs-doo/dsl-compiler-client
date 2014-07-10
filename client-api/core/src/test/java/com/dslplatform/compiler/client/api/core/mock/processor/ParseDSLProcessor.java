package com.dslplatform.compiler.client.api.core.mock.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.json.JsonReader;

public class ParseDSLProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.PUT && request.path.equals("Platform.svc/parse");
    }

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {

        final Map<String, Object> dsl;
        {
            final JsonReader jr =
                    new JsonReader(new InputStreamReader(new ByteArrayInputStream(request.body), ENCODING));
            dsl = jr.readMap();
        }

        int state = success;

        for (final Object current : dsl.values()) {
            if (((String) current).contains("!")) {
                state = name_invalid;
                break;
            }
        }

        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = new byte[0];
                break;
            case name_invalid:
                code = 400;
                body = "Parse error - encountered an exclamation mark!".getBytes(ENCODING);
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
