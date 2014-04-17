package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import com.dslplatform.compiler.client.response.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class ApiImplTest extends MockData {

    private Api api;

    @Before
    public void setUp() {
        try {
            api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), UnmanagedDSLMock.mock_single_integrated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void registerUserTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String email = "some@test.com";

        api.registerUser(email);
    }

    @Test
    public void parseDSLTest() throws IOException {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), null);
        final Map<String, String> dsl = new HashMap<String, String>();
        dsl.put("model.dsl", "module Foo {\n" +
                "\taggregate Bar { String baz; }\n" +
                "}");

        final ParseDSLResponse parseDSLResponse =
                api.parseDSL(Tokenizer.tokenHeader(validUser, validPassword, validId), dsl);
        assertTrue(parseDSLResponse.parsed);
    }

    @Test
    public void parseDSLTestFail() throws IOException {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), null);
        final Map<String, String> dsl = new HashMap<String, String>();
        dsl.put("model.dsl", "module Foo {\n" +
                "\taggregate Bar { String baz; 3}\n" +
                "}");

        final ParseDSLResponse parseDSLResponse = api.parseDSL(projectToken(validUser, validPassword, validId), dsl);

        assertTrue(parseDSLResponse.parseMessage.contains("line 2:"));
        assertFalse(parseDSLResponse.parsed);
    }

    @Test
    public void createTestProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final String projectName = "projectName";

        api.createTestProject(token, projectName);
    }

    @Test
    public void createExternalProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = userToken(validUser, validPassword);
        final String projectName = "NewProjectName";
        final String serverName = "someServerName";
        final String applicationName = "someApplicationName";
        final Map<String, Object> databaseConnection = new LinkedHashMap<String, Object>();
        databaseConnection.put("Server", "someServer");
        databaseConnection.put("Port", 4);
        databaseConnection.put("Database", "someDatabase");
        databaseConnection.put("Username", "someUsername");
        databaseConnection.put("Password", "somePassword");

        api.createExternalProject(token, projectName, serverName, applicationName, databaseConnection);
    }

    @Test
    public void downloadBinariesTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.downloadBinaries(token, UUID.fromString(validId));
    }

    @Test
    public void downloadGeneratedModelTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.downloadGeneratedModel(token, UUID.fromString(validId));
    }

    @Test
    public void inspectManagedProjectChangesTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final Map<String, String> dsl = new LinkedHashMap<String, String>() {{
            put("only", "module A { root B; root C{ B b;}}");
        }};

        final InspectManagedProjectChangesResponse response = api.inspectManagedProjectChanges(token, UUID.fromString(validId), dsl);
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
        assertNotNull(response.changes);
    }

    @Test
    public void getLastManagedDSLTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        final GetLastManagedDSLResponse response = api.getLastManagedDSL(token, UUID.fromString(validId));

        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test
    public void getConfigTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final Set<String> targets = new HashSet<String>() {{
            add("java");
            add("scala");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";

        final GetConfigResponse response = api.getConfig(token, UUID.fromString(validId), targets, packageName, options);
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
        // TODO - deserialize config assertNotNull(response.changes);
    }

    @Test
    public void updateManagedProjectTest() throws IOException {
        final String token = projectToken(validUser, validPassword, validId);
        final Set<String> targets = new HashSet<String>() {{
            add("Java");
            add("Scala");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "name.space";
        final String migration = "migration";
        final Map<String, String> dsl = MockData.dsl_AB;

        final UpdateManagedProjectResponse ump =
                api.updateManagedProject(token, UUID.fromString(validId), targets, packageName, migration,
                        options, dsl);

        assertNull(ump.authorizationErrorMessage);
        assertTrue(ump.sources.containsKey("scala"));
        assertTrue(ump.sources.containsKey("java"));
        assertTrue(ump.sources.get("java").containsKey("/name/space/A/B.java"));
        assertTrue(ump.sources.get("java").containsKey("/name/space/A/C.java"));
        assertTrue(ump.sources.get("scala").containsKey("/name/space/A/B.scala"));
    }

    @Test
    public void generateMigrationSQLTest() throws IOException {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final Map<String, String> olddsl = MockData.dsl_test_migration_single_old;
        final Map<String, String> newdsl = MockData.dsl_test_migration_single_new;
        final String version = MockData.version_real;

        final GenerateMigrationSQLResponse generateMigrationSQLResponse = api.generateMigrationSQL(token, version, olddsl, newdsl);
        assertTrue(generateMigrationSQLResponse.authorized);
        assertNull(generateMigrationSQLResponse.authorizationErrorMessage);
        assertTrue(generateMigrationSQLResponse.migration.contains("New object B will be created in schema myModule"));
    }

    @Test
    public void generateSourcesTest() throws IOException {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final Set<String> targets = new HashSet<String>() {{
            add("Java");
            add("Scala");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("with-active-record");
        }};

        final String packageName = "name.space";

        final GenerateSourcesResponse gsr =
                api.generateSources(token, UUID.fromString(validId), targets, packageName, options);

        assertTrue(gsr.sources.containsKey("scala"));
        assertTrue(gsr.sources.containsKey("java"));
        assertTrue(gsr.sources.get("java").containsKey("/name/space/A/B.java"));
        assertTrue(gsr.sources.get("java").containsKey("/name/space/A/C.java"));
        assertTrue(gsr.sources.get("scala").containsKey("/name/space/A/B.scala"));
    }

    @Test
    public void generateUnmanagedSourcesTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final Set<String> targets = new HashSet<String>() {{
            add("ScalaServer");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";
        final Map<String, String> dsl = new LinkedHashMap<String, String>() {{
            put("only", MockData.ABdsl);
        }};

        final GenerateUnmanagedSourcesResponse generateUnmanagedSourcesResponse =
                api.generateUnmanagedSources(token, packageName, targets, options, dsl);

        assertTrue(generateUnmanagedSourcesResponse.authorized);
        assertTrue(generateUnmanagedSourcesResponse.sources.containsKey("scala"));
        assertTrue(generateUnmanagedSourcesResponse.sources.get("scala").containsKey("/namespace/myModule/IBRepository.scala"));
    }

    @Test
    public void getProjectByNameTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        final GetProjectByNameResponse response = api.getProjectByName(token, "name");
        assertNull(response.authorizationErrorMessage);
        assertTrue(response.authorized);
        assertNotNull(response.project);
    }

    @Test
    public void getAllProjectsTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.getAllProjects(token);
    }

    @Test
    public void renameProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);
        final String oldName = "GreenLeopard";
        final String newName = "GreenLion";

        api.renameProject(token, oldName, newName);
    }

    @Test
    public void cleanProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.cleanProject(token);
    }

    @Test
    public void templateGetTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.templateGet(token, validId, "templateName");
    }

    @Test
    public void templateCreateTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        api.templateCreate(token, "templateName", new byte[0]);
    }

    @Test
    public void templateListAllTest() {
        final String token = projectToken(validUser, validPassword, validId);

        final TemplateListAllResponse response = api.templateListAll(token, UUID.fromString(validId));
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test
    public void templateDeleteTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String token = projectToken(validUser, validPassword, validId);

        final TemplateDeleteResponse response = api.templateDelete(token, "templateName");
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test public void doesUnmanagedDSLExitsTest() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);

        final DoesUnmanagedDSLExitsResponse response = api.doesUnmanagedDSLExits(null);
        assertTrue(response.databaseConnectionSuccessful);
        assertNull(response.databaseConnectionErrorMessage);
    }

    @Test
    public void getLastDSLTest_single() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);

        final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = api.getLastUnmanagedDSL(null);

        assertTrue(getLastUnmanagedDSLResponse.databaseConnectionSuccessful);
        assertTrue(getLastUnmanagedDSLResponse.lastMigration.dsls.containsKey("test.dsl"));
    }

    @Test
    public void getAllDSLTest() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_complex);

        final GetAllUnmanagedDSLResponse getAllUnmanagedDSLResponse = api.getAllUnmanagedDSL(null);

        assertTrue(getAllUnmanagedDSLResponse.databaseConnectionSuccessful);
        assertTrue(getAllUnmanagedDSLResponse.allMigrations.get(2).dsls.containsKey("One.dsl"));
    }

    @Test
    public void inspectUnmanagedProjectChangesTest() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);

        api.inspectUnmanagedProjectChanges(null, "", null);
    }

    @Test
    public void createUnmanagedProjectTest() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);
        final String token = projectToken(validUser, validPassword, validId);

        api.createUnmanagedProject(token, null, "", "");
    }

    @Test
    public void upgradeUnmanagedDatabaseTest() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);

        api.upgradeUnmanagedDatabase(null, null);
    }

    @Test
    public void upgradeUnmanagedServerTest_mockedSingleMigration() throws IOException {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
        //final Api api = new ApiImpl(new HttpRequestBuilderImpl(), HttpTransportProvider.httpTransport(), UnmanagedDSLMock.mock_single_integrated);
        final String token = projectToken(validUser, validPassword, validId);
        final Set<String> targets = new HashSet<String>() {{
            add("ScalaServer");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";
        final Map<String, String> newDsl = MockData.dsl_test_migration_single_new;

        final UpgradeUnmanagedServerResponse upgradeUnmanagedServerResponse = api.upgradeUnmanagedServer(token, null, packageName, targets, options, newDsl);
        assertTrue(upgradeUnmanagedServerResponse.authorized);
        assertNull(upgradeUnmanagedServerResponse.authorizationErrorMessage);
        assertTrue(upgradeUnmanagedServerResponse.migration.contains("New object B will be created in schema myModule"));
        assertTrue(upgradeUnmanagedServerResponse.migration.contains("ALTER TABLE \"myModule\".\"B\" ALTER \"ID\" SET NOT NULL"));
        assertTrue(upgradeUnmanagedServerResponse.serverSource.containsKey("scala"));
        assertTrue(upgradeUnmanagedServerResponse.serverSource.get("scala").containsKey("/namespace/myModule/IBRepository.scala"));
    }
}
