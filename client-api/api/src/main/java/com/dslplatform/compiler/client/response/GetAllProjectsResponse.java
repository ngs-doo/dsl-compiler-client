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

    public static class Project{
        final public UUID id;
        final public String userID; //ocd@dsl-platform.com
        final public String createdAt; //2014-03-14T18:55:26.462775+01:00
        final public String databaseServer;
        final public String databasePort;
        final public String databaseName;
        final public String applicationServer;
        final public String applicationName;
        final public String applicationPoolName;
        final public String nick;

        public Project(
                UUID id,
                String userID,
                String createdAt,
                String databaseServer,
                String databasePort,
                String databaseName,
                String applicationServer,
                String applicationName,
                String applicationPoolName,
                String nick) {
            this.id = id;
            this.userID = userID;
            this.createdAt = createdAt;
            this.databaseServer = databaseServer;
            this.databasePort = databasePort;
            this.databaseName = databaseName;
            this.applicationServer = applicationServer;
            this.applicationName = applicationName;
            this.applicationPoolName = applicationPoolName;
            this.nick = nick;
        }
    }
}
