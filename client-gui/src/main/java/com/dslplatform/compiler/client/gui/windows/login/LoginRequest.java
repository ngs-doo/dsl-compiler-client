package com.dslplatform.compiler.client.gui.windows.login;

public class LoginRequest {
    public final String username;
    public final String password;
    public final boolean remember;

    public LoginRequest(
            final String username,
            final String password,
            final boolean remember) {
        this.username = username;
        this.password = password;
        this.remember = remember;
    }
}
