package com.dslplatform.compiler.client.params;

public class Username implements Param {
    public final String username;

    public Username(
            final String username) {
        this.username = username;
        // TODO: see if this needs to be validated for whitespaces, etc.
        // TODO: do the same for Password, and other recently added single string params
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Username) || that == null) return false;

        final Username thatUsername = (Username) that;
        return username.equals(thatUsername.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        return "Username(" + username + ")";
    }
}
