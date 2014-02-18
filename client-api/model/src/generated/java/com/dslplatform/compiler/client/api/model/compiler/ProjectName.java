package com.dslplatform.compiler.client.api.model.compiler;

import com.fasterxml.jackson.annotation.*;

public final class ProjectName
        implements
        java.io.Serializable,
        com.dslplatform.compiler.client.api.model.compiler.Project<com.dslplatform.compiler.client.api.model.compiler.ProjectName> {
    public ProjectName(
            final String projectName) {
        setProjectName(projectName);
    }

    public ProjectName() {
        this.projectName = "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 535300928;
        result = prime * result
                + (this.projectName != null ? this.projectName.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (!(obj instanceof ProjectName)) return false;
        final ProjectName other = (ProjectName) obj;

        if (!(this.projectName.equals(other.projectName))) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ProjectName(" + projectName + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String projectName;

    @JsonProperty("projectName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getProjectName() {
        return projectName;
    }

    public ProjectName setProjectName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"projectName\" cannot be null!");
        this.projectName = value;

        return this;
    }
}
