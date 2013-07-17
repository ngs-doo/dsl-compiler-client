package com.dslplatform.compiler.client.cmdline.prompt;

import java.io.Console;

public class PromptConsole extends PromptReader {

    final Console console = System.console();
    @Override
    public String readLine(final String message, final Character mask) {
        System.out.print(message);
        return
                ( console == null ) ?
                    super.readBuffered(mask) :
                        mask != null ?
                            new String(console.readPassword()) :
                            console.readLine();
    }
}
