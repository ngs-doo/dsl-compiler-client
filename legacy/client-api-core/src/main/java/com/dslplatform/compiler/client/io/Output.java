package com.dslplatform.compiler.client.io;

public interface Output {
    public boolean isAvailable();

    public void print(final String message);

    public void println(final String message);
}
