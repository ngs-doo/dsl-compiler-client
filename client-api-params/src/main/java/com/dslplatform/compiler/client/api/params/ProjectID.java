/**
 * Copyright (C) 2013 Nova Generacija Softvera d.o.o. (HR), <https://dsl-platform.com/>
 */
package com.dslplatform.compiler.client.api.params;

import java.util.UUID;

public class ProjectID implements Param {
    public final UUID projectID;

    public ProjectID(final UUID projectID) {
        this.projectID = projectID;
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean allowMultiple() { return false; }

    @Override
    public void addToPayload(final XMLOut xO) {
        xO.node("project-id", projectID.toString());
    }
}
