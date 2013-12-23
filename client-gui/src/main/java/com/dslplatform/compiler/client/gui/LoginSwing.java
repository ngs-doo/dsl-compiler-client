package com.dslplatform.compiler.client.gui;

import java.awt.GraphicsEnvironment;

import com.dslplatform.compiler.client.api.ApiCall;
import com.dslplatform.compiler.client.api.params.Credentials;
import com.dslplatform.compiler.client.gui.windows.login.LoginDialog;
import com.dslplatform.compiler.client.gui.windows.login.LoginDialogResult;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Output;

public class LoginSwing implements Login {
    private Logger logger;
    private Output output;
    private ApiCall apiCall;

    public LoginSwing(
            final Logger logger,
            final Output output) {
        this.logger = logger;
        this.output = output;
        this.apiCall = new ApiCall(logger);
    }

    @Override
    public boolean isAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }

    @Override
    public Credentials acquireCredentials(
            final String defaultUsername,
            final String defaultPassword) {

        Setup.setLookAndFeel(true);

        output.println("Please enter your credentials in the popup dialog ...");
        final LoginDialogResult result = LoginDialog.show(logger, apiCall,
                defaultUsername, defaultPassword, true);

        if (result.response.ok) {
            return new Credentials(result.request.username,
                    result.request.password);
        } else {
            throw new IllegalArgumentException(result.response.message);
        }
    }
}
