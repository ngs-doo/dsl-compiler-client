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
    public void testGenerateMigrationSQLRequestValid() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String token = userToken(validUser, validPassword);
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_old);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_new);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(token, version, olddsl, newdsl);
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
            final String token = userToken(validUser, validPassword);
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_old);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_bad);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(token, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(400, response.code);
    }

    @Test
    public void testGenerateMigrationSQLRequestInvalidPassword() throws IOException {
        final HttpRequest generateMigrationSQLRequest;
        {
            final String token = userToken(validUser, inValidPassword);
            final String version = MockData.version_real;
            final Map<String, String> olddsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_simple_old);}};
            final Map<String, String> newdsl =
                    new HashMap<String, String>() {{put("dsl.dsl", MockData.test_migration_sql_bad);}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(token, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(403, response.code);
    }
}
