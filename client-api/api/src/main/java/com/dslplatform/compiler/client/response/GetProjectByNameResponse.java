package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.model.Project;

public class GetProjectByNameResponse extends AuthorizationResponse {
    public final Project project;

    public GetProjectByNameResponse(
            boolean authorized,
            String authorizationErrorMessage,
            Project project) {
        super(authorized, authorizationErrorMessage);
        this.project = project;
    }
}
