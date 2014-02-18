package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class Token
        implements
        java.io.Serializable,
        com.dslplatform.compiler.client.api.model.compiler.Auth<com.dslplatform.compiler.client.api.model.compiler.Token> {
    public Token(
            final byte[] token) {
        setToken(token);
    }

    public Token() {
        this.token = new byte[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 1601847199;
        result = prime * result + (java.util.Arrays.hashCode(this.token));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof Token)) return false;
        final Token other = (Token) obj;

        if (!(java.util.Arrays.equals(this.token, other.token))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "Token(" + token + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private byte[] token;

    @JsonProperty("token")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public byte[] getToken() {
        return token;
    }

    public Token setToken(final byte[] value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"token\" cannot be null!");
        this.token = value;

        return this;
    }
}
