package com.dslplatform.compiler.client.params;

public class Password implements Param {
    public final String password;

    public Password(
            final String password) {
        this.password = password;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Password) || that == null) return false;

        final Password thatPassword = (Password) that;
        return password.equals(thatPassword.password);
    }

    @Override
    public int hashCode() {
        return password.hashCode();
    }

    @Override
    public String toString() {
        return "Password(" + password + ")";
    }
}
