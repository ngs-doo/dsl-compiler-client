package com.dslplatform.compiler.client.response;

public class RegisterUserResponse {

    public final boolean registered;

    public final String errorMessage;

    public RegisterUserResponse(boolean registered, String errorMessage) {
        this.registered = registered;
        this.errorMessage = errorMessage;
    }
}
