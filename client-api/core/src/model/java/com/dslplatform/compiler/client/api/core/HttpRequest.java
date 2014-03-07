package com.dslplatform.compiler.client.api.core;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dslplatform.compiler.client.api.core.impl.JsonWriter;

public final class HttpRequest {
    public static enum Method { GET, POST, PUT }

    public final Method method;
    public final String path;
    public final Map<String, List<String>> headers;
    public final byte[] body;

    private HttpRequest(final Method method, final String path, final byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = new LinkedHashMap<>();
        this.body = body;
    }

    public static HttpRequest GET(final String path) {
        return new HttpRequest(Method.GET, path, null);
    }

    public static HttpRequest POST(final String path, final Object body) {
        return new HttpRequest(Method.POST, path, serialize(body));
    }

    public static HttpRequest PUT(final String path, final Object body) {
        return new HttpRequest(Method.PUT, path, serialize(body));
    }

    @SuppressWarnings("unchecked")
    private static byte[] serialize(final Object body) {
        final JsonWriter jb = new JsonWriter();
        if (body instanceof Map) {
            jb.write((Map<String, String>) body);
        }
        return jb.toString().getBytes(Charset.forName("UTF-8"));
    }
}
