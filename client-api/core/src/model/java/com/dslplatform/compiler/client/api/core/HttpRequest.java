package com.dslplatform.compiler.client.api.core;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dslplatform.compiler.client.api.json.JsonWriter;

public final class HttpRequest {

    public static enum Method { GET, POST, PUT }

    public final Method method;
    public final String path;
    public final Map<String, List<String>> headers;
    public final Map<String, List<String>> query;
    public final byte[] body;

    private HttpRequest(final Method method, final String path, final byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = new LinkedHashMap<>();
        this.query = new LinkedHashMap<>();
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
            jb.write((Map<String, Object>) body);
        }
        return jb.toString().getBytes(Charset.forName("UTF-8"));
    }

    public String buildQuery() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, List<String>> queryPart : query.entrySet()) {
            if (queryPart.getValue().size() != 0) {
                sb.append((sb.length() == 0) ? "?" : "&").append(queryPart.getKey()).append('=');

                final Iterator<String> valueIterator = queryPart.getValue().iterator();
                sb.append(valueIterator.next());
                while (valueIterator.hasNext()) {
                    sb.append(',').append(valueIterator.next());
                }
            }
        }
        return sb.toString();
    }
}
