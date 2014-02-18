package com.dslplatform.compiler.client.api.model.Client;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;
import com.fasterxml.jackson.annotation.*;

public final class Register implements DomainEvent, java.io.Serializable {
    public Register(
            final String Email) {
        setEmail(Email);
    }

    public Register() {
        this.Email = "";
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
        final Register other = (Register) obj;

        return URI != null && URI.equals(other.URI);
    }

    @Override
    public String toString() {
        return URI != null ? "Register(" + URI + ')' : "new Register("
                + super.hashCode() + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    private String Email;

    @JsonProperty("Email")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getEmail() {
        return Email;
    }

    public Register setEmail(final String value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"Email\" cannot be null!");
        this.Email = value;

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
