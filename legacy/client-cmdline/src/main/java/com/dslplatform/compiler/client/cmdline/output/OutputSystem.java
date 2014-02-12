package com.dslplatform.compiler.client.cmdline.output;

import com.dslplatform.compiler.client.io.Output;

public class OutputSystem implements Output {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void print(final String message) {
        if (isAvailable()) {
            System.out.print(message);
        }
    }

    @Override
    public void println(final String message) {
        if (isAvailable()) {
            System.out.println(message);
        }
    }
}
