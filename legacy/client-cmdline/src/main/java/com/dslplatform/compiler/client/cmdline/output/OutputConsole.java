package com.dslplatform.compiler.client.cmdline.output;

import java.io.Console;

import com.dslplatform.compiler.client.io.Output;

public class OutputConsole implements Output {
    private final Console console;

    public OutputConsole() {
        console = System.console();
    }

    @Override
    public boolean isAvailable() {
        return console != null;
    }

    @Override
    public void print(final String message) {
        if (isAvailable()) {
            console.writer().print(message);
            console.flush();
        }
    }

    @Override
    public void println(final String message) {
        if (isAvailable()) {
            console.writer().println(message);
            console.flush();
        }
    }
}
