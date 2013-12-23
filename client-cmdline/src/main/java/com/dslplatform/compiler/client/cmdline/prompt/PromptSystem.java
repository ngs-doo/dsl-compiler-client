package com.dslplatform.compiler.client.cmdline.prompt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class PromptSystem implements Prompt {
    private final Output output;

    public PromptSystem(
            final Output output) {
        this.output = output;
    }

    @Override
    public boolean isAvailable() {
        return true;
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
        return readBuffered(mask);
    }

    protected String readBuffered(final Character mask) {
        final BufferedReader br = new BufferedReader(new InputStreamReader(
                System.in));
        try {
            return br.readLine();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
