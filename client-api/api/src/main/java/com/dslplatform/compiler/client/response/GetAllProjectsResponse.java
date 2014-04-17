package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.model.Project;

import java.util.List;

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
