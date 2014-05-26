package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import com.dslplatform.compiler.client.api.core.mock.MockData;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class GenerateMigrationSQLTransportTest extends HttpTransportImplTest {

    @Test
    public void testGenerateMigrationSQLRequestValid_to1() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("1.dsl", MockData.test_migration_sql_simple_1);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(auth, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        final String responseBodyStr = new String(response.body, "UTF-8");
        logger.info("Response: {}", responseBodyStr);
        assertEquals(200, response.code);
        assertTrue(responseBodyStr.contains("New object A will be created in schema myModule"));
        assertTrue(responseBodyStr.contains("INSERT INTO \"myModule\".\"A\" (\"ID\")"));
        assertEquals(Arrays.asList("application/octet-stream"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateMigrationSQLRequestValid_1to2() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("1.dsl", MockData.test_migration_sql_simple_1);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("2.dsl", MockData.test_migration_sql_simple_2);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(auth, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        final String responseBodyStr = new String(response.body, "UTF-8");
        logger.info("Response: {}", responseBodyStr);
        assertEquals(200, response.code);
        assertTrue(responseBodyStr.contains("New object B will be created in schema myModule"));
        assertTrue(responseBodyStr.contains("INSERT INTO \"myModule\".\"A\" (\"ID\")"));
        assertEquals(Arrays.asList("application/octet-stream"), response.headers.get("Content-Type"));
    }

    @Test
    public void testGenerateMigrationSQLRequestBadDSL() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_1);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_bad);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(auth, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(400, response.code);
    }

    @Test
    public void testGenerateMigrationSQLRequestbadDsl() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_1);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_bad);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(auth, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(400, response.code);
    }
}
