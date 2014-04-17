package com.dslplatform.compiler.client.response;

public class CreateExternalProjectResponse extends AuthorizationResponse {
    public final boolean createExternalProjectSuccessful;

    public CreateExternalProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean createExternalProjectSuccessful) {

        super(authorized, authorizationErrorMessage);
        this.createExternalProjectSuccessful = createExternalProjectSuccessful;
    }
}
