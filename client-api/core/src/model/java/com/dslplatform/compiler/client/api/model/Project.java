package com.dslplatform.compiler.client.api.model;

import java.util.UUID;

import org.joda.time.DateTime;

public class Project {
    public final UUID ID;
    public final String userID;      // ocd@dsl-platform.com
    public final DateTime createdAt; // 2014-03-14T18:55:26.462775+01:00
    public final String nick;
    public final ApplicationServer applicationServer;
    public final DatabaseConnection databaseConnection;

    public Project(
            final UUID ID,
            final String userID,
            final DateTime createdAt,
            final String nick,
            final ApplicationServer applicationServer,
            final DatabaseConnection databaseConnection) {
        this.ID = ID;
        this.userID = userID;
        this.createdAt = createdAt;
        this.nick = nick;
        this.applicationServer = applicationServer;
        this.databaseConnection = databaseConnection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        if (ID != null ? !ID.equals(project.ID) : project.ID != null) return false;
        if (applicationServer != null
                ? !applicationServer.equals(project.applicationServer)
                : project.applicationServer != null) return false;
        if (createdAt != null ? !createdAt.equals(project.createdAt) : project.createdAt != null) return false;
        if (databaseConnection != null
                ? !databaseConnection.equals(project.databaseConnection)
                : project.databaseConnection != null) return false;
        if (nick != null ? !nick.equals(project.nick) : project.nick != null) return false;
        if (userID != null ? !userID.equals(project.userID) : project.userID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ID != null ? ID.hashCode() : 0;
    }
}
