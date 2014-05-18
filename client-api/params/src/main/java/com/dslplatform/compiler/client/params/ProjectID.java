package com.dslplatform.compiler.client.params;

import java.util.UUID;

public class ProjectID implements Param {
    public final UUID projectID;

    public ProjectID(
            final UUID projectID) {
        this.projectID = projectID;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof ProjectID) || that == null) return false;

        final ProjectID thatProjectID = (ProjectID) that;
        return projectID.equals(thatProjectID.projectID);
    }

    @Override
    public int hashCode() {
        return projectID.hashCode();
    }

    @Override
    public String toString() {
        return "ProjectID(" + projectID + ")";
    }
}
