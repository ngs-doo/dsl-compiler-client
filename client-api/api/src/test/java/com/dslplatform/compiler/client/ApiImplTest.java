package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.response.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;

public class ApiImplTest extends MockData {

    private Api api;

    private DataSource dataSource;
    
    final String auth = Tokenizer.basicHeader(validUser, validPassword);
    
    @Before
    public void setUp() {
        final DataSource dataSource = null;
        //api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), new UnmanagedDSLImpl());
        api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), UnmanagedDSLMock.mock_single_integrated);
    }

    @Test
    public void registerUserTest() {
        final String email = "some@test.com";

        api.registerUser(email);
    }

    @Test
    public void parseDSLTest() throws IOException {
        final Map<String, String> dsl = new HashMap<String, String>();
        dsl.put("model.dsl", "module Foo {\n" +
                "\taggregate Bar { String baz; }\n" +
                "}");

        final ParseDSLResponse parseDSLResponse =
                api.parseDSL(auth, dsl);
        assertTrue(parseDSLResponse.parsed);
    }

    @Test
    public void parseDSLTestFail() throws IOException {
        final Map<String, String> dsl = new HashMap<String, String>();
        dsl.put("model.dsl", "module Foo {\n" +
                "\taggregate Bar { String baz; 3}\n" +
                "}");

        final ParseDSLResponse parseDSLResponse = api.parseDSL(auth, dsl);

        assertTrue(parseDSLResponse.parseMessage.contains("line 2:"));
        assertFalse(parseDSLResponse.parsed);
    }

    @Test
    public void createTestProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String projectName = "projectName";

        api.createTestProject(auth, projectName);
    }

    @Test
    public void createExternalProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String projectName = "NewProjectName";
        final String serverName = "someServerName";
        final String applicationName = "someApplicationName";
        final Map<String, Object> databaseConnection = new LinkedHashMap<String, Object>();
        databaseConnection.put("Server", "someServer");
        databaseConnection.put("Port", 4);
        databaseConnection.put("Database", "someDatabase");
        databaseConnection.put("Username", "someUsername");
        databaseConnection.put("Password", "somePassword");

        api.createExternalProject(auth, projectName, serverName, applicationName, databaseConnection);
    }

    @Test
    public void downloadBinariesTest() {

        api.downloadBinaries(auth, UUID.fromString(validId));
    }

    @Test
    public void downloadGeneratedModelTest() {

        api.downloadGeneratedModel(auth, UUID.fromString(validId));
    }

    @Test
    public void inspectManagedProjectChangesTest() {
        final Map<String, String> dsl = new LinkedHashMap<String, String>() {{
            put("only", "module A { root B; root C{ B b;}}");
        }};

        final InspectManagedProjectChangesResponse response = api.inspectManagedProjectChanges(auth, UUID.fromString(validId), dsl);
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
        assertNotNull(response.changes);
    }

    @Test
    public void getLastManagedDSLTest() {
        final GetLastManagedDSLResponse response = api.getLastManagedDSL(auth, UUID.fromString(validId));

        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test
    public void getConfigTest() {
        final Set<String> targets = new HashSet<String>() {{
            add("java");
            add("scala");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";

        final GetConfigResponse response = api.getConfig(auth, UUID.fromString(validId), targets, packageName, options);
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
        // TODO - deserialize config assertNotNull(response.changes);
    }

    @Test
    public void updateManagedProjectTest() throws IOException {
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
        final Map<String, String> dsl = MockData.managed_dsl_AB;

        final UpdateManagedProjectResponse ump =
                api.updateManagedProject(auth, UUID.fromString(validId), targets, packageName, migration,
                        options, dsl);

        assertNull(ump.authorizationErrorMessage);

        Source [] sources = ump.sources.toArray(new Source[ump.sources.size()]);

        assertThat(sources, hasItemInArray(containsSource("java", "/name/space/A/B.java")));
        assertThat(sources, hasItemInArray(containsSource("java","/name/space/A/C.java")));
        assertThat(sources, hasItemInArray(containsSource("scala", "/name/space/A/B.scala")));
    }

    @Test
    public void generateMigrationSQLTest() throws IOException {
        final Map<String, String> olddsl = MockData.dsl_test_migration_single_1;
        final Map<String, String> newdsl = MockData.dsl_test_migration_single_2;
        final String version = MockData.version_real;

        final GenerateMigrationSQLResponse generateMigrationSQLResponse = api.generateMigrationSQL(auth, version, olddsl, newdsl);
        assertTrue(generateMigrationSQLResponse.authorized);
        assertNull(generateMigrationSQLResponse.authorizationErrorMessage);
        assertTrue(generateMigrationSQLResponse.migration.contains("New object B will be created in schema myModule"));
    }

    @Test
    public void generateSourcesTest() throws IOException {
        final Set<String> targets = new HashSet<String>() {{
            add("Java");
            add("Scala");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("with-active-record");
        }};

        final String packageName = "name.space";

        final GenerateSourcesResponse gsr =
                api.generateSources(auth, UUID.fromString(validId), targets, packageName, options);

        Source [] sources = gsr.sources.toArray(new Source[gsr.sources.size()]);

        assertThat(sources, hasItemInArray(containsSource("scala", "/name/space/A/B.scala")));
        assertThat(sources, hasItemInArray(containsSource("java", "/name/space/A/B.java")));
        assertThat(sources, hasItemInArray(containsSource("java", "/name/space/A/C.java")));
    }

    @Test
    public void generateUnmanagedSourcesTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        final Set<String> targets = new HashSet<String>() {{
            add("ScalaServer");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";
        final Map<String, String> dsl = new LinkedHashMap<String, String>() {{
            put("only", MockData.managedABdsl);
        }};

        final GenerateUnmanagedSourcesResponse generateUnmanagedSourcesResponse =
                api.generateUnmanagedSources(auth, packageName, targets, options, dsl);

        Source [] sources = generateUnmanagedSourcesResponse.sources.toArray(new Source[generateUnmanagedSourcesResponse.sources.size()]);
        assertTrue(generateUnmanagedSourcesResponse.authorized);
        assertThat(sources, hasItemInArray(containsSource("scala", "/namespace/myModule/IBRepository.scala")));
    }

    @Test
    public void getProjectByNameTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        final GetProjectByNameResponse response = api.getProjectByName(auth, "name");
        assertNull(response.authorizationErrorMessage);
        assertTrue(response.authorized);
        assertNotNull(response.project);
    }

    @Test
    public void getAllProjectsTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        api.getAllProjects(auth);
    }

    @Test
    public void renameProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final String oldName = "GreenLeopard";
        final String newName = "GreenLion";

        api.renameProject(auth, oldName, newName);
    }

    @Test
    public void cleanProjectTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        api.cleanProject(auth);
    }

    @Test
    public void templateGetTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        api.templateGet(auth, validId, "templateName");
    }

    @Test
    public void templateCreateTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        api.templateCreate(auth, validId, "templateName", new byte[0]);
    }

    @Test
    public void templateListAllTest() {
        final TemplateListAllResponse response = api.templateListAll(auth, validId);
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test
    public void templateDeleteTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);

        final TemplateDeleteResponse response = api.templateDelete(auth, validId, "templateName");
        assertTrue(response.authorized);
        assertNull(response.authorizationErrorMessage);
    }

    @Test public void doesUnmanagedDSLExitsTest() {
        final DoesUnmanagedDSLExitsResponse response = api.doesUnmanagedDSLExits(dataSource);
        assertTrue(response.databaseConnectionSuccessful);
        assertNull(response.databaseConnectionErrorMessage);
        assertFalse(response.databaseExists);
    }

    @Test
    public void getLastDSLTest_single() {
        final Api api = new ApiImpl(null, null, UnmanagedDSLMock.mock_single_integrated);

        final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = api.getLastUnmanagedDSL(null);

        assertTrue(getLastUnmanagedDSLResponse.databaseConnectionSuccessful);
        assertTrue(getLastUnmanagedDSLResponse.lastMigration.dsls.containsKey("1.dsl"));
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
        api.createUnmanagedProject(auth, null, "", "");
    }

    @Test
    public void upgradeUnmanagedDatabaseTest() {
        api.upgradeUnmanagedDatabase(null, null);
    }

    @Test
    public void upgradeUnmanagedServerTest_mockedSingleMigration() throws IOException {
        final Set<String> targets = new HashSet<String>() {{
            add("ScalaServer");
        }};
        final Set<String> options = new HashSet<String>() {{
            add("opt1");
            add("opt2");
        }};
        final String packageName = "namespace";
        final Map<String, String> newDsl = MockData.dsl_test_migration_single_2;

        final UpgradeUnmanagedServerResponse upgradeUnmanagedServerResponse = api.upgradeUnmanagedServer(auth, dataSource, packageName, targets, options, newDsl);
        assertTrue(upgradeUnmanagedServerResponse.authorized);
        assertNull(upgradeUnmanagedServerResponse.authorizationErrorMessage);
        assertTrue(upgradeUnmanagedServerResponse.migration.contains("New object B will be created in schema myModule"));
        assertTrue(upgradeUnmanagedServerResponse.migration.contains("ALTER TABLE \"myModule\".\"B\" ALTER \"ID\" SET NOT NULL"));
    }

    //----------- Matchers

    public static class ContainsSource extends TypeSafeMatcher<Source> {
        final String language;
        final String path;
        public ContainsSource(String language, String path) {
            this.language = language;
            this.path = path;
        }

        @Override
        protected boolean matchesSafely(Source item) {
            return item.language == language && item.path == path;
        }


        @Override
        public void describeTo(Description description) {
            description.appendText("Not mention of ").appendText(language);
        }
    }

    public static Matcher<Source> containsSource(String language, String path) {
        return new ContainsSource(language, path);
    }
}
