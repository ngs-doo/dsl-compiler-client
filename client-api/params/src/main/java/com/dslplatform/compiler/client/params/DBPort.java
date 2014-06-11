package com.dslplatform.compiler.client.params;

public class DBPort implements Param {
    public final Integer dbPort;

    public DBPort(final String dbPort) {
        if (dbPort == null) {
            this.dbPort = null;
        } else {
            this.dbPort = Integer.parseInt(dbPort);
        }
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof DBPort) || that == null) return false;

        final DBPort thatDBPort = (DBPort) that;
        return dbPort == thatDBPort.dbPort;
    }

    @Override
    public int hashCode() {
        return dbPort.hashCode();
    }

    @Override
    public String toString() {
        return "DBPort(" + dbPort + ")";
    }
}
