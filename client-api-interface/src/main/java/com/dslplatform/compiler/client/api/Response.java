package com.dslplatform.compiler.client.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.zip.InflaterInputStream;

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

    public boolean isOK() {
        return code == 200;
    }

    public String bodyToString() {
        return new String(body, Charset.forName("UTF-8"));
    }

    public Message[] getMessages() throws IOException {
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(body);
            final InflaterInputStream dis = new InflaterInputStream(bais);
            final ObjectInputStream ois = new ObjectInputStream(dis);
            return (Message[]) ois.readObject();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            throw new IOException("Invalid request received", e);
        }
    }
}
