package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.cmdline.ActionContext;
import com.dslplatform.compiler.client.cmdline.CommandLinePrompt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MockCommandLinePrompt implements CommandLinePrompt {

    private Iterator<Boolean> responses;

    public List<String> whats;

    public MockCommandLinePrompt(Boolean... responses) {

        whats = new LinkedList<String>();
        this.responses = Arrays.asList(responses).iterator();
    }

    public boolean contains(String what) {
        for (String w : whats) if (w.equals(what)) return true;
        return false;
    }

    @Override
    public ActionContext.ContinueRetryQuit promptCRQ(String what) {
        return ActionContext.ContinueRetryQuit.Continue;
    }

    @Override
    public boolean promptRetry(String what) {
        whats.add(what);
        return responses.next();
    }

    @Override
    public boolean promptContinue(String what) {
        whats.add(what);
        return responses.next();
    }

    @Override
    public char readCharacter(String message, String allowed) {
        whats.add(message);
        return 'c';
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
