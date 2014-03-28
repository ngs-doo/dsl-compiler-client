package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.impl.HttpRequestBuilderImpl;
import com.dslplatform.compiler.client.api.core.mock.HttpTransportMock;
import com.dslplatform.compiler.client.api.core.mock.UnmanagedDSLMock;
import com.dslplatform.compiler.client.api.core.test.MockUser;
import com.dslplatform.compiler.client.response.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ApiImplTest extends MockUser {

    @Test
    public void registerUserTest() {
    }

    @Test
    public void parseDslTest() {
        final Api api = new ApiImpl(new HttpRequestBuilderImpl(), new HttpTransportMock(), null);
        final Map<String, String> dsl = new HashMap<String, String>();
        dsl.put("model.dsl", "module Foo {\n" +
                "\taggregate Bar { String baz; }\n" +
                "}");

        final ParseDSLResponse parseDSLResponse = api.parseDsl(projectToken(validUser, validPassword, validID), dsl);

    }

    @Test
    public void createTestProjectTest() {

    }

    @Test
    public void createExternalProjectTest() {

    }

    @Test
    public void downloadBinariesTest() {

    }

    @Test
    public void downloadGeneratedModelTest() {

    }

    @Test
    public void inspectManagedProjectChangesTest() {

    }

    @Test
    public void getLastManagedDSLTest() {

    }

    @Test
    public void getConfigTest() {

    }

    @Test
    public void updateManagedProjectTest() {

    }

    @Test
    public void generateMigrationSQLTest() {

    }

    @Test
    public void generateSourcesTest() {

    }

    @Test
    public void generateUnmanagedSourcesTest() {

    }

    @Test
    public void getProjectByNameTest() {

    }

    @Test
    public void getAllProjectsTest() {

    }

    @Test
    public void renameProjectTest() {

    }

    @Test
    public void cleanProjectTest() {

    }

    @Test
    public void templateGetTest() {

    }

    @Test
    public void templateCreateTest() {

    }

    @Test
    public void templateListAllTest() {

    }

    @Test
    public void templateDeleteTest() {

    }

    @Test public void doesUnmanagedDSLExitsTest() {

    }

    @Test
    public void getLastDSLTest() {
        final Api api = new ApiImpl(null, null, new UnmanagedDSLMock());
        final GetLastUnmanagedDSLResponse getLastUnmanagedDSLResponse = api.getLastUnmanagedDSL(null);
        assertTrue(getLastUnmanagedDSLResponse.isDatabaseConnectionSuccessful());
        assertTrue(getLastUnmanagedDSLResponse.getLastMigration().dsls.containsKey("One.dsl"));
    }

    @Test
    public void getAllDSLTest() {
        final Api api = new ApiImpl(null, null, new UnmanagedDSLMock());
        final GetAllUnmanagedDSLResponse getAllUnmanagedDSLResponse = api.getAllUnmanagedDSL(null);
        assertTrue(getAllUnmanagedDSLResponse.isDatabaseConnectionSuccessful());
        assertTrue(getAllUnmanagedDSLResponse.getAllMigrations().get(3).dsls.containsKey("One.dsl"));
    }

    @Test
    public void inspectUnmanagedProjectChangesTest() {

    }

    @Test
    public void createUnmanagedProjectTest() {

    }

    @Test
    public void upgradeUnmanagedDatabaseTest() {

    }

}
