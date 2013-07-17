/*
package com.dslplatform.compiler.client.cmdline.prompt;

import jline.console.ConsoleReader;

public class PromptJLine implements Prompt {
    @Override
    public char readCharacter(final String message, final String allowed) {
        try {
            System.out.print(message);
            final ConsoleReader console = new ConsoleReader();
            return (char) console.readCharacter(allowed.toCharArray());
        }
        catch (final Throwable t) {
            t.printStackTrace();
            throw new IllegalArgumentException(t);
        }
    }

    @Override
    public String readLine(final String message, final Character mask) {
        try {
            final ConsoleReader console = new ConsoleReader();
            return mask != null
                    ? console.readLine(message, mask)
                    : console.readLine(message);
        }
        catch (final Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }
}
*/
