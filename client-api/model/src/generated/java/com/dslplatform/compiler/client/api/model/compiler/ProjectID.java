package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class ProjectID
        implements
        java.io.Serializable,
        com.dslplatform.compiler.client.api.model.compiler.Project<com.dslplatform.compiler.client.api.model.compiler.ProjectID> {
    public ProjectID(
            final java.util.UUID projectID) {
        setProjectID(projectID);
    }

    public ProjectID() {
        this.projectID = java.util.UUID.randomUUID();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 469814658;
        result = prime * result
                + (this.projectID != null ? this.projectID.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof ProjectID)) return false;
        final ProjectID other = (ProjectID) obj;

        if (!(this.projectID.equals(other.projectID))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ProjectID(" + projectID + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private java.util.UUID projectID;

    @JsonProperty("projectID")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.UUID getProjectID() {
        return projectID;
    }

    public ProjectID setProjectID(final java.util.UUID value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"projectID\" cannot be null!");
        this.projectID = value;

        return this;
    }
}
