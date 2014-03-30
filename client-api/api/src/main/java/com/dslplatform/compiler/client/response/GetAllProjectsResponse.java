package com.dslplatform.compiler.client.response;

import java.util.List;
import java.util.UUID;

public class GetAllProjectsResponse extends AuthorizationResponse {

    final List<Project> projects;

    public GetAllProjectsResponse(
            boolean authorized,
            String authorizationErrorMessage,
            List<Project> projects) {
        super(authorized, authorizationErrorMessage);
        this.projects = projects;
    }
}
