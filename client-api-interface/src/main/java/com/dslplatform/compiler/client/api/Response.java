package com.dslplatform.compiler.client.api;

import java.io.IOException;

import com.dslplatform.compiler.client.api.transport.Message;

class Response {
    public final boolean ok;
    public final int code;
    public final byte[] body;

    public Response(
            final boolean ok,
            final int code,
            final byte[] body) {
        this.ok = ok;
        this.code = code;
        this.body = body;
    }

    public Message[] toMessages() throws IOException {
        return JavaSerialization.deserialize(body, Message[].class);
    }
}
