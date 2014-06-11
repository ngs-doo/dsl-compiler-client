package com.dslplatform.compiler.client.params;

public class DBHost implements Param {
    public final String dbHost;

    public DBHost(
            final String dbHost) {
        this.dbHost = dbHost;
        // TODO: see if this needs to be validated for whitespaces, etc.
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBHost) || that == null) return false;

        final DBHost thatDBHost = (DBHost) that;
        return dbHost.equals(thatDBHost.dbHost);
    }

    @Override
    public int hashCode() {
        return dbHost.hashCode();
    }

    @Override
    public String toString() {
        return "DBHost(" + dbHost + ")";
    }
}
