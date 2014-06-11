package com.dslplatform.compiler.client.params;

public class DBConnectionString implements Param {
    public final String dbConnectionString;

    public DBConnectionString(
            final String dbConnectionString) {
        this.dbConnectionString = dbConnectionString;
        // TODO: see if this needs to be validated for whitespaces, etc.
        // TODO: do the same for Password, and other recently added single string params
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBConnectionString) || that == null) return false;

        final DBConnectionString thatDBConnectionString = (DBConnectionString) that;
        return dbConnectionString.equals(thatDBConnectionString.dbConnectionString);
    }

    @Override
    public int hashCode() {
        return dbConnectionString.hashCode();
    }

    @Override
    public String toString() {
        return "DBConnectionString(" + dbConnectionString + ")";
    }
}
