package com.dslplatform.compiler.client.api.core.mock.processor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;

public interface MockProcessor {

    public abstract boolean isDefinedAt(final HttpRequest request);
    public abstract HttpResponse apply(final HttpRequest request) throws IOException;

    public static final Charset ENCODING = Charset.forName("UTF-8");

    final static Set<String> supportedLanguages = new HashSet<String>() {{
        add("Java");
        add("Scala");
    }};

    final static Set<String> supportedLanguagesUnmanaged = new HashSet<String>() {{
        add("ScalaServer");
        add("csharp_server"); // not sure about this one
        add("CSharpServer");
        addAll(supportedLanguages);
    }};

    public final static int success             = 0;
    public final static int unknown_language    = 1;
    public final static int name_invalid        = 2;
    public final static int name_missing        = 3;
    public final static int bad_dsl             = 11;

/*
    HttpResponse mockResponse(final int state) {

        final int code;
        final byte[] body;
        final Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();

        switch (state) {
            case success:
                code = 200;
                headers.put("Content-Type", Arrays.asList("text/plain; charset=\"utf-8\""));
                body = new byte[0];
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

        return new HttpResponse(code, headers, body);
    }*/
}
