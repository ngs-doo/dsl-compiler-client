package com.dslplatform.compiler.client.cmdline;

public interface CommandLinePrompt {
    public boolean promptRetry(String what);

    public boolean promptContinue(String what);

    public boolean promptMigrationInformation(String migrationInformation, boolean isMigrationDestructive, boolean promptAnyway);

    public char readCharacter(final String message, final String allowed);

    public String readLine(final String message, final Character mask);

    public String readBuffered(final Character mask);
}
