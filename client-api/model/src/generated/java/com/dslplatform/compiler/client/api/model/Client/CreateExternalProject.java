package com.dslplatform.compiler.client.api.model.Client;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;
import com.fasterxml.jackson.annotation.*;

public final class CreateExternalProject implements DomainEvent,
        java.io.Serializable {
    public CreateExternalProject(
            final String ProjectName,
            final String ServerName,
            final String ApplicationName,
            final com.dslplatform.compiler.client.api.model.Client.DatabaseConnection Database) {
        setProjectName(ProjectName);
        setServerName(ServerName);
        setApplicationName(ApplicationName);
        setDatabase(Database);
    }

    public CreateExternalProject() {
        this.ProjectName = "";
        this.ServerName = "";
        this.ApplicationName = "";
        this.Database = new com.dslplatform.compiler.client.api.model.Client.DatabaseConnection();
    }

    private String URI;

    @JsonProperty("URI")
    public String getURI() {
        return this.URI;
    }

    @Override
    public int hashCode() {
        return URI != null ? URI.hashCode() : super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;
        final CreateExternalProject other = (CreateExternalProject) obj;

        return URI != null && URI.equals(other.URI);
    }

    @Override
    public String toString() {
        return URI != null
                ? "CreateExternalProject(" + URI + ')'
                : "new CreateExternalProject(" + super.hashCode() + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String ProjectName;

    @JsonProperty("ProjectName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getProjectName() {
        return ProjectName;
    }

    public CreateExternalProject setProjectName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"ProjectName\" cannot be null!");
        this.ProjectName = value;

        return this;
    }

    private String ServerName;

    @JsonProperty("ServerName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getServerName() {
        return ServerName;
    }

    public CreateExternalProject setServerName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"ServerName\" cannot be null!");
        this.ServerName = value;

        return this;
    }

    private String ApplicationName;

    @JsonProperty("ApplicationName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getApplicationName() {
        return ApplicationName;
    }

    public CreateExternalProject setApplicationName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"ApplicationName\" cannot be null!");
        this.ApplicationName = value;

        return this;
    }

    private com.dslplatform.compiler.client.api.model.Client.DatabaseConnection Database;

    @JsonProperty("Database")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public com.dslplatform.compiler.client.api.model.Client.DatabaseConnection getDatabase() {
        return Database;
    }

    public CreateExternalProject setDatabase(
            final com.dslplatform.compiler.client.api.model.Client.DatabaseConnection value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Database\" cannot be null!");
        this.Database = value;

        return this;
    }

    public String submit() throws java.io.IOException {
        return submit(Bootstrap.getLocator());
    }

    public String submit(final ServiceLocator locator)
            throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class).submit(this).get();
        } catch (InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }
}
