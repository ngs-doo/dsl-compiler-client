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
}
