package com.dslplatform.compiler.client.api.core;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface HttpRequestBuilder {
    /**
     * Registers a user and sends an email confirmation to the provided address
     */
    public HttpRequest registerUser(final String email);

    /**
     * Verifies the syntax for the provided DSL
     */
    public HttpRequest parseDsl(final String token, final Map<String, String> dsl);

    /**
     * Creates a test project with a given name
     */
    public HttpRequest createTestProject(final String token, final String projectName);

    /**
     * Creates an external project, given a project name and database connection parameters
     */
    public HttpRequest createExternalProject(
            final String token,
            final String projectName,
            final String serverName,
            final String applicationName,
            final Map<String, Object> databaseConnection);

    /**
     * Downloads the server bundle for a non-test project.
     */
    public HttpRequest downloadBinaries(final String token, final UUID projectID);

    /**
     * Downloads just the generated model part of the server-bundle
     * (use-case after an update action)
     */
    public HttpRequest downloadGeneratedModel(final String token, final UUID projectID);

    /**
     * Compare new DSL with the last one compiled via DSL Platform
     */
    public HttpRequest inspectManagedProjectChanges(
            final String token,
            final UUID projectID,
            final Map<String, String> dsl);

    /**
     * Retrieve the last DSL for a managed project (test, external)
     */
    public HttpRequest getLastManagedDSL(final String token, final UUID projectID);

    /**
     * Retrieves the configuration file necessary to bootstrap the client libraries.
     */
    public HttpRequest getConfig(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options);

    /**
     * Updates a managed project with the new model, performing database migrations
     * In case of a test project, a new Revenj will be deployed as well.
     */
    public HttpRequest updateManagedProject(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final String migration,
            final Set<String> options,
            final Map<String, String> dsl);

    /**
     * Creates the migration SQL for deploying a model change on an unmanaged cluster.
     */
    public HttpRequest generateMigrationSQL(
            final String token,
            final String version,
            final Map<String, String> oldDsl,
            final Map<String, String> newDsl);

    /**
     * Retrieves the client source files for the last DSL applied to a managed project
     */
    public HttpRequest generateSources(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options);

    /**
     * Retrieves the client source files for an unmanaged project
     */
    public HttpRequest generateUnmanagedSources(
            final String token,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl);

    /**
     * Retrieves information about a project, given a project name
     */
    public HttpRequest getProjectByName(final String token, final String projectName);

    /**
     * Retrieves information about all projects for the current user
     */
    public HttpRequest getAllProjects(final String token);

    /**
     * Renames a project
     */
    public HttpRequest renameProject(final String token, final String oldName, final String newName);

    /**
     * Erases the entire database for a project, replacing it with an empty instance
     */
    public HttpRequest cleanProject(final String token);

    /**
     * Retrieve a template from the test project document repository
     */
    public HttpRequest templateGet(final String token, final String templateName);

    /**
     * Creates a new template in the test project document repository
     */
    public HttpRequest templateCreate(final String token, final String templateName, final byte[] content);

    /**
     * Lists all the templates in a test project's document repository
     */
    public HttpRequest templateListAll(final String token, final UUID projectID);

    /**
     * Removes a template from the test project document repository
     */
    public HttpRequest templateDelete(final String token, final String templateName);
}
