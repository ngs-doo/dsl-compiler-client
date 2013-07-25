package com.dslplatform.compiler.client.api.params;

import com.dslplatform.compiler.client.api.commons.codec.binary.Base64;

public class Token implements Auth {
    public final byte[] token;

    public Token(final byte[] token) {
        this.token = token;
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return false; }

    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("auth")
            .node("token", Base64.encodeBase64String(token))
        .end();
    }
}
