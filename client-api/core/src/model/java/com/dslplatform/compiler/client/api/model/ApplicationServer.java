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
}
