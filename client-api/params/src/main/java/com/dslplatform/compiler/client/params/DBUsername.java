package com.dslplatform.compiler.client.params;

public class DBUsername implements Param {
    public final String dbUsername;

    public DBUsername(
            final String dbUsername) {
        this.dbUsername = dbUsername;
        // TODO: see if this needs to be validated for whitespaces, etc.
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBUsername) || that == null) return false;

        final DBUsername thatDBUsername = (DBUsername) that;
        return dbUsername.equals(thatDBUsername.dbUsername);
    }

    @Override
    public int hashCode() {
        return dbUsername.hashCode();
    }

    @Override
    public String toString() {
        return "DBUsername(" + dbUsername + ")";
    }
}
