package com.dslplatform.compiler.client.cmdline.login;

import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.params.Credentials;
import com.dslplatform.compiler.client.cmdline.prompt.Prompt;

public class LoginConsole implements Login {
    private final Logger logger;
    private final Prompt prompt;

    public LoginConsole(final Logger logger, final Prompt prompt) {
        this.logger = logger;
        this.prompt = prompt;
    }

    private String begForUsername(final String defaultUsername) {
        while (true) {
            final String username = prompt.readLine("Username" + (defaultUsername == null ? ": " : " [" + defaultUsername + "]: "), null);
            if (!username.isEmpty()) return username;
            if (defaultUsername != null) return defaultUsername;
        }
    }

    private String begForPassword(final String defaultPassword) {
        while (true) {
            final String password = prompt.readLine("Password" + (defaultPassword == null ? ": " : " [****]: "), '*');
            if (!password.isEmpty()) return password;
            if (defaultPassword != null) return defaultPassword;
        }
    }


    @Override
    public Credentials acquireCredentials(final String defaultUsername, final String defaultPassword) {
        return new Credentials(begForUsername(defaultUsername), begForPassword(defaultPassword));
    }
}
