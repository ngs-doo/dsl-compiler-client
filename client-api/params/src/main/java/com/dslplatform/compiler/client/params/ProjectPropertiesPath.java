package com.dslplatform.compiler.client.params;

import java.io.File;

public class ProjectPropertiesPath implements Param {
    public final File projectPropertiesPath;

    public ProjectPropertiesPath(
            final File projectPropertiesPath) {
        this.projectPropertiesPath = projectPropertiesPath;
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof ProjectPropertiesPath) || that == null) return false;

        final ProjectPropertiesPath thatProjectPropertiesPath = (ProjectPropertiesPath) that;
        return projectPropertiesPath.equals(thatProjectPropertiesPath.projectPropertiesPath);
    }

    @Override
    public int hashCode() {
        return projectPropertiesPath.hashCode();
    }

    @Override
    public String toString() {
        return "ProjectPropertiesPath(" + projectPropertiesPath + ")";
    }
}
