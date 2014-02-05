package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.api.processors.DownloadServerModelProcessor;
import com.dslplatform.compiler.client.cmdline.params.AuthProvider;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class DownloadServerModelHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;
    private final Actions actions;

    public DownloadServerModelHandler(
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
        arguments.readProjectIni();
        final ProjectID projectID = arguments.getProjectID();
        final AuthProvider authProvider = new AuthProvider(logger, prompt,
                login, arguments);
        final File outputPath = arguments.getServerModelPath();

        final DownloadServerModelProcessor dsmp = actions.downloadServerModel(
                authProvider.getAuth(), projectID);

        if (dsmp.isAuthorized()) {
            authProvider.setToken(dsmp.getAuthorization());
        } else if (authProvider.isToken()) {
            authProvider.removeToken();
            apply(arguments);
            return;
        }

        if (dsmp.isSuccessful()) {
            FileUtils.writeByteArrayToFile(outputPath, dsmp.getServerModel());
            logger.debug("Wrote server archive to: " + outputPath);
        }

        output.println(dsmp.getResponse());
    }
}
