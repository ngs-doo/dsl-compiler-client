package com.dslplatform.compiler.client.api.model.Client.repositories;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;

public class ProjectRepository
        extends
        ClientPersistableRepository<com.dslplatform.compiler.client.api.model.Client.Project> {
    public ProjectRepository(
            final ServiceLocator locator) {
        super(com.dslplatform.compiler.client.api.model.Client.Project.class,
                locator);
    }
}
