package com.dslplatform.compiler.client.cmdline.processor;

import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.ProjectName;
import com.dslplatform.compiler.client.api.processors.CreateProcessor;
import com.dslplatform.compiler.client.cmdline.params.AuthProvider;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class CreateHandler extends BaseHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;
    private final Actions actions;

    public CreateHandler(
            final Logger logger,
            final Prompt prompt,
            final Output output,
            final Login login,
            final Actions actions) {
        super(logger, prompt, output);
        this.logger = logger;
        this.prompt = prompt;
        this.output = output;
        this.login = login;
        this.actions = actions;
    }

    public void apply(final Arguments arguments) throws IOException {
        final ProjectName projectName = arguments.getProjectName();

        if (projectName.projectName == null) {
            logger.info("Create request for project name: "
                    + projectName.projectName);
        } else {
            logger.info("Create request (project name will be generated)");
        }

        final AuthProvider authProvider = new AuthProvider(logger, prompt,
                login, arguments);

        final CreateProcessor cp = actions.create(authProvider.getAuth(),
                projectName);

        if (cp.isAuthorized()) {
            authProvider.setToken(cp.getProjectID(), cp.getAuthorization());
        } else if (authProvider.isToken()) {
            authProvider.removeToken();
            apply(arguments);
            return;
        }

        if (cp.isSuccessful()) {
            updateProjectIni(arguments.getProjectIniPath(), cp.getProjectIni());
        }

        output.println(cp.getResponse());
    }
}
