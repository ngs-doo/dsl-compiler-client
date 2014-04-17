package com.dslplatform.compiler.client.response;

public class RenameProjectResponse extends AuthorizationResponse {

    final boolean successfull;

    public RenameProjectResponse(boolean authorized, String authorizationErrorMessage, boolean successfull) {
        super(authorized, authorizationErrorMessage);
        this.successfull = successfull;
    }
}
