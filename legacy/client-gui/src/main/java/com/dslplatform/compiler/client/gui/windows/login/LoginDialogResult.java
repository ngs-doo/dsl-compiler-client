package com.dslplatform.compiler.client.gui.windows.login;

public class LoginDialogResult {
    public final LoginRequest request;
    public final LoginResponse response;

    public LoginDialogResult(
            final LoginRequest request,
            final LoginResponse response) {
        this.request = request;
        this.response = response;
    }

}
