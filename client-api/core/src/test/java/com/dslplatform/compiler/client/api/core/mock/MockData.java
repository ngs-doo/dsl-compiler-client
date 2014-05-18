package com.dslplatform.compiler.client.api.core.mock;

import com.dslplatform.compiler.client.api.config.Tokenizer;
import com.dslplatform.compiler.client.api.json.JsonWriter;
import com.dslplatform.compiler.client.api.model.Migration;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MockData {

    //public static final String validUser = "ocd@dsl-platform.com";
    public static final String validUser = "rinmalavi@gmail.com";
    //public static final String validPassword = "ocdcdo";
    public static final String validPassword = "qwe321";

    public static final String inValidUser = "cdo@dsl-platform.com";
    public static final String inValidPassword = "cdoodc";

    public static final String validId = "6bff118e-0ad9-4aee-813d-b292df9b9291";

    //public static final String validId = "d7b8ee4d-2226-49b5-a5aa-74af1d29e644";
    public static final String inValidID = "c3cc0007-5dc2-4fd0-b14b-0dcbfce1e4bf";

    public static final String version_real = "1.0.1.24037";
    public static final Charset ENCODING = Charset.forName("UTF-8");

    public static final String test_migration_sql_simple_1 = resourceToString("/test_migration_sql_simple/1.dsl");
    public static final String test_migration_sql_simple_2 = resourceToString("/test_migration_sql_simple/2.dsl");
    public static final Map<String, String> dsl_test_migration_single_1 = new HashMap<String, String>() {{put("1.dsl",
            test_migration_sql_simple_1);}};
    public static final Map<String, String> dsl_test_migration_single_2 = new HashMap<String, String>() {{put("2.dsl",
            test_migration_sql_simple_2);}};
    public static final String test_migration_sql_bad = resourceToString("/test_migration_sql_simple/bad.dsl");
    public static final byte[] test_migration_sql_response_1to2 = resourceToBytes(
            "/test_migration_sql_simple/SQLMigration_1to2.response");
    public static final byte[] test_migration_sql_response_to1 = resourceToBytes(
            "/test_migration_sql_simple/SQLMigration_to1.response");

    public static final byte[] test_migration_serversource_scalaserver_response_2 = resourceToBytes(
            "/test_migration_sql_simple/ServerSource_ScalaServer_2.response");
    public static final byte[] test_migration_serversource_cs_response_2 = resourceToBytes(
            "/test_migration_sql_simple/ServerSource_CS_2.response");
    public static final byte[] test_migration_serversource_cs_j_response_2 = resourceToBytes(
            "/test_migration_sql_simple/ServerSource_CS_J_2.response");
    public static byte[] test_migration_serversource_cs_s_response_2 = resourceToBytes(
            "/test_migration_sql_simple/ServerSource_CS_S_2.response");
    public static final Migration migration_test_migration_single = new Migration(3, version_real,
            dsl_test_migration_single_1);

    public static final Map<String, String> dsl_0 = new HashMap<String, String>() {{put("AB.dsl", dslTest);}};
    public static final Map<String, String> dsl_1 = new HashMap<String, String>() {{put("One.dsl", one_1); put("Two.dsl", two_1); }};
    public static final Map<String, String> dsl_2 = new HashMap<String, String>() {{put("One.dsl", one_2); put("Two.dsl", two_2); }};
    public static final String dslTest = MockData.resourceToString("/dsl_2/dslTest.dsl");
    public static final String one_1 = MockData.resourceToString("/dsl_3/One.dsl");
    public static final String two_1 = MockData.resourceToString("/dsl_3/Two.dsl");
    public static final String one_2 = MockData.resourceToString("/dsl_4/One.dsl");
    public static final String two_2 = MockData.resourceToString("/dsl_4/Two.dsl");
    public static final String one_last = MockData.resourceToString("/dsl_5_after_last/One.dsl");
    public static final String two_last = MockData.resourceToString("/dsl_5_after_last/Two.dsl");
    public static final Migration migration_1 = new Migration(1, "version_0", new HashMap<String, String>());
    public static final Migration migration_2 = new Migration(2, "version_1", dsl_1);
    public static final Migration migration_3 = new Migration(3, "version_2", dsl_2);
    public static final Map<String, String> migrate_with = new HashMap<String, String>() {{put("One.dsl", one_last); put("Two.dsl", two_last);}};

    public static final String ABdsl = resourceToString("/test_AB/AB.dsl");
    public static final String ABChangeddsl = resourceToString("/test_AB/AB+.dsl");
    public static final Map<String, String> dsl_AB = new HashMap<String, String>() {{put("AB.dsl", ABdsl);}};
    public static final Map<String, String> dsl_changed_AB = new HashMap<String, String>() {{put("AB.dsl", ABChangeddsl);}};
    public static final String ABresponse = resourceToString("/test_AB/AB.response");
    public static final byte[] ABresponseBytes = resourceToBytes("/test_AB/AB.response");

    public static final byte[] template_1 = resourceToBytes("/template/simple_temp.txt");

    public static final byte[] getProjectsResponse = "[{\"ID\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-04-01T13:13:57.480884+02:00\",\"DatabaseServer\":\"someExternalPlace\",\"DatabasePort\":5432,\"DatabaseName\":\"DatabaseNameValue\",\"ApplicationServer\":\"test.dsl-platform.com\",\"ApplicationName\":\"ApplicationNameValue0\",\"ApplicationPoolName\":\"ApplicationPoolName0\",\"Nick\":\"BrownBison\",\"URI\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserURI\":\"user@domain.omm\"},{\"ID\":\"6bff118e-0ad9-4aee-813d-b292df9b9291\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-03-12T16:48:58.740447+01:00\",\"DatabaseServer\":\"10.5.13.1\",\"DatabasePort\":5432,\"DatabaseName\":\"dccTest\",\"ApplicationServer\":\"\",\"ApplicationName\":\"\",\"ApplicationPoolName\":\"ExternalApplicationPoolName\",\"Nick\":\"dccRobiTest\",\"CustomDatabaseAdmin\":\"dccTest\",\"CustomDatabasePassword\":\"testingTest3\",\"IsExternal\":true,\"URI\":\"6bff118e-0ad9-4aee-813d-b292df9b9291\",\"UserURI\":\"user@domain.omm\"},{\"ID\":\"87ef8769-56bf-4e9b-a90a-ecf3b24d5fc7\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-04-01T13:13:42.757042+02:00\",\"DatabaseServer\":\"10.5.6.1\",\"DatabasePort\":5432,\"DatabaseName\":\"beta_b0326548084b05a80a79ba\",\"ApplicationServer\":\"test.dsl-platform.com\",\"ApplicationName\":\"ApplicationNameValue1\",\"ApplicationPoolName\":\"ApplicationPoolNameValue1\",\"Nick\":\"RedPigeon\",\"URI\":\"87ef8769-56bf-4e9b-a90a-ecf3b24d5fc7\",\"UserURI\":\"user@domain.omm\"}]".getBytes();


    public static final String projectToken(
            final String user,
            final String pass,
            final String projectid) {
        return Tokenizer.tokenHeader(user, pass, projectid);
    }

    public static final String userToken(
            final String user,
            final String pass) {
        return Tokenizer.userTokenHeader(user, pass);
    }

    public static String resourceToString(final String resourceName) {
        StringBuffer sb = new StringBuffer();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(UnmanagedDSLMock.class.getResourceAsStream(resourceName), "UTF-8"));
            for (int c = br.read(); c != -1; c = br.read()) sb.append((char) c);
            return sb.toString();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static byte[] resourceToBytes(final String resourceName) {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        final InputStream inputStream = UnmanagedDSLMock.class.getResourceAsStream(resourceName);
        byte[] buffer = new byte[512];
        int leng;
        try {
            while ((leng = inputStream.read(buffer)) != -1) {
                bais.write(buffer, 0, leng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bais.toByteArray();
    }

    public static byte[] serialize(final Object body) {
        final JsonWriter jb = new JsonWriter();
        if (body instanceof Map) {
            jb.write((Map<String, Object>) body);
        }
        return jb.toString().getBytes(Charset.forName("UTF-8"));
    }

    public static String userToken() {
        return userToken(validUser, validPassword);
    }

    public static String projectToken() {
        return projectToken(validUser, validPassword, validId);
    }
}
