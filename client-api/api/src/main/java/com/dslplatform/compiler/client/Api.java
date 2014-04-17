package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.response.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface Api {
    /**
     * Registers a user and sends an email confirmation to the provided address
     */
    public RegisterUserResponse registerUser(
            final String email);

    /**
     * Verifies the syntax for the provided DSL
     */
    public ParseDSLResponse parseDSL(
            final String token, final Map<String, String> dsl);

    /**
     * Creates a test project with a given name
     */
    public CreateTestProjectResponse createTestProject(
            final String token, final String projectName);

    /**
     * Creates an external project, given a project name and database connection parameters
     */
    public CreateExternalProjectResponse createExternalProject(
            final String token,
            final String projectName,
            final String serverName,
            final String applicationName,
            final Map<String, Object> databaseConnection);

    /**
     * Downloads the server bundle for a non-test project.
     */
    public DownloadBinariesResponse downloadBinaries(
            final String token, final UUID projectID);

    /**
     * Downloads just the generated model part of the server-bundle
     * (use-case after an update action)
     */
    public DownloadGeneratedModelResponse downloadGeneratedModel(
            final String token, final UUID projectID);

    /**
     * Compare new DSL with the last one compiled via DSL Platform
     */
    public InspectManagedProjectChangesResponse inspectManagedProjectChanges(
            final String token,
            final UUID projectID,
            final Map<String, String> dsl);

    /**
     * Retrieve the last DSL for a managed project (test, external)
     */
    public GetLastManagedDSLResponse getLastManagedDSL(
            final String token, final UUID projectID);

    /**
     * Retrieves the configuration file necessary to bootstrap the client libraries.
     */
    public GetConfigResponse getConfig(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options);

    /**
     * Updates a managed project with the new model, performing database migrations
     * In case of a test project, a new Revenj will be deployed as well.
     */
    public UpdateManagedProjectResponse updateManagedProject(
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
    public GenerateMigrationSQLResponse generateMigrationSQL(
            String token,
            String version,
            Map<String, String> oldDsl,
            Map<String, String> newDsl);

    /**
     * Retrieves the client source files for the last DSL applied to a managed project
     */
    public GenerateSourcesResponse generateSources(
            final String token,
            final UUID projectID,
            final Set<String> targets,
            final String packageName,
            final Set<String> options);

    /**
     * Retrieves the client source files for an unmanaged project
     */
    public GenerateUnmanagedSourcesResponse generateUnmanagedSources(
            final String token,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl);

    /**
     * Retrieves information about a project, given a project name
     */
    public GetProjectByNameResponse getProjectByName(
            final String token, final String projectName);

    /**
     * Retrieves information about all projects for the current user
     */
    public GetAllProjectsResponse getAllProjects(
            final String token);

    /**
     * Renames a project
     */
    public RenameProjectResponse renameProject(
            final String token, final String oldName, final String newName);

    /**
     * Erases the entire database for a project, replacing it with an empty instance
     */
    public CleanProjectResponse cleanProject(
            final String token);

    /**
     * Retrieve a template from the test project document repository
     */
    public TemplateGetResponse templateGet(
            final String token, final String projectID, final String templateName);

    /**
     * Creates a new template in the test project document repository
     */
    public TemplateCreateResponse templateCreate(
            final String token,
            final String templateName,
            final byte[] content);

    /**
     * Lists all the templates in a test project's document repository
     */
    public TemplateListAllResponse templateListAll(
            final String token, final UUID projectID);

    /**
     * Removes a template from the test project document repository
     */
    public TemplateDeleteResponse templateDelete(
            final String token, final String templateName);

    public DoesUnmanagedDSLExitsResponse doesUnmanagedDSLExits(final DataSource dataSource);

    /**
     * Retrieve all DSLs for an unmanaged project
     */
    public GetAllUnmanagedDSLResponse  getAllUnmanagedDSL(
            final DataSource dataSource);

    /**
     * Retrieve the last DSL for an unmanaged project
     */
    public GetLastUnmanagedDSLResponse getLastUnmanagedDSL(
            final DataSource dataSource);

    /**
     * Compare new DSL with the old one, retrieved from the unamanaged database.
     */
    public InspectUnmanagedProjectChangesResponse inspectUnmanagedProjectChanges(
            final DataSource dataSource,
            final String version,
            final Map<String, String> dsl);

    /**
     * Creates an unmanaged project
     */
    public CreateUnmanagedProjectResponse createUnmanagedProject(
            final String token,
            final DataSource dataSource,
            final String serverName,
            final String applicationName);

    public CreateUnmanagedServerResponse createUnmanagedServer(
            final String token,
            final DataSource dataSource,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl);
    /**
     * Compare new DSL with the old one, retrieved from the unamanaged database.
     */
    public UpgradeUnmanagedDatabaseResponse upgradeUnmanagedDatabase(
            final DataSource dataSource,
            final List<String> migration);


    public UpgradeUnmanagedServerResponse upgradeUnmanagedServer(
            final String token,
            final DataSource dataSource,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl);
}
