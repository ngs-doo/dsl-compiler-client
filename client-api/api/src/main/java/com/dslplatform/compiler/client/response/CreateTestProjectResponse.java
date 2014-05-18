package com.dslplatform.compiler.client.response;

public class CreateTestProjectResponse extends AuthorizationResponse {
    private final boolean createTestProjectSuccessful;

    public boolean isCreateTestProjectSuccessful() {
        return createTestProjectSuccessful;
    }

    public CreateTestProjectResponse(
            boolean authorized,
            String authorizationErrorMessage,
            boolean createTestProjectSuccessful) {

        super(authorized, authorizationErrorMessage);
        this.createTestProjectSuccessful = createTestProjectSuccessful;
    }
}
