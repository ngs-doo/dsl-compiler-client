package com.dslplatform.compiler.client.params;

public class Username implements Param {
    public final String username;

    public Username(
            final String username) {
        this.username = username;
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
