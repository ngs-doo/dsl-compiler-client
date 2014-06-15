package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.io.Output;

public class TestingOutput implements Output {

    public StringBuilder acc = new StringBuilder();

    @Override
    public void print(String message) {
        acc.append(message);
    }

    @Override
    public void println(String message) {
        acc.append(message);
    }
}
