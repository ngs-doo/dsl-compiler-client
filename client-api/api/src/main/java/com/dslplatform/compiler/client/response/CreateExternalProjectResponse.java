package com.dslplatform.compiler.client.response;

public class CreateExternalProjectResponse extends AuthorizationResponse {
    private final boolean createExternalProjectSuccessful;

    public boolean isCreateExternalProjectSuccessful() {
        return createExternalProjectSuccessful;
    }

    public CreateExternalProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean createExternalProjectSuccessful) {

        super(authorized, authorizationErrorMessage);
        this.createExternalProjectSuccessful = createExternalProjectSuccessful;
    }

}
