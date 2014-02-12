package com.dslplatform.compiler.client.cmdline.prompt;

import java.io.Console;

import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class PromptConsole implements Prompt {
    private final Output output;
    private final Console console;

    public PromptConsole(
            final Output output) {
        this.output = output;
        console = System.console();
    }

    @Override
    public boolean isAvailable() {
        return console != null;
    }

    @Override
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

    @Override
    public String readLine(final String message, final Character mask) {
        output.print(message);
        return mask != null ? new String(console.readPassword()) : console
                .readLine();
    }
}
