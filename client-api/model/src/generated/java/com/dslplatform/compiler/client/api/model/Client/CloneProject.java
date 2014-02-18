package com.dslplatform.compiler.client.api.model.Client;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;
import com.fasterxml.jackson.annotation.*;

public final class CloneProject implements DomainEvent, java.io.Serializable {
    public CloneProject(
            final String Project) {
        setProject(Project);
    }

    public CloneProject() {
        this.Project = "";
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
        final CloneProject other = (CloneProject) obj;

        return URI != null && URI.equals(other.URI);
    }

    @Override
    public String toString() {
        return URI != null ? "CloneProject(" + URI + ')' : "new CloneProject("
                + super.hashCode() + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String Project;

    @JsonProperty("Project")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getProject() {
        return Project;
    }

    public CloneProject setProject(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Project\" cannot be null!");
        this.Project = value;

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
