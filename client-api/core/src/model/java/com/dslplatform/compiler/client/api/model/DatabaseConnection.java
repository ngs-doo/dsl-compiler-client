package com.dslplatform.compiler.client.api.model;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseConnection that = (DatabaseConnection) o;

        if (port != that.port) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!Arrays.equals(password, that.password)) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
