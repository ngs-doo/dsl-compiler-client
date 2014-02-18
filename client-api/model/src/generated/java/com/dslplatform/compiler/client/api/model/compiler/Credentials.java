package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class Credentials
        implements
        java.io.Serializable,
        com.dslplatform.compiler.client.api.model.compiler.Auth<com.dslplatform.compiler.client.api.model.compiler.Credentials> {
    public Credentials(
            final String user,
            final byte[] password) {
        setUser(user);
        setPassword(password);
    }

    public Credentials() {
        this.user = "";
        this.password = new byte[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 2015805848;
        result = prime * result
                + (this.user != null ? this.user.hashCode() : 0);
        result = prime * result + (java.util.Arrays.hashCode(this.password));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof Credentials)) return false;
        final Credentials other = (Credentials) obj;

        if (!(this.user.equals(other.user))) return false;
        if (!(java.util.Arrays.equals(this.password, other.password)))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "Credentials(" + user + ',' + password + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String user;

    @JsonProperty("user")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getUser() {
        return user;
    }

    public Credentials setUser(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"user\" cannot be null!");
        this.user = value;

        return this;
    }

    private byte[] password;

    @JsonProperty("password")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public byte[] getPassword() {
        return password;
    }

    public Credentials setPassword(final byte[] value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"password\" cannot be null!");
        this.password = value;

        return this;
    }
}
