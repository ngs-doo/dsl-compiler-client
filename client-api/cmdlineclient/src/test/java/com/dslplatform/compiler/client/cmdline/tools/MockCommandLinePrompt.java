package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.cmdline.CommandLinePrompt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MockCommandLinePrompt implements CommandLinePrompt {

    private Iterator<Boolean> responses;

    public List<String> whats;

    public MockCommandLinePrompt(Boolean... responses) {
        this.responses = Arrays.asList(responses).iterator();
    }

    public boolean contains(String what) {
        for (String w : whats) if (w.equals(what)) return true;
        return false;
    }

    @Override
    public boolean promptRetry(String what) {
        System.out.println(what);
        return responses.next();
    }

    @Override
    public boolean promptContinue(String what) {
        return responses.next();
    }

    @Override
    public boolean promptMigrationInformation(String migrationInformation, boolean isMigrationDestructive, boolean promptAnyway) {
        return responses.next();
    }

    @Override
    public char readCharacter(String message, String allowed) {
        return 0;
    }

    @Override
    public String readLine(String message, Character mask) {
        return null;
    }

    @Override
    public String readBuffered(Character mask) {
        return null;
    }
}
