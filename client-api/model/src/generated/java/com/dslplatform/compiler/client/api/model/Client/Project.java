package com.dslplatform.compiler.client.api.model.Client;

import com.dslplatform.patterns.*;
import com.dslplatform.client.*;
import com.fasterxml.jackson.annotation.*;

public class Project implements java.io.Serializable, AggregateRoot {
    public Project() {
        _serviceLocator = Bootstrap.getLocator();
        _domainProxy = _serviceLocator.resolve(DomainProxy.class);
        _crudProxy = _serviceLocator.resolve(CrudProxy.class);
        this.ID = java.util.UUID.randomUUID();
        this.CreatedAt = new org.joda.time.DateTime();
    }

    private transient final ServiceLocator _serviceLocator;
    private transient final DomainProxy _domainProxy;
    private transient final CrudProxy _crudProxy;

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
        final Project other = (Project) obj;

        return URI != null && URI.equals(other.URI);
    }

    @Override
    public String toString() {
        return URI != null ? "Project(" + URI + ')' : "new Project("
                + super.hashCode() + ')';
    }

    private static final long serialVersionUID = 0x0097000a;

    public Project(
            final String Nick,
            final org.joda.time.DateTime CreatedAt) {
        _serviceLocator = Bootstrap.getLocator();
        _domainProxy = _serviceLocator.resolve(DomainProxy.class);
        _crudProxy = _serviceLocator.resolve(CrudProxy.class);
        setNick(Nick);
        setCreatedAt(CreatedAt);
    }

    @JsonCreator
    private Project(
            @JacksonInject("_serviceLocator") final ServiceLocator _serviceLocator,
            @JsonProperty("URI") final String URI,
            @JsonProperty("ID") final java.util.UUID ID,
            @JsonProperty("Nick") final String Nick,
            @JsonProperty("CreatedAt") final org.joda.time.DateTime CreatedAt) {
        this._serviceLocator = _serviceLocator;
        this._domainProxy = _serviceLocator.resolve(DomainProxy.class);
        this._crudProxy = _serviceLocator.resolve(CrudProxy.class);
        this.URI = URI;
        this.ID = ID == null ? java.util.UUID.randomUUID() : ID;
        this.Nick = Nick;
        this.CreatedAt = CreatedAt == null
                ? new org.joda.time.DateTime()
                : CreatedAt;
    }

    private java.util.UUID ID;

    @JsonProperty("ID")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public java.util.UUID getID() {
        return ID;
    }

    private Project setID(final java.util.UUID value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"ID\" cannot be null!");
        this.ID = value;

        return this;
    }

    public static Project find(final String uri) throws java.io.IOException {
        return find(uri, Bootstrap.getLocator());
    }

    public static Project find(final String uri, final ServiceLocator locator)
            throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(CrudProxy.class).read(Project.class, uri).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    public static java.util.List<Project> find(final Iterable<String> uris)
            throws java.io.IOException {
        return find(uris, Bootstrap.getLocator());
    }

    public static java.util.List<Project> find(
            final Iterable<String> uris,
            final ServiceLocator locator) throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class).find(Project.class, uris).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    public static java.util.List<Project> findAll() throws java.io.IOException {
        return findAll(null, null, Bootstrap.getLocator());
    }

    public static java.util.List<Project> findAll(final ServiceLocator locator)
            throws java.io.IOException {
        return findAll(null, null, locator);
    }

    public static java.util.List<Project> findAll(
            final Integer limit,
            final Integer offset) throws java.io.IOException {
        return findAll(limit, offset, Bootstrap.getLocator());
    }

    public static java.util.List<Project> findAll(
            final Integer limit,
            final Integer offset,
            final ServiceLocator locator) throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class)
                    .findAll(Project.class, limit, offset, null).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    public static java.util.List<Project> search(
            final Specification<Project> specification)
            throws java.io.IOException {
        return search(specification, null, null, Bootstrap.getLocator());
    }

    public static java.util.List<Project> search(
            final Specification<Project> specification,
            final ServiceLocator locator) throws java.io.IOException {
        return search(specification, null, null, locator);
    }

    public static java.util.List<Project> search(
            final Specification<Project> specification,
            final Integer limit,
            final Integer offset) throws java.io.IOException {
        return search(specification, limit, offset, Bootstrap.getLocator());
    }

    public static java.util.List<Project> search(
            final Specification<Project> specification,
            final Integer limit,
            final Integer offset,
            final ServiceLocator locator) throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class)
                    .search(specification, limit, offset, null).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    public static long count() throws java.io.IOException {
        return count(Bootstrap.getLocator());
    }

    public static long count(final ServiceLocator locator)
            throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class).count(Project.class).get()
                    .longValue();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    public static long count(final Specification<Project> specification)
            throws java.io.IOException {
        return count(specification, Bootstrap.getLocator());
    }

    public static long count(
            final Specification<Project> specification,
            final ServiceLocator locator) throws java.io.IOException {
        try {
            return (locator != null ? locator : Bootstrap.getLocator())
                    .resolve(DomainProxy.class).count(specification).get()
                    .longValue();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    private void updateWithAnother(
            final com.dslplatform.compiler.client.api.model.Client.Project result) {
        this.URI = result.URI;

        this.Nick = result.Nick;
        this.CreatedAt = result.CreatedAt;
        this.ID = result.ID;
    }

    public Project persist() throws java.io.IOException {
        final Project result;
        try {
            result = this.URI == null
                    ? _crudProxy.create(this).get()
                    : _crudProxy.update(this).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
        this.updateWithAnother(result);
        return this;
    }

    public Project delete() throws java.io.IOException {
        try {
            return _crudProxy.delete(Project.class, URI).get();
        } catch (final InterruptedException e) {
            throw new java.io.IOException(e);
        } catch (final java.util.concurrent.ExecutionException e) {
            throw new java.io.IOException(e);
        }
    }

    private String Nick;

    @JsonProperty("Nick")
    public String getNick() {
        return Nick;
    }

    public Project setNick(final String value) {
        this.Nick = value;

        return this;
    }

    private org.joda.time.DateTime CreatedAt;

    @JsonProperty("CreatedAt")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public org.joda.time.DateTime getCreatedAt() {
        return CreatedAt;
    }

    public Project setCreatedAt(final org.joda.time.DateTime value) {
        if (value == null)
            throw new IllegalArgumentException(
                    "Property \"CreatedAt\" cannot be null!");
        this.CreatedAt = value;

        return this;
    }

    public static class FindByUserAndName implements java.io.Serializable,
            Specification<Project> {
        public FindByUserAndName(
                final String User,
                final String Name) {
            setUser(User);
            setName(Name);
        }

        public FindByUserAndName() {
            this.User = "";
            this.Name = "";
        }

        private static final long serialVersionUID = 0x0097000a;

        private String User;

        @com.fasterxml.jackson.annotation.JsonProperty("User")
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
        public String getUser() {
            return User;
        }

        public FindByUserAndName setUser(final String value) {
            if (value == null)
                throw new IllegalArgumentException(
                        "Property \"User\" cannot be null!");
            this.User = value;

            return this;
        }

        private String Name;

        @com.fasterxml.jackson.annotation.JsonProperty("Name")
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
        public String getName() {
            return Name;
        }

        public FindByUserAndName setName(final String value) {
            if (value == null)
                throw new IllegalArgumentException(
                        "Property \"Name\" cannot be null!");
            this.Name = value;

            return this;
        }

        public java.util.List<Project> search() throws java.io.IOException {
            return search(null, null, Bootstrap.getLocator());
        }

        public java.util.List<Project> search(final ServiceLocator locator)
                throws java.io.IOException {
            return search(null, null, locator);
        }

        public java.util.List<Project> search(
                final Integer limit,
                final Integer offset) throws java.io.IOException {
            return search(limit, offset, Bootstrap.getLocator());
        }

        public java.util.List<Project> search(
                final Integer limit,
                final Integer offset,
                final ServiceLocator locator) throws java.io.IOException {
            try {
                return (locator != null ? locator : Bootstrap.getLocator())
                        .resolve(DomainProxy.class)
                        .search(this, limit, offset, null).get();
            } catch (final InterruptedException e) {
                throw new java.io.IOException(e);
            } catch (final java.util.concurrent.ExecutionException e) {
                throw new java.io.IOException(e);
            }
        }

        public long count() throws java.io.IOException {
            return count(Bootstrap.getLocator());
        }

        public long count(final ServiceLocator locator)
                throws java.io.IOException {
            try {
                return (locator != null ? locator : Bootstrap.getLocator())
                        .resolve(DomainProxy.class).count(this).get()
                        .longValue();
            } catch (final InterruptedException e) {
                throw new java.io.IOException(e);
            } catch (final java.util.concurrent.ExecutionException e) {
                throw new java.io.IOException(e);
            }
        }
    }

    public static class FindByUser implements java.io.Serializable,
            Specification<Project> {
        public FindByUser(
                final String User) {
            setUser(User);
        }

        public FindByUser() {
            this.User = "";
        }

        private static final long serialVersionUID = 0x0097000a;

        private String User;

        @com.fasterxml.jackson.annotation.JsonProperty("User")
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
        public String getUser() {
            return User;
        }

        public FindByUser setUser(final String value) {
            if (value == null)
                throw new IllegalArgumentException(
                        "Property \"User\" cannot be null!");
            this.User = value;

            return this;
        }

        public java.util.List<Project> search() throws java.io.IOException {
            return search(null, null, Bootstrap.getLocator());
        }

        public java.util.List<Project> search(final ServiceLocator locator)
                throws java.io.IOException {
            return search(null, null, locator);
        }

        public java.util.List<Project> search(
                final Integer limit,
                final Integer offset) throws java.io.IOException {
            return search(limit, offset, Bootstrap.getLocator());
        }

        public java.util.List<Project> search(
                final Integer limit,
                final Integer offset,
                final ServiceLocator locator) throws java.io.IOException {
            try {
                return (locator != null ? locator : Bootstrap.getLocator())
                        .resolve(DomainProxy.class)
                        .search(this, limit, offset, null).get();
            } catch (final InterruptedException e) {
                throw new java.io.IOException(e);
            } catch (final java.util.concurrent.ExecutionException e) {
                throw new java.io.IOException(e);
            }
        }

        public long count() throws java.io.IOException {
            return count(Bootstrap.getLocator());
        }

        public long count(final ServiceLocator locator)
                throws java.io.IOException {
            try {
                return (locator != null ? locator : Bootstrap.getLocator())
                        .resolve(DomainProxy.class).count(this).get()
                        .longValue();
            } catch (final InterruptedException e) {
                throw new java.io.IOException(e);
            } catch (final java.util.concurrent.ExecutionException e) {
                throw new java.io.IOException(e);
            }
        }
    }
}
