package com.dslplatform.compiler.client.cmdline;

public interface CommandLinePrompt {
    public boolean promptRetry(String what);

    public boolean promptContinue(String what);

    public ActionContext.ContinueRetryQuit promptCRQ(String what);

    public char readCharacter(final String message, final String allowed);

    public String readLine(final String message, final Character mask);

    public String readBuffered(final Character mask);
}
