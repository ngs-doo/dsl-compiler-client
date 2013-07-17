package com.dslplatform.compiler.client.cmdline.prompt;

public interface Prompt {
    public char readCharacter(final String message, final String allowed);
    public String readLine(final String message, final Character mask);
}
