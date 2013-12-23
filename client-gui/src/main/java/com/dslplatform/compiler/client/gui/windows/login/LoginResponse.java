package com.dslplatform.compiler.client.gui.windows.login;

public class LoginResponse {

    public static enum Status {
        SUCCESS,
        INVALID,
        CANCELED,
        ERROR,
        EMPTY,
        PENDING
    }

    public final boolean ok;
    public final Status status;
    public final String message;

    public LoginResponse(
            final Status status,
            final String message) {
        this.ok = status == Status.SUCCESS;
        this.status = status;
        this.message = message;
    }
}
