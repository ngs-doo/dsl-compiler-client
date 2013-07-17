package com.dslplatform.compiler.client.cmdline;

import java.io.IOException;
import java.util.Arrays;

import com.dslplatform.compiler.client.api.API;
import com.dslplatform.compiler.client.api.RunningTask;
import com.dslplatform.compiler.client.api.params.Action;
import com.dslplatform.compiler.client.api.params.Param;
import com.dslplatform.compiler.client.cmdline.arguments.ApiArguments;
import com.dslplatform.compiler.client.cmdline.processor.DiffHandler;
import com.dslplatform.compiler.client.cmdline.processor.MessageHandler;
import com.dslplatform.compiler.client.cmdline.processor.ParseAndDiffHandler;
import com.dslplatform.compiler.client.cmdline.processor.ParseHandler;
import com.dslplatform.compiler.client.cmdline.processor.UpdateHandler;
import com.dslplatform.compiler.client.prompt.CLPrompt;

public class ApiCall {
    final private ApiArguments args;
    final private CLPrompt prompt;

    public static ApiCall GetCall(final CLPrompt cl, final String[] args) throws IOException {
        final ApiArguments  pa = new ApiArguments(args, cl);
        return new ApiCall(cl, pa);
    }

    public ApiCall(final CLPrompt prompt,
            final ApiArguments args) throws IOException {
        this.prompt = prompt;
        this.args = args;
    }

    public ApiCall(final CLPrompt prompt,
            final String[] args) throws IOException {
        this.args = new ApiArguments(args, prompt);
        this.prompt = prompt;
        call();
    }

    public void call() throws IOException {
        switch (args.action) {
            case PARSE:
                prompt.info("Parse action selected.\n").nextln();
                process(new ParseHandler(prompt), Action.PARSE).print();
                break;

            case PARSE_AND_DIFF:
                prompt.info("Parse and diff action selected.").nextln();
                process(new ParseAndDiffHandler(prompt),
                        Action.PARSE_AND_DIFF).print();
                break;

            case UPDATE_UNSAFE:
                prompt.info("Update Unsafe action selected.").nextln();
                if (args.skipDiff()) unsafeUpdate();
                else {
                    final ParseAndDiffHandler pad = new ParseAndDiffHandler(prompt);
                    process(pad, Action.PARSE_AND_DIFF).print();
                    if (pad.isParsed) unsafeUpdate();
                }
                break;

            case UPDATE:
                prompt.info("Update action selected.").nextln();
                if (args.skipDiff()) safeUpdate();
                else {
                    final ParseAndDiffHandler pad = new ParseAndDiffHandler(prompt);
                    process(pad, Action.PARSE_AND_DIFF).print();

                    if (pad.needsConfirmation)
                        System.out.println("NEEDS!");
                    else
                        System.out.println("DOESNT NEEDS!");
                    if (pad.isParsed) {
                        if (pad.isNewProject || ! pad.needsConfirmation ||
                                confirmDiff(args.action))
                            safeUpdate();
                        }
                    }
                break;

            case CLEAN:
            case CLONE:
                prompt.info(args.getAction().name() + " action selected.").nextln();
                if (confirmDiff( args.getAction()))
                    process( new MessageHandler(prompt), args.action).print();
                else
                    prompt.info("Aborting!");
                break;

            case DIFF:
                prompt.info("Diff action selected.\n").nextln();
                process(new DiffHandler(prompt), args.action).print();
                break;

        }
    }

    private void safeUpdate() {
        final UpdateHandler uh = new UpdateHandler(args.outputPath, prompt);
        process(uh, Action.UPDATE).print();

        if (uh.needsConfirmation && confirmDestructiveUpdate("")) {
            process(uh, Action.UPDATE_UNSAFE).print();
        }
    }

    private void unsafeUpdate( ) {
        process(new UpdateHandler(args.outputPath, prompt), Action.UPDATE_UNSAFE).print();
    }

    // Prompt user
    // ------------
    private boolean confirmDiff(final Action action){
        final String ask = prompt.prompt(
            "[C]onfirm/[A]bort " + action.name(), false);
        return ask.equalsIgnoreCase("c");
    }

    private boolean confirmDestructiveUpdate(final String info){
        final String ask = prompt.prompt(info + " [Yes/No] ", false);
        return ask.equalsIgnoreCase("y");
    }

    // Process Call
    // ------------------
    private MessageHandler process(final MessageHandler mh, final Action action) {
        try{
            RunningTask rt;
            rt = call(action);
            rt.processMessages(mh);

            if (mh.error & DataUtil.writeToken(args.getProjectID().value, rt.token))
                    prompt.info("Token saved!").nextln();
                else
                    prompt.info("There was an error saving token!").nextln();
        } catch (final IOException ioe) {
            prompt.nextln().info("Something unexpected happende while " + action.name() + ": " + ioe.getMessage()).nextln();
            mh.setError();
        }
        return mh;
    }

    // Call Api
    // ---------
    private RunningTask call(final Action action) throws IOException {
        final int projectIDLength = action == Action.PARSE ? 0 : 1;
        final Param[] params = Arrays.copyOf(args.languages, args.languages.length + 1 + projectIDLength, Param[].class);
        params[args.languages.length ] = args.namespace;
        if (args.projectID != null) params[params.length - 1] = args.projectID;
        return API.call(args.action, args.auth, args.dsl, params);
    }
}
