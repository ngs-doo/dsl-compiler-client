package com.dslplatform.compiler.client.params;

public class DBPassword implements Param {
    public final String dbPassword;

    public DBPassword(
            final String dbPassword) {
        this.dbPassword = dbPassword;
        // TODO: see if this needs to be validated for whitespaces, etc.
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBPassword) || that == null) return false;

        final DBPassword thatDBPassword = (DBPassword) that;
        return dbPassword.equals(thatDBPassword.dbPassword);
    }

    @Override
    public int hashCode() {
        return dbPassword.hashCode();
    }

    @Override
    public String toString() {
        return "DBPassword(" + dbPassword + ")";
    }
}
