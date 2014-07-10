package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.HttpRequestBuilder;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.HttpTransport;
import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.api.model.Migration;
import com.dslplatform.compiler.client.params.*;
import com.dslplatform.compiler.client.processor.*;
import com.dslplatform.compiler.client.response.*;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApiImpl implements Api {

    private static final String revenjURLformat = "https://github.com/ngs-doo/revenj/releases/download/%s/http-server.zip";
    private final HttpRequestBuilder httpRequestBuilder;
    private final HttpTransport httpTransport;
    private final UnmanagedDSL unmanagedDSL;

    public Logger logger;

    public ApiImpl() throws IOException {
        this(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), new UnmanagedDSLImpl());
    }

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

    /**
     * todo -?
     */

    @Override
    public CreateUnmanagedProjectResponse createUnmanagedProject(
            String token, DataSource dataSource, String serverName, String applicationName) {

        return null; /** todo -? */
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
                generateMigrationSQL(token, "", new HashMap<String, String>(), dsl);
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
        if (dependencies == null || dependencies.isFile())
            new IllegalArgumentException("Must provide a folder in which source build dependencies reside.");
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
        String version = "";
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
        return new CompileCSharpServerProcessor(logger, sourcePath, dependencies, target).process();
    }

    public String getDiff(
            final Map<String, String> olddsl,
            final Map<String, String> newdsl) {

        return DiffProcessor.jGitDiff(olddsl, newdsl);
    }

    @Override
    public CacheRevenjResponse cacheRevenj(
            final RevenjVersion revenjVersion,
            final RevenjPath revenjPath
    ) {
        final File cachePath = revenjPath.revenjPath;
        final String version = revenjVersion.version;
        final String revenjURL = String.format(revenjURLformat, version);
        ZipInputStream zipInputStream = null;
        cachePath.mkdirs();
        try {
            final URL url = new URL(revenjURL);
            final URLConnection urlConnection = url.openConnection();
            zipInputStream = new ZipInputStream(urlConnection.getInputStream());

            while (true) {
                final ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null) break;
                else {
                    final File file = new File(cachePath, zipEntry.getName());
                    if (file.exists()) {
                        logger.warn("{} already exists skipping.", file.getAbsolutePath());
                    } else {
                        file.createNewFile();
                        logger.debug("Unpacking {}.", file.getAbsolutePath());
                        final FileOutputStream fileOutputStream = new FileOutputStream(file);
                        try {
                            IOUtils.copy(zipInputStream, fileOutputStream);
                        } finally {
                            fileOutputStream.close();
                        }
                    }
                }
            }
            return new CacheRevenjResponse(true, String.format("Received revenj {} from {}", version, revenjURL));
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
            return new CacheRevenjResponse(false, e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            return new CacheRevenjResponse(false, e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
            return new CacheRevenjResponse(false, e.getMessage());
        } finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public boolean mingleDatabaseConnectionString(final MonoApplicationPath monoApplicationPath, final DBConnectionString dbConnectionString, final CompilationTargetPath compilationTargetPath) {
        final File revenjConfigPath = new File(monoApplicationPath.monoApplicationPath, "bin/Revenj.Http.exe.config");
        String connectionString = dbConnectionString.dbConnectionString;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(revenjConfigPath);
            /* find appSettings tag */
            NodeList appSettings = doc.getElementsByTagName("appSettings").item(0).getChildNodes();
            boolean cschange = false;
            boolean sachange = false;
            for (int i = 1; i < appSettings.getLength() && !(cschange && sachange); i++) {
                Node tmpNod = appSettings.item(i);
                if (tmpNod.hasAttributes()) {
                    NamedNodeMap namedNodeMap = tmpNod.getAttributes();
                    /* find key with ConnectionString value, and change value */
                    if (!cschange && changeKeyValue(namedNodeMap, "ConnectionString", connectionString))
                        cschange = true;
                    /* find key with ServerAssembly value, and change its value */
                    if (!sachange && changeKeyValue(namedNodeMap, "ServerAssembly", compilationTargetPath.compilationTargetPath.getName()))
                        sachange = true;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(revenjConfigPath);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean changeKeyValue(NamedNodeMap namedNodeMap, String key, String value) {
        Node keyNode = namedNodeMap.getNamedItem("key");
        if (keyNode.getNodeValue().equals(key)) {
            Node valueNode = namedNodeMap.getNamedItem("value");
            valueNode.setNodeValue(value);
            return true;
        }
        return false;
    }

    public boolean makeMonoServer(final MonoApplicationPath monoApplicationPath,
                                  final RevenjPath revenjPath,
                                  final CompilationTargetPath compilationTargetPath,
                                  final DBConnectionString dbConnectionString) {
        final File monoApplicationFile = monoApplicationPath.monoApplicationPath;
        final File monoBin = new File(monoApplicationFile, "bin");
        final File startSh = new File(monoApplicationFile, "start.sh");
        if (!monoApplicationFile.exists()) monoApplicationFile.mkdir();
        new File(monoApplicationFile, "cache").mkdir();
        new File(monoApplicationFile, "logs").mkdir();

        try {
            final InputStream startShIS = getClass().getResourceAsStream("/start.sh");
            IO.copyToDir(revenjPath.revenjPath, monoBin);
            IO.copyToDir(compilationTargetPath.compilationTargetPath, monoBin);
            mingleDatabaseConnectionString(monoApplicationPath, dbConnectionString, compilationTargetPath);
            FileUtils.copyInputStreamToFile(startShIS, startSh);
        } catch (IOException e) {
            logger.error("There was an error copying files to mono location {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
