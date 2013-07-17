package com.dslplatform.compiler.client.cmdline.output;

public class OutputConsole implements Output {
    @Override
    public void println(final String message) {
        System.out.println(message);
    }
}
