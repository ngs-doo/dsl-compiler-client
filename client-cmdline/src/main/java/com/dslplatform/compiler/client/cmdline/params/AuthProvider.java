package com.dslplatform.compiler.client.cmdline.params;

import java.util.UUID;

import com.dslplatform.compiler.client.api.Cache;
import com.dslplatform.compiler.client.api.logging.Logger;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.Auth;
import com.dslplatform.compiler.client.api.params.Credentials;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.api.params.Token;
import com.dslplatform.compiler.client.cmdline.login.Login;
import com.dslplatform.compiler.client.cmdline.prompt.Prompt;

public class AuthProvider {
    private final Logger logger;
    private final Prompt prompt;
    private final Login login;

    private final Arguments arguments;
    private final Auth auth;

    public Auth getAuth() {
        return auth;
    }

    public AuthProvider(
            final Logger logger,
            final Prompt prompt,
            final Login login,
            final Arguments arguments) {
        this.logger = logger;
        this.prompt = prompt;
        this.login = login;

        this.arguments = arguments;
        this.auth = makeAuth();
    }

    private Auth makeAuth() {
        final Credentials credentials = makeCredentials();
        if (credentials != null) return credentials;

        final Token token = readToken();
        if (token != null) return token;

        return promptCredentials();
    }

    private Credentials makeCredentials() {
        logger.trace("Attempting to make credentials from arguments ...");

        final String username = arguments.getUsername();
        logger.trace("AuthProvider received username from arguments: " + username);

        final String password = arguments.getPassword();
        logger.trace("AuthProvider received password from arguments: " + (password == null ? null : "****"));

        if (username == null || password == null) {
            return null;
        }

        logger.debug("Constructing auth from arguments ...");
        return new Credentials(username, password);
    }

    private Cache getCache() {
        final String cachePath = arguments.getCachePath();
        logger.trace("AuthProvider received cache path from arguments: " + cachePath);

        final ProjectID projectID = arguments.getProjectID();
        final UUID pid = projectID == null ? null : projectID.projectID;
        logger.trace("AuthProvider received project ID from arguments: " + pid);

        final Cache cache = new Cache(logger, cachePath, pid);
        return cache;
    }

    private Token readToken() {
        logger.trace("Attempting to read the token from the cache ...");

        final Cache cache = getCache();
        final byte[] cookie = cache.get();

        if (cookie == null) {
            return null;
        }

        logger.debug("Constructing auth from a token ...");
        return new Token(cookie);
    }

    private Credentials promptCredentials() {
        logger.trace("Token authentication failed, trying to prompt for missing info ...");
        return login.acquireCredentials(arguments.getUsername(), arguments.getPassword());
    }

    public boolean isToken() {
        return auth instanceof Token;
    }

    public void setToken(final byte[] token) {
        getCache().set(token);
    }

    public void removeToken() {
        getCache().delete();
    }
}
