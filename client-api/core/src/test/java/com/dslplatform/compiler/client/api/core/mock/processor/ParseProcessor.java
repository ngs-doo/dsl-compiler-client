package com.dslplatform.compiler.client.api.core.mock.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpRequest.Method;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.impl.JsonReader;

public class ParseProcessor implements MockProcessor {
    @Override
    public boolean isDefinedAt(final HttpRequest request) {
        return request.method == Method.PUT && request.path.equals("Alpha.svc/parse");
    }

    private static final Charset ENCODING = Charset.forName("UTF-8");

    @Override
    public HttpResponse apply(final HttpRequest request) throws IOException {
        final Map<String, String> dsl;
        {
            final JsonReader jr =
                    new JsonReader(new InputStreamReader(new ByteArrayInputStream(request.body), ENCODING));
            dsl = jr.readMap();
        }

        final int code;
        final byte[] body;

        boolean success = true;

        for (final String current : dsl.values()) {
            if (current.contains("!")) {
                success = false;
                break;
            }
        }

        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        if (success) {
            code = 200;
            body = new byte[0];
        } else {
            code = 400;
            body = "Parse error - encountered an exclamation mark!".getBytes(ENCODING);
            headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
        }

        headers.put("Content-Length", Arrays.asList(String.valueOf(body.length)));
        return new HttpResponse(code, headers, body);
    }
}
