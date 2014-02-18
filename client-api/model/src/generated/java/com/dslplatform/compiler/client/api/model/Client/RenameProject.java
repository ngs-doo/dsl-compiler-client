package com.dslplatform.compiler.client.api.model.Client;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;
import com.fasterxml.jackson.annotation.*;

public final class RenameProject implements DomainEvent, java.io.Serializable {
    public RenameProject(
            final String OldName,
            final String NewName) {
        setOldName(OldName);
        setNewName(NewName);
    }

    public RenameProject() {
        this.OldName = "";
        this.NewName = "";
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
        final RenameProject other = (RenameProject) obj;

        return URI != null && URI.equals(other.URI);
    }

    @Override
    public String toString() {
        return URI != null
                ? "RenameProject(" + URI + ')'
                : "new RenameProject(" + super.hashCode() + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String OldName;

    @JsonProperty("OldName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getOldName() {
        return OldName;
    }

    public RenameProject setOldName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"OldName\" cannot be null!");
        this.OldName = value;

        return this;
    }

    private String NewName;

    @JsonProperty("NewName")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getNewName() {
        return NewName;
    }

    public RenameProject setNewName(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"NewName\" cannot be null!");
        this.NewName = value;

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
