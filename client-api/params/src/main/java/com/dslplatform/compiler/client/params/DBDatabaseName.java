package com.dslplatform.compiler.client.params;

public class DBDatabaseName implements Param {
    public final String dbDatabaseName;

    public DBDatabaseName(
            final String dbDatabaseName) {
        this.dbDatabaseName = dbDatabaseName;
        // TODO: see if this needs to be validated for whitespaces, etc.
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBDatabaseName) || that == null) return false;

        final DBDatabaseName thatDBDatabaseName = (DBDatabaseName) that;
        return dbDatabaseName.equals(thatDBDatabaseName.dbDatabaseName);
    }

    @Override
    public int hashCode() {
        return dbDatabaseName.hashCode();
    }

    @Override
    public String toString() {
        return "DBDatabaseName(" + dbDatabaseName + ")";
    }
}
