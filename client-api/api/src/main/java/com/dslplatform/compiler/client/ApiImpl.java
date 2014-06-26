package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.model.Migration;
import com.dslplatform.compiler.client.processor.*;
import com.dslplatform.compiler.client.response.*;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class ApiImpl implements Api {

    private static final String NGSdbmigrationAbsent = "relation \"-NGS-.database_migration\" does not exist";
    private static final String version_real = "1.0.1.24037";
    private static final String compilationMessage = "Compilation successful";
    private static final String compileScriptWriteErrorMsg = "Unable to write script to file ";

    private final HttpRequestBuilder httpRequestBuilder;
    private final HttpTransport httpTransport;
    private final UnmanagedDSL unmanagedDSL;

    public Logger logger;

    public ApiImpl(
            Logger logger,
            HttpRequestBuilder httpRequestBuilder,
            HttpTransport httpTransport,
            UnmanagedDSL unmanagedDSL) {
        this.logger = logger;
        this.httpRequestBuilder = httpRequestBuilder;
        this.httpTransport = httpTransport;
        this.unmanagedDSL = unmanagedDSL;
    }

    public ApiImpl(
            HttpRequestBuilder httpRequestBuilder,
            HttpTransport httpTransport,
            UnmanagedDSL unmanagedDSL) {
        this.logger = LoggerFactory.getLogger(ApiImpl.class);
        this.httpRequestBuilder = httpRequestBuilder;
        this.httpTransport = httpTransport;
        this.unmanagedDSL = unmanagedDSL;
    }

    @Override
    public RegisterUserResponse registerUser(
            String email) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.registerUser(email));
        } catch (IOException e) {
            return new RegisterUserResponse(false, e.getMessage());
        }

        return new RegisterUserProcessor().process(httpResponse);
    }

    @Override
    public ParseDSLResponse parseDSL(
            String token,
            Map<String, String> dsl
    ) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.parseDSL(token, dsl));
        } catch (IOException e) {
            return new ParseDSLResponse(false, e.getMessage(), false, null);
        }

        return new ParseDSLProcessor().process(httpResponse);
    }

    @Override
    public CreateTestProjectResponse createTestProject(
            String token, String projectName) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.createTestProject(token, projectName));
        } catch (IOException e) {
            return new CreateTestProjectResponse(false, e.getMessage(), false);
        }

        return new CreateTestProjectProcessor().process(httpResponse);
    }

    @Override
    public CreateExternalProjectResponse createExternalProject(
            String token,
            String projectName,
            String serverName,
            String applicationName,
            Map<String, Object> databaseConnection) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.createExternalProject(token, projectName,
                    serverName, applicationName, databaseConnection));
        } catch (IOException e) {
            return new CreateExternalProjectResponse(false, e.getMessage(), false);
        }

        return new CreateExternalProjectProcessor().process(httpResponse);
    }

    @Override
    public DownloadBinariesResponse downloadBinaries(
            String token, UUID projectID) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.downloadBinaries(token, projectID));
        } catch (IOException e) {
            return new DownloadBinariesResponse(false, e.getMessage(), null);
        }

        return new DownloadBinariesProcessor().process(httpResponse);
    }

    @Override
    public DownloadGeneratedModelResponse downloadGeneratedModel(
            String token, UUID projectID) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.downloadGeneratedModel(token, projectID));
        } catch (IOException e) {
            return new DownloadGeneratedModelResponse(false, e.getMessage(), null);
        }

        return new DownloadGeneratedModelProcessor().process(httpResponse);
    }

    @Override
    public InspectManagedProjectChangesResponse inspectManagedProjectChanges(
            String token,
            UUID projectID,
            Map<String, String> dsl) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.inspectManagedProjectChanges(token, projectID, dsl));
        } catch (IOException e) {
            return new InspectManagedProjectChangesResponse(false, e.getMessage(), null);
        }

        return new InspectManagedProjectChangesProcessor().process(httpResponse);
    }

    @Override
    public GetLastManagedDSLResponse getLastManagedDSL(
            String token, UUID projectID) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.getLastManagedDSL(token, projectID));
        } catch (IOException e) {
            return new GetLastManagedDSLResponse(false, e.getMessage(), null);
        }

        return new GetLastManagedDSLProcessor().process(httpResponse);
    }

    @Override
    public GetConfigResponse getConfig(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            Set<String> options) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport
                    .sendRequest(httpRequestBuilder.getConfig(token, projectID, targets, packageName, options));
        } catch (IOException e) {
            return new GetConfigResponse(false, e.getMessage(), null);
        }

        return new GetConfigProcessor().process(httpResponse);
    }

    @Override
    public UpdateManagedProjectResponse updateManagedProject(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            String migration,
            Set<String> options,
            Map<String, String> dsl) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.updateManagedProject(token, projectID, targets,
                    packageName, migration, options, dsl));
        } catch (IOException e) {
            return new UpdateManagedProjectResponse(false, e.getMessage());
        }

        if (logger.isTraceEnabled())
            logger.trace("Upgrade managed response {}", new String(httpResponse.body), Charsets.UTF_8);
        return new UpdateManagedProjectProcessor().process(httpResponse);
    }

    @Override
    public GenerateMigrationSQLResponse generateMigrationSQL(
            String token,
            String version,
            Map<String, String> oldDsl,
            Map<String, String> newDsl) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.generateMigrationSQL(token, version, oldDsl, newDsl));
        } catch (IOException e) {
            return new GenerateMigrationSQLResponse(false, e.getMessage());
        }

        return new GenerateMigrationSQLProcessor().process(httpResponse);
    }

    @Override
    public GenerateMigrationSQLResponse generateMigrationSQL(
            final String token,
            final DataSource dataSource,
            final Map<String, String> dsl
    ) {
        final DoesUnmanagedDSLExitsResponse doesUnmanagedDSLExitsResponse = doesUnmanagedDSLExits(dataSource);
        final Map<String, String> lastdsl;
        String version = "1.0.1.24037";
        if (!doesUnmanagedDSLExitsResponse.databaseExists) {
            lastdsl = new HashMap<String, String>();
        } else {
            final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = getLastUnmanagedDSL(dataSource);
            if (!getLastUnmanagedDSLResponse.databaseConnectionSuccessful) {
                final String authMsg =
                        "Unable to get last dsl: " +
                                (getLastUnmanagedDSLResponse.databaseConnectionErrorMessage != null
                                        ? getLastUnmanagedDSLResponse.databaseConnectionErrorMessage
                                        : "<last dsl msg null>");

                return new GenerateMigrationSQLResponse(false, authMsg);
            }
            version = getLastUnmanagedDSLResponse.lastMigration.version;
            lastdsl = getLastUnmanagedDSLResponse.lastMigration.dsls;
        }

        return generateMigrationSQL(token, version, lastdsl, dsl);
    }

    @Override
    public GenerateSourcesResponse generateSources(
            String token,
            UUID projectID,
            Set<String> targets,
            String packageName,
            Set<String> options) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(
                    httpRequestBuilder.generateSources(
                            token,
                            projectID,
                            targets,
                            packageName,
                            options)
            );
        } catch (IOException e) {
            e.printStackTrace();
            return new GenerateSourcesResponse(false, e.getMessage(), false, null);
        }

        return new GenerateSourcesProcessor().process(httpResponse);
    }

    @Override
    public GenerateSourcesResponse generateUnmanagedSources(
            String token,
            String packageName,
            Set<String> targets,
            Set<String> options,
            Map<String, String> dsl) {
        final HttpResponse generateSourcesResponse;
        try {
            generateSourcesResponse =
                    httpTransport.sendRequest(
                            httpRequestBuilder.generateUnmanagedSources(token, packageName, targets, options, dsl));
        } catch (IOException e) {
            e.printStackTrace();
            return new GenerateSourcesResponse(false, e.getMessage());
        }

        logger.trace("Response for unmanaged request: {}", new String(generateSourcesResponse.body, Charsets.UTF_8));

        return new GenerateUnmanagedSourcesProcessor().process(generateSourcesResponse);
    }

    @Override
    public GetProjectByNameResponse getProjectByName(
            String token, String projectName) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.getProjectByName(token, projectName));
        } catch (IOException e) {
            return new GetProjectByNameResponse(false, e.getMessage(), null);
        }

        return new GetProjectByNameProcessor().process(httpResponse);
    }

    @Override
    public GetAllProjectsResponse getAllProjects(
            String token) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.getAllProjects(token));
        } catch (IOException e) {
            return new GetAllProjectsResponse(false, e.getMessage(), null);
        }

        return new GetAllProjectsProcessor().process(httpResponse);

    }

    @Override
    public RenameProjectResponse renameProject(
            String token, String oldName, String newName) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.renameProject(token, oldName, newName));
        } catch (IOException e) {
            return new RenameProjectResponse(false, e.getMessage(), false);
        }
        return new RenameProjectProcessor().process(httpResponse);
    }

    @Override
    public CleanProjectResponse cleanProject(
            String token) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.cleanProject(token));
        } catch (IOException e) {
            return new CleanProjectResponse(false, e.getMessage(), false);
        }

        return new CleanProjectProcessor().process(httpResponse);
    }

    @Override
    public TemplateGetResponse templateGet(
            String token,
            String projectID,
            String templateName) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.templateGet(token, projectID, templateName));
        } catch (IOException e) {
            return new TemplateGetResponse(false, e.getMessage(), null);
        }

        return new TemplateGetProcessor().process(httpResponse);
    }

    @Override
    public TemplateCreateResponse templateCreate(
            String token,
            String projectID,
            String templateName,
            byte[] content
    ) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport
                            .sendRequest(httpRequestBuilder.templateCreate(token, projectID, templateName, content));
        } catch (IOException e) {
            return new TemplateCreateResponse(false, e.getMessage(), false);
        }

        return new TemplateCreateProcessor().process(httpResponse);
    }

    @Override
    public TemplateListAllResponse templateListAll(
            String token,
            String projectID) {
        final HttpResponse httpResponse;
        try {
            httpResponse =
                    httpTransport.sendRequest(httpRequestBuilder.templateListAll(token, projectID));
        } catch (IOException e) {
            return new TemplateListAllResponse(false, e.getMessage(), null);
        }

        return new TemplateListAllProcessor().process(httpResponse);
    }

    @Override
    public TemplateDeleteResponse templateDelete(
            String token,
            String projectID,
            String templateName) {
        final HttpResponse httpResponse;
        try {
            httpResponse = httpTransport.sendRequest(httpRequestBuilder.templateDelete(token, projectID, templateName));
        } catch (IOException e) {
            return new TemplateDeleteResponse(false, e.getMessage(), false);
        }

        return new TemplateDeleteProcessor().process(httpResponse);
    }

    @Override
    public DoesUnmanagedDSLExitsResponse doesUnmanagedDSLExits(DataSource dataSource) {
        if (dataSource == null) return new DoesUnmanagedDSLExitsResponse(true, null, false);
        try {
            return new DoesUnmanagedDSLExitsResponse(true, null, unmanagedDSL.doesUnmanagedDSLExits(dataSource));
        } catch (SQLException e) {
            return new DoesUnmanagedDSLExitsResponse(true, e.getMessage(), false);
        }
    }

    @Override
    public GetAllUnmanagedDSLResponse getAllUnmanagedDSL(
            DataSource dataSource) {
        final List<Migration> migrations;
        try {
            migrations = unmanagedDSL.getAllUnmanagedDSL(dataSource);
        } catch (SQLException e) {
            return new GetAllUnmanagedDSLResponse(false, e.getMessage(), null);
        }

        return new GetAllUnmanagedDSLResponse(true, null, migrations);
    }

    @Override
    public GetLastUnmanagedDSLResponse getLastUnmanagedDSL(
            final DataSource dataSource) {
        try {
            final Migration migration;
            if (doesUnmanagedDSLExits(dataSource).databaseExists) {
                logger.trace("Getting last migration");
                migration = unmanagedDSL.getLastUnmanagedDSL(dataSource);
            } else return new GetLastUnmanagedDSLResponse(true, null, null);
            return new GetLastUnmanagedDSLResponse(true, null, migration);
        } catch (SQLException e) {
            return new GetLastUnmanagedDSLResponse(false, e.getMessage(), null);
        }
    }

    @Override
    public InspectUnmanagedProjectChangesResponse inspectUnmanagedProjectChanges(
            DataSource dataSource, String version, Map<String, String> dsl) {
        return null;
    }

    @Override
    public CreateUnmanagedProjectResponse createUnmanagedProject(
            String token, DataSource dataSource, String serverName, String applicationName) {

        return null;
    }

    @Override
    public UpgradeUnmanagedDatabaseResponse upgradeUnmanagedDatabase(
            DataSource dataSource,
            List<String> migration) {
        try {
            unmanagedDSL.upgradeUnmanagedDatabase(dataSource, migration);
            return UpgradeUnmanagedDatabaseResponse.success();
        } catch (SQLException e) {
            return UpgradeUnmanagedDatabaseResponse.error(e);
        }
    }


    @Override
    public CreateUnmanagedServerResponse createUnmanagedServer(
            final String token,
            final DataSource dataSource,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl) {
        final GenerateSourcesResponse generateSourcesResponse =
                generateUnmanagedSources(token, packageName, targets, options, dsl);

        final GenerateMigrationSQLResponse generateMigrationSQLResponse =
                generateMigrationSQL(token, version_real, new HashMap<String, String>(), dsl);
        if (!generateMigrationSQLResponse.authorized)
            return new CreateUnmanagedServerResponse(false, generateMigrationSQLResponse.authorizationErrorMessage);

        return new CreateUnmanagedServerResponse(true, null, generateMigrationSQLResponse.migration,
                generateSourcesResponse.sources);
    }

    @Override
    public UpgradeUnmanagedServerAndDatabaseResponse upgradeUnmanagedServerAndDataBase(
            DataSource dataSource,
            String migration,
            List<Source> sources,
            File dependencies,
            File serverPath) {
        if (dependencies == null || dependencies.isFile())
            new IllegalArgumentException("Must provide a folder in which source build dependencies reside.");
        if (serverPath == null || serverPath.isFile())
            new IllegalArgumentException("Must provide a folder to deploy server to");
        final String tmpName = "tmp-" + java.util.UUID.randomUUID().toString();
        final File tempFile = new File(tmpName);
        final UpgradeUnmanagedServerAndDatabaseResponse upgradeUnmanagedServerAndDatabaseResponse =
                upgradeUnmanagedServerAndDataBase(dataSource, migration, sources, dependencies, serverPath, tempFile);
        tempFile.delete();

        return upgradeUnmanagedServerAndDatabaseResponse;
    }

    @Override
    public UpgradeUnmanagedServerAndDatabaseResponse upgradeUnmanagedServerAndDataBase(
            DataSource dataSource,
            String migration,
            List<Source> sources,
            File dependencies,
            File serverPath,
            File targetOutput) {
        if (serverPath == null || serverPath.isFile())
            new IllegalArgumentException("Must provide a folder to deploy server to");
        if (targetOutput == null || targetOutput.isDirectory())
            new IllegalArgumentException("Must provide a File as a target to write to.");
        final String tmpName = "tmp-" + java.util.UUID.randomUUID().toString();
        final File tempFile = new File(tmpName);
        final UpgradeUnmanagedServerAndDatabaseResponse upgradeUnmanagedServerAndDatabaseResponse =
                upgradeUnmanagedServerAndDataBase(dataSource, migration, sources, dependencies, serverPath, targetOutput, tempFile);
        try {
            FileUtils.deleteDirectory(tempFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return upgradeUnmanagedServerAndDatabaseResponse;
    }

    @Override
    public UpgradeUnmanagedServerAndDatabaseResponse upgradeUnmanagedServerAndDataBase(
            DataSource dataSource,
            String migration,
            List<Source> sources,
            File dependencies,
            File serverPath,
            File sourceOutput,
            File targetOutput) {
        // Upgrade database
        try {
            unmanagedDSL.upgradeUnmanagedDatabase(dataSource, Arrays.asList(migration));
        } catch (SQLException e) {
            return new UpgradeUnmanagedServerAndDatabaseResponse(false, e.getMessage());
        }

        // write files to disk
        for (Source source : sources) {
            if (source.language.toLowerCase() == "csharpserver") try { // todo - hardcodes -> enum!
                FileUtils.writeByteArrayToFile(new File(sourceOutput, source.path), source.content);
            } catch (IOException e) {
                new RuntimeException(e.getMessage());
            }
        }

        compileCSharpServer(sourceOutput, dependencies, targetOutput);

        // TODO - link Revenj.Http.config to given path?

        return new UpgradeUnmanagedServerAndDatabaseResponse(true, null, true, true);
    }

    @Override
    public UpgradeUnmanagedServerResponse upgradeUnmanagedServer(
            final String token,
            final DataSource dataSource,
            final String packageName,
            final Set<String> targets,
            final Set<String> options,
            final Map<String, String> dsl) {
        final GenerateSourcesResponse GenerateSourcesResponse =
                generateUnmanagedSources(token, packageName, targets, options, dsl);
        if (!GenerateSourcesResponse.generatedSuccess) {
            final String authMsg = "Source generation unsuccessful " +
                    (GenerateSourcesResponse.authorizationErrorMessage != null
                            ? GenerateSourcesResponse.authorizationErrorMessage
                            : "<generate msg null>");

            return new UpgradeUnmanagedServerResponse(false, authMsg);
        }
        final DoesUnmanagedDSLExitsResponse doesUnmanagedDSLExitsResponse = doesUnmanagedDSLExits(dataSource);
        final Map<String, String> lastdsl;
        String version = version_real;
        if (!doesUnmanagedDSLExitsResponse.databaseExists) {
            lastdsl = new HashMap<String, String>();
        } else {
            final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = getLastUnmanagedDSL(dataSource);

            if (!getLastUnmanagedDSLResponse.databaseConnectionSuccessful) {
                final String authMsg =
                        "Unable to get last dsl: " +
                                (getLastUnmanagedDSLResponse.databaseConnectionErrorMessage != null
                                        ? getLastUnmanagedDSLResponse.databaseConnectionErrorMessage
                                        : "<last dsl msg null>");

                return new UpgradeUnmanagedServerResponse(false, authMsg);
            }
            version = getLastUnmanagedDSLResponse.lastMigration.version;
            lastdsl = getLastUnmanagedDSLResponse.lastMigration.dsls;
        }
        final GenerateMigrationSQLResponse generateMigrationSQLResponse =
                generateMigrationSQL(token, version, lastdsl, dsl);
        if (!generateMigrationSQLResponse.authorized)
            return new UpgradeUnmanagedServerResponse(false, generateMigrationSQLResponse.authorizationErrorMessage);

        return new UpgradeUnmanagedServerResponse(true, null, generateMigrationSQLResponse.migration,
                GenerateSourcesResponse.sources);
    }

    @Override
    public CompileCSharpServerResponse compileCSharpServer(
            File sourcePath,
            File dependencies,
            File target) {
        String runScript = makeRunScript(sourcePath, dependencies, target);
        Process process = null;
        logger.trace("About to run mcs script");
        try {
            process = Runtime.getRuntime().exec(runScript);
            final Process finalProcess = process;

            /* Consume output stream */
            new Thread() {
                public void run() {
                    final InputStream in = finalProcess.getInputStream();
                    try {
                        final InputStreamReader inputStreamReader = new InputStreamReader(in);
                        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) logger.trace(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        close(in);
                    }
                }
            }.start();

            /* Consume error stream */
            new Thread() {
                public void run() {
                    final InputStream in = finalProcess.getErrorStream();
                    try {
                        final InputStreamReader inputStreamReader = new InputStreamReader(in);
                        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) logger.error(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        close(in);
                    }
                }
            }.start();
            process.waitFor();

            return new CompileCSharpServerResponse(true, compilationMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return new CompileCSharpServerResponse(false, e.getMessage());
        } catch (InterruptedException e) {
            return new CompileCSharpServerResponse(false, e.getMessage());
        } finally {
            if (process != null) {
                try {
                    process.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                process.destroy();
            }
        }
    }

   private static void close(final InputStream inputStream) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String makeRunScript(File sourcePath,
                                        File revenj,
                                        File target) {
        StringBuilder sb = new StringBuilder("mcs -v");
        String[] systemDependencies = {"System.ComponentModel.Composition", "System", "System.Data", "System.Xml", "System.Runtime.Serialization", "System.Configuration", "System.Drawing"};

        final String targetOutputPath = target.getPath();
        sb.append(" -out:").append(targetOutputPath)
                .append(" -target:library")
                .append(" -lib:").append(revenj.getPath());
        for (String systemDependency : systemDependencies)
            addCSCompileDependency(sb, systemDependency);
        for (File dependency : revenj.listFiles()) /* todo - dereference may cause NPE, unable to test. */
            if (dependency.getName().endsWith(".dll"))
                addCSCompileDependency(sb, dependency.getName());
        sb.append(" -recurse:").append(sourcePath.getAbsolutePath()).append("/*.cs");
        return sb.toString();
    }

    private static void addCSCompileDependency(StringBuilder sb, String dependency) {
        sb.append(" -r:").append(dependency);
    }

    public String getDiff(
            final Map<String, String> olddsl,
            final Map<String, String> newdsl) {

        return DiffProcessor.jGitDiff(olddsl, newdsl);
    }
}
