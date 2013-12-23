package com.dslplatform.compiler.client.cmdline.prompt;

import java.io.IOException;

import jline.console.ConsoleReader;

import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class PromptJLine implements Prompt {
    private final Output output;

    public PromptJLine(
            final Output output) {
        this.output = output;
    }

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("jline.console.ConsoleReader");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public char readCharacter(final String message, final String allowed) {
        try {
            output.print(message);
            final ConsoleReader console = new ConsoleReader();
            try {
                return (char) console.readCharacter(allowed.toCharArray());
            } finally {
                console.shutdown();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readLine(final String message, final Character mask) {
        try {
            final ConsoleReader console = new ConsoleReader();
            try {
                return mask != null ? console.readLine(message, mask) : console
                        .readLine(message);
            } finally {
                console.shutdown();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
