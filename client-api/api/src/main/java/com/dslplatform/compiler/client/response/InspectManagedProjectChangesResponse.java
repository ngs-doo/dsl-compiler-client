package com.dslplatform.compiler.client.response;

import com.dslplatform.compiler.client.api.model.Change;

import java.util.List;

public class InspectManagedProjectChangesResponse extends AuthorizationResponse {
    final public List<Change> changes;

    public InspectManagedProjectChangesResponse(
            boolean authorized,
            String authorizationErrorMessage,
            List<Change> changes) {
        super(authorized, authorizationErrorMessage);
        this.changes = changes;
    }
}
