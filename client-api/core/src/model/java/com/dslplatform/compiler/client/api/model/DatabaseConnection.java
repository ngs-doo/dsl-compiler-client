package com.dslplatform.compiler.client.api.model;

public class DatabaseConnection {
    public final String host;
    public final int port;
    public final String name;
    public final String user;
    public final char[] password;

    public DatabaseConnection(
            final String host,
            final int port,
            final String name,
            final String user,
            final char[] password) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
    }
}
