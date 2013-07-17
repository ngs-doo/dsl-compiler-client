package com.dslplatform.compiler.client.cmdline;

import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.ApiCall;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.logging.Logger.Level;
import com.dslplatform.compiler.client.api.logging.LoggerSystemOut;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.cmdline.login.Login;
import com.dslplatform.compiler.client.cmdline.login.LoginConsole;
import com.dslplatform.compiler.client.cmdline.output.Output;
import com.dslplatform.compiler.client.cmdline.output.OutputConsole;
import com.dslplatform.compiler.client.cmdline.params.ArgumentsParser;
import com.dslplatform.compiler.client.cmdline.processor.DiffHandler;
import com.dslplatform.compiler.client.cmdline.processor.ParseAndDiffHandler;
import com.dslplatform.compiler.client.cmdline.processor.ParseHandler;
import com.dslplatform.compiler.client.cmdline.processor.UpdateHandler;
import com.dslplatform.compiler.client.cmdline.processor.UpdateUnsafeHandler;
import com.dslplatform.compiler.client.cmdline.prompt.Prompt;
import com.dslplatform.compiler.client.cmdline.prompt.PromptConsole;

public class Main {
    public static void main(final String[] args) {
        try {
            final Logger logger = new LoggerSystemOut(Level.NONE);
            final Prompt prompt = new PromptConsole();
            final Output output = new OutputConsole();
            final Login login = new LoginConsole(logger, prompt);

            new Main(logger, prompt, output, login).process(args);
            System.exit(0);
        }
        catch (final Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;

    public Main(
            final Logger logger,
            final Prompt prompt,
            final Output output,
            final Login login) {
        this.logger = logger;
        this.prompt = prompt;
        this.output = output;
        this.login = login;
    }

    public void process(final String[] args) throws IOException {
        final ApiCall apiCall = new ApiCall(logger);
        final Actions actions = new Actions(logger, apiCall);
        final Arguments params = new ArgumentsParser(logger, args);

        switch (params.getAction()) {
            case PARSE:
                new ParseHandler(logger, prompt, output, login, actions).apply(params);
                break;

            case DIFF:
                new DiffHandler(logger, prompt, output, login, actions).apply(params);
                break;

            case PARSE_AND_DIFF:
                new ParseAndDiffHandler(logger, prompt, output, login, actions).apply(params);
                break;

            case UPDATE:
                new UpdateHandler(logger, prompt,  output, login, actions).apply(params);
                break;

            case UPDATE_UNSAFE:
                new UpdateUnsafeHandler(logger, prompt,  output, login, actions).apply(params);
                break;

            default:
                throw new IllegalArgumentException(
                        "The action \"" + params.getAction() + "\" does not exist, or is not supported at the moment.");
        }
    }
}
