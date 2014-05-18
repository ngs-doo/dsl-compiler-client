package com.dslplatform.compiler.client.response;

public class Source {
    public final String language;
    public final String path;
    public final byte[] content;

    public Source(String language, String path, byte [] content) {
        this.language = language;
        this.path = path;
        this.content = content;
    }
}
