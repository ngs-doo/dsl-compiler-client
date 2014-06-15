package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.io.Output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemInCommandLinePrompt implements CommandLinePrompt {

    public SystemInCommandLinePrompt(Output output) {
        this.output = output;
    }

    private final Output output;

    public boolean promptRetry(String what) {
        String msg = what + " (R)etry/(Q)uit?";

        char ch = readCharacter(msg, "RrQq");

        return String.valueOf(ch).toLowerCase().equals("r");
    }

    public boolean promptContinue(String what) {
        String msg = "Do you wish to precede with " + what + " Y/N?";

        char ch = readCharacter(msg, "YyNn");

        return String.valueOf(ch).toLowerCase().equals("y");
    }

    public boolean promptMigrationInformation(String migrationInformation, boolean isMigrationDestructive, boolean promptAnyway) {
        return false;
    }

    public char readCharacter(final String message, final String allowed) {
        while (true) {
            final String line = readLine(message, null).trim();
            if (!line.isEmpty()) {
                final char ch = line.charAt(0);
                if (allowed.indexOf(ch) != -1) {
                    return ch;
                }
            }
        }
    }

    public String readLine(final String message, final Character mask) {
        output.print(message);
        return readBuffered(mask);
    }

    public String readBuffered(final Character mask) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return br.readLine();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
