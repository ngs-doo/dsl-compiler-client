package com.dslplatform.compiler.client.io;

public interface Prompt {
    public boolean isAvailable();

    public char readCharacter(final String message, final String allowed);

    public String readLine(final String message, final Character mask);
}
