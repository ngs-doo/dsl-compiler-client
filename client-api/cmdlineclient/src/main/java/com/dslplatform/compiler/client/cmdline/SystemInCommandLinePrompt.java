package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.io.Output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemInCommandLinePrompt implements CommandLinePrompt {

    public SystemInCommandLinePrompt(final Output output) {
        this.output = output;
    }

    private final Output output;

    public boolean promptRetry(final String what) {
        final String msg = String.format("%s (R)etry|(Q)uit? ", what);

        final char ch = readCharacter(msg, "RrQq");

        return String.valueOf(ch).toLowerCase().equals("r");
    }

    public boolean promptContinue(String what) {
        final String msg = String.format("Do you wish to proceed with %s Y|N? ", what);

        final char ch = readCharacter(msg, "YyNn");

        return String.valueOf(ch).toLowerCase().equals("y");
    }

    public ActionContext.ContinueRetryQuit promptCRQ(String what) {
        final String msg = what + "(C|c|Q|q|R|r)";
        switch (readCharacter(msg, "RrCcQq")) {
            case 'R': /* Start again from reloading the dsl. */
            case 'r':
                return ActionContext.ContinueRetryQuit.Retry;
            case 'Q': /* Quit! */
            case 'q':
                return ActionContext.ContinueRetryQuit.Quit;
            default:
                return ActionContext.ContinueRetryQuit.Continue;
        }
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
