package com.dslplatform.compiler.client.api.model.Client;

import com.fasterxml.jackson.annotation.*;

public final class ProjectDetails implements java.io.Serializable {
    public ProjectDetails(
            final java.util.UUID ID,
            final String projectName,
            final org.joda.time.DateTime createdAt) {
        setID(ID);
        setProjectName(projectName);
        setCreatedAt(createdAt);
    }

    public ProjectDetails() {
        this.ID = java.util.UUID.randomUUID();
        this.projectName = "";
        this.createdAt = new org.joda.time.DateTime();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 126464007;
        result = prime * result + (this.ID != null ? this.ID.hashCode() : 0);
        result = prime * result
                + (this.projectName != null ? this.projectName.hashCode() : 0);
        result = prime * result
                + (this.createdAt != null ? this.createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof ProjectDetails)) return false;
        final ProjectDetails other = (ProjectDetails) obj;

        if (!(this.ID.equals(other.ID))) return false;
        if (!(this.projectName.equals(other.projectName))) return false;
        if (!(this.createdAt.equals(other.createdAt))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ProjectDetails(" + ID + ',' + projectName + ',' + createdAt
                + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private java.util.UUID ID;

    @JsonProperty("ID")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.UUID getID() {
        return ID;
    }

    public ProjectDetails setID(final java.util.UUID value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"ID\" cannot be null!");
        this.ID = value;

        return this;
    }

    private String projectName;

    @JsonProperty("projectName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getProjectName() {
        return projectName;
    }

    public ProjectDetails setProjectName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"projectName\" cannot be null!");
        this.projectName = value;

        return this;
    }

    private org.joda.time.DateTime createdAt;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public org.joda.time.DateTime getCreatedAt() {
        return createdAt;
    }

    public ProjectDetails setCreatedAt(final org.joda.time.DateTime value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"createdAt\" cannot be null!");
        this.createdAt = value;

        return this;
    }
}
