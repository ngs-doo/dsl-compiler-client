/**
 * Copyright (C) 2013 Nova Generacija Softvera d.o.o. (HR), <https://dsl-platform.com/>
 */
package com.dslplatform.compiler.client.api.params;

import org.apache.commons.codec.binary.Base64;

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
