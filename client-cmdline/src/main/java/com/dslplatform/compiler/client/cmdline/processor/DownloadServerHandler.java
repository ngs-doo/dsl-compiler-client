package com.dslplatform.compiler.client.cmdline.processor;

import java.io.File;
import java.io.IOException;

import com.dslplatform.compiler.client.api.Actions;
import com.dslplatform.compiler.client.api.commons.io.FileUtils;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.api.processors.DownloadServerProcessor;
import com.dslplatform.compiler.client.cmdline.params.AuthProvider;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;
import com.dslplatform.compiler.client.io.Prompt;

public class DownloadServerHandler {
    private final Logger logger;
    private final Prompt prompt;
    private final Output output;
    private final Login login;
    private final Actions actions;

    public DownloadServerHandler(
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
        final File outputPath = arguments.getServerArchivePath();

        final DownloadServerProcessor dsp = actions.downloadServer(
                authProvider.getAuth(), projectID);

        if (dsp.isAuthorized()) {
            authProvider.setToken(dsp.getAuthorization());
        } else if (authProvider.isToken()) {
            authProvider.removeToken();
            apply(arguments);
            return;
        }

        if (dsp.isSuccessful()) {
            FileUtils.writeByteArrayToFile(outputPath, dsp.getServerArchive());
            logger.debug("Wrote server archive to: " + outputPath);
        }

        output.println(dsp.getResponse());
    }
}
