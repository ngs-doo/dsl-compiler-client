package com.dslplatform.compiler.client.cmdline.params;

import java.io.File;
import java.util.UUID;

import com.dslplatform.compiler.client.api.Cache;
import com.dslplatform.compiler.client.api.params.Arguments;
import com.dslplatform.compiler.client.api.params.Auth;
import com.dslplatform.compiler.client.api.params.Credentials;
import com.dslplatform.compiler.client.api.params.Token;
import com.dslplatform.compiler.client.io.Logger;
import com.dslplatform.compiler.client.io.Login;
import com.dslplatform.compiler.client.io.Prompt;

public class AuthProvider {
    private final Logger logger;
//    private final Prompt prompt;
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
//        this.prompt = prompt;
        this.login = login;

        this.arguments = arguments;
        auth = makeAuth();
    }

    private Auth makeAuth() {
        final Credentials credentials = makeCredentials();
        if (credentials != null) {
            return credentials;
        }

        final Token token = readToken();
        if (token != null) {
            return token;
        }

        return promptCredentials();
    }

    private Credentials makeCredentials() {
        logger.debug("Attempting to make credentials from arguments ...");

        final String username = arguments.getUsername();
        logger.debug("AuthProvider received username from arguments: "
                + username);

        final String password = arguments.getPassword();
        logger.trace("AuthProvider received password from arguments: "
                + (password == null ? null : "****"));

        if (username == null || password == null) {
            return null;
        }

        logger.debug("Constructing auth from arguments ...");
        return new Credentials(username, password);
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Cache getCache() {
        final File cachePath = arguments.getCachePath();
        logger.debug("AuthProvider received cache path from arguments: "
                + cachePath);

        UUID projectID = null;
        try {
            projectID = arguments.getProjectID().projectID;
            logger.debug("AuthProvider received project ID from arguments: "
                    + projectID);
        } catch (final NullPointerException e) {}

        final Cache cache = new Cache(logger, cachePath, projectID);
        return cache;
    }

    private Cache getCacheForProject(final UUID projectID) {
        final File cachePath = arguments.getCachePath();
        logger.debug("AuthProvider received cache path from arguments: "
                + cachePath);
        final Cache cache = new Cache(logger, cachePath, projectID);
        return cache;
    }

    public boolean isToken() {
        return auth instanceof Token;
    }

    public void setToken(final byte[] token) {
        getCache().set(token);
    }

    public void setToken(final UUID projectID, final byte[] token) {
        getCacheForProject(projectID).set(token);
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

    public void removeToken() {
        getCache().delete();
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Credentials promptCredentials() {
        logger.debug("Token authentication failed, trying to prompt for missing info ...");
        return login.acquireCredentials(arguments.getUsername(),
                arguments.getPassword());
    }
}
