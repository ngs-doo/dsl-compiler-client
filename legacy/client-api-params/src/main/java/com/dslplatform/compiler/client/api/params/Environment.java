package com.dslplatform.compiler.client.api.params;

import java.nio.charset.Charset;

public class Environment implements Param {
    public static enum ResponseType {
        TEXT,
        HTML;
    }

    public static enum Newline {
        CR,
        CRLF,
        LF;
    }

    public final ResponseType responseType;
    public final Newline newline;
    public final Charset encoding;

    public Environment(
            final ResponseType responseType,
            final Newline newline,
            final Charset encoding) {

        this.responseType = responseType;
        this.newline = newline;
        this.encoding = encoding;
    }

    public Environment() {
        this(ResponseType.TEXT, Newline.LF, Charset.forName("UTF8"));
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() {
        return false;
    }

    // format: OFF
    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("environment")
            .node("responseType", responseType.name())
            .node("newline", newline.toString())
            .node("encoding", encoding.toString())
        .end();
    }
}
