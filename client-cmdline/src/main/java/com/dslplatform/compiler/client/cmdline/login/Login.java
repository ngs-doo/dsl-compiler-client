package com.dslplatform.compiler.client.cmdline.login;

import com.dslplatform.compiler.client.api.params.Credentials;

public interface Login {
    public Credentials acquireCredentials(final String defaultUsername, final String defaultPassword);
}
