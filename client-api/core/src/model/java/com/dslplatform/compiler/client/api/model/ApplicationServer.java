package com.dslplatform.compiler.client.api.model;

public class ApplicationServer {
    public final String host;
    public final String name;
    public final String pool;

    public ApplicationServer(
            final String host,
            final String name,
            final String pool) {
        this.host = host;
        this.name = name;
        this.pool = pool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationServer that = (ApplicationServer) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (pool != null ? !pool.equals(that.pool) : that.pool != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
