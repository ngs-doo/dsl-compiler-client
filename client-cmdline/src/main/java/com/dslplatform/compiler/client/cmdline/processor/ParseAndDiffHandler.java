package com.dslplatform.compiler.client.cmdline.processor;

import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.DSL;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.api.processors.ParseAndDiffProcessor;
import com.dslplatform.compiler.client.cmdline.params.AuthProvider;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;
import com.dslplatform.compiler.client.io.Logger;

public class ParseAndDiffHandler extends BaseHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;
    private final Actions actions;

    public ParseAndDiffHandler(
            final Logger logger,
            final Prompt prompt,
            final Output output,
            final Login login,
            final Actions actions) {
        super(logger, prompt);
        this.logger = logger;
        this.prompt = prompt;
        this.output = output;
        this.login = login;
        this.actions = actions;
    }

    public void apply(final Arguments arguments) throws IOException {
        final DSL dsl = arguments.getDsl();
        final ProjectID projectID = arguments.getProjectID();

        final AuthProvider authProvider = new AuthProvider(logger, prompt, login, arguments);
        final ParseAndDiffProcessor pdp = actions.parseAndDiff(authProvider.getAuth(), dsl, projectID);

        if (pdp.isAuthorized()) {
            authProvider.setToken(pdp.getAuthorization());
        }
        else if (authProvider.isToken()) {
            authProvider.removeToken();
            apply(arguments);
            return;
        }

        for(final String diff : pdp.getDiffs()) {
            output.println(diff);
        }

        output.println(pdp.getResponse());
    }
}
