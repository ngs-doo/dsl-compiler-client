package com.dslplatform.compiler.client.cmdline.output;

import org.fusesource.jansi.AnsiConsole;

import com.dslplatform.compiler.client.io.Output;

public class OutputJAnsi implements Output {
    private final boolean available;

    public OutputJAnsi() {
        boolean available;

        try {
            Class.forName("org.fusesource.jansi.AnsiConsole");
            available = true;
        } catch (final ClassNotFoundException e) {
            available = false;
        }

        this.available = available;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void print(final String message) {
        if (isAvailable()) {
            AnsiConsole.out.print(message);
            AnsiConsole.out.flush();
        }
    }

    @Override
    public void println(final String message) {
        if (isAvailable()) {
            AnsiConsole.out.println(message);
            AnsiConsole.out.flush();
        }
    }
}
