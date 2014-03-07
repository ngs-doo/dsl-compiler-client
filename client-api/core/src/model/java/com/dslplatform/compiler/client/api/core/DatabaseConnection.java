package com.dslplatform.compiler.client.api.core;

public final class DatabaseConnection {
    final String host;
    final int port;
    final String dbName;
    final String user;
    final String password;

    public DatabaseConnection(
            final String host,
            final int port,
            final String dbName,
            final String user,
            final String password) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.password = password;
    }
}
