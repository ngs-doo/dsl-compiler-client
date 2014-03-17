package com.dslplatform.compiler.client.api.core.test.transport;

import com.dslplatform.compiler.client.api.core.HttpRequest;
import com.dslplatform.compiler.client.api.core.HttpResponse;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GenerateMigrationSQLTransportTest extends HttpTransportImplTest {

    @Test
    public void testGenerateMigrationSQLRequestValid() throws IOException {
        final HttpRequest generateMigrationSQLRequest; {
            final String token = userToken(validUser, inValidPassword);
            final String version = "1.0.0.31761";
            final Map<String, String> olddsl = new HashMap<String, String>(){{put("dsl.dsl", "module myModule { root A;}");}};
            final Map<String, String> newdsl = new HashMap<String, String>(){{put("dsl.dsl", "module myModule { root A{ int i;}}");}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(token, version, olddsl, newdsl);
        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(201, response.code);
        assertEquals(Arrays.asList("text/plain; charset=\"utf-8\""), response.headers.get("Content-Type"));
        assertArrayEquals("Project ? not found.".getBytes("UTF-8"), response.body);
    }

    @Test
    public void testGenerateMigrationSQLRequestBadDSL() throws IOException {
        final HttpRequest generateMigrationSQLRequest; {
            final String token = userToken(validUser, inValidPassword);
            final String version = "1.0.0.31761";
            final Map<String, String> olddsl = new HashMap<String, String>(){{put("dsl.dsl", "module myModule { root A;}");}};
            final Map<String, String> newdsl = new HashMap<String, String>(){{put("dsl.dsl", "module myModule { root A{ int !;}}");}};
            generateMigrationSQLRequest = httpRequestBuilder.generateMigrationSQL(token, version, olddsl, newdsl);        }

        final HttpResponse response = httpTransport.sendRequest(generateMigrationSQLRequest);
        assertEquals(400, response.code);
    }
}
