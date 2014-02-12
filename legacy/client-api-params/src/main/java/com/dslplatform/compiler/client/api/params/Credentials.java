package com.dslplatform.compiler.client.api.params;

import java.nio.charset.Charset;

import com.dslplatform.compiler.client.api.commons.codec.binary.Base64;

public class Credentials implements Auth {
    public final String user;
    public final byte[] password;

    public Credentials(
            final String user,
            final String password) {
        this.user = user;
        this.password = password.getBytes(Charset.forName("UTF-8"));
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() {
        return false;
    }

    // format: OFF
    @Override
    public void addToPayload(final XMLOut xO) {
        xO.start("auth")
            .start("credentials")
                .node("user", user)
                .node("password", Base64.encodeBase64String(password))
            .end()
        .end();
    }
}
