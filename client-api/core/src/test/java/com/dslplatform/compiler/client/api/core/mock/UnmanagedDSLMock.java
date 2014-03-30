package com.dslplatform.compiler.client.api.core.mock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.model.Migration;

public class UnmanagedDSLMock implements UnmanagedDSL {
    @Override
    public boolean doesUnmanagedDSLExits(final DataSource dataSource) {
        //throw new RuntimeException("Asserting does last migration exist failed.");
        return true;
    }

    @Override
    public List<Migration> getAllUnmanagedDSL(final DataSource dataSource) {
        //throw new RuntimeException("Getting all migration failed.");

        return Arrays.asList(
                migration_1,
                migration_2,
                migration_3);
    }

    @Override
    public Migration getLastUnmanagedDSL(final DataSource dataSource) {
        // throw new RuntimeException("Getting last migration failed.");
        return migration_3;
    }

    @Override
    public void inspectUnmanagedProjectChanges(DataSource dataSource, String version, Map<String, String> dsl) {

    }

    @Override
    public void createUnmanagedProject(String token, DataSource dataSource, String serverName, String applicationName) {

    }

    @Override
    public void upgradeUnmanagedDatabase(DataSource dataSource, List<String> migration) {

    }

    private Map<String, String> dsl_0 = new HashMap<String, String>() {{
        put("dslTest.dsl", dslTest);
    }};

    private Map<String, String> dsl_1 = new HashMap<String, String>() {{
        put("One.dsl", one_1);
        put("Two.dsl", two_1);
    }};

    private Map<String, String> dsl_2 = new HashMap<String, String>() {{
        put("One.dsl", one_2);
        put("Two.dsl", two_2);
    }};

    private final String dslTest = resourceToString("/dsl_2/dslTest.dsl");

    private final String one_1 = resourceToString("/dsl_3/One.dsl");
    private final String two_1 = resourceToString("/dsl_3/Two.dsl");

    private final String one_2 = resourceToString("/dsl_4/One.dsl");
    private final String two_2 = resourceToString("/dsl_4/Two.dsl");

    private final Migration migration_1 = new Migration(1, "version_0", new HashMap<String, String>());
    private final Migration migration_2 = new Migration(2, "version_1", dsl_1);
    private final Migration migration_3 = new Migration(3, "version_2", dsl_2);

    private static String resourceToString(final String resourceName) {
        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(UnmanagedDSLMock.class.getResourceAsStream(resourceName), "UTF-8"));
            for (int c = br.read(); c != -1; c = br.read()) sb.append((char) c);
            return sb.toString();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
