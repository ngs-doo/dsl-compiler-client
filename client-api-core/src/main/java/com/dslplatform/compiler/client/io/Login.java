package com.dslplatform.compiler.client.io;

import com.dslplatform.compiler.client.api.params.Credentials;

;

public interface Login {
    public boolean isAvailable();

    public Credentials acquireCredentials(
            final String defaultUsername,
            final String defaultPassword);
}
