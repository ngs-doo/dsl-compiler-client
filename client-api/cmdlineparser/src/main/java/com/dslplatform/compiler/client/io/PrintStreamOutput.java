package com.dslplatform.compiler.client.io;

import java.io.PrintStream;

public class PrintStreamOutput implements Output {
    private final PrintStream printStream;

    public PrintStreamOutput() {
        this(System.out);
    }

    public PrintStreamOutput(
            final PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void print(final String message) {
        printStream.print(message);
    }

    @Override
    public void println(final String message) {
        printStream.println(message);
    }
}
