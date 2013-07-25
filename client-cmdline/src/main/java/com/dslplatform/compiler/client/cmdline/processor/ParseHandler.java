package com.dslplatform.compiler.client.cmdline.processor;

import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.DSL;
import com.dslplatform.compiler.client.api.processors.ParseProcessor;
import com.dslplatform.compiler.client.cmdline.params.AuthProvider;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;
import com.dslplatform.compiler.client.io.Logger;

public class ParseHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;
    private final Actions actions;

    public ParseHandler(
            final Logger logger,
            final Prompt prompt,
            final Output output,
            final Login login,
            final Actions actions) {
        this.logger = logger;
        this.prompt = prompt;
        this.output = output;
        this.login = login;
        this.actions = actions;
    }

    public void apply(final Arguments arguments) throws IOException {
        final DSL dsl = arguments.getDsl();

        final AuthProvider authProvider = new AuthProvider(logger, prompt, login, arguments);
        final ParseProcessor pp = actions.parse(authProvider.getAuth(), dsl);

        if (pp.isAuthorized()) {
            authProvider.setToken(pp.getAuthorization());
        }
        else if (authProvider.isToken()) {
            authProvider.removeToken();
            apply(arguments);
            return;
        }

        output.println(pp.getResponse());
    }
}
