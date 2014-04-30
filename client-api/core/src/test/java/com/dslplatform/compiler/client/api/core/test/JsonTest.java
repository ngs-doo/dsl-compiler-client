package com.dslplatform.compiler.client.api.core.test;

import com.dslplatform.compiler.client.api.json.JsonReader;
import com.dslplatform.compiler.client.api.json.JsonWriter;
import com.dslplatform.compiler.client.api.model.ApplicationServer;
import com.dslplatform.compiler.client.api.model.DatabaseConnection;
import com.dslplatform.compiler.client.api.model.Project;
import com.dslplatform.compiler.client.api.model.json.ProjectJsonDeserialization;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class JsonTest {

    @Test
    public void stringArrayTest() throws Exception {
        String[] original = { "a", "b", "%", "#", "1" };
        final String json = new JsonWriter().write(original).toString();

        assertEquals("[\"a\",\"b\",\"%\",\"#\",\"1\"]", json);

        final ArrayList<String> deserialized = new JsonReader(new StringReader(json)).readStringArray();
        assertArrayEquals(original, deserialized.toArray());
    }

    @Test
    public void projectListDeserialization() throws Exception {

        final String json =
                "[{\"ID\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-04-01T13:13:57.480884+02:00\",\"DatabaseServer\":\"someExternalPlace\",\"DatabasePort\":5432,\"DatabaseName\":\"DatabaseNameValue\",\"ApplicationServer\":\"test.dsl-platform.com\",\"ApplicationName\":\"ApplicationNameValue0\",\"ApplicationPoolName\":\"ApplicationPoolName0\",\"Nick\":\"BrownBison\",\"URI\":\"40443b90-44d8-48ee-b65b-0992cf642637\",\"UserURI\":\"user@domain.omm\"},{\"ID\":\"6bff118e-0ad9-4aee-813d-b292df9b9291\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-03-12T16:48:58.740447+01:00\",\"DatabaseServer\":\"10.5.13.1\",\"DatabasePort\":5432,\"DatabaseName\":\"dccTest\",\"ApplicationServer\":\"\",\"ApplicationName\":\"\",\"ApplicationPoolName\":\"ExternalApplicationPoolName\",\"Nick\":\"dccRobiTest\",\"CustomDatabaseAdmin\":\"dccTest\",\"CustomDatabasePassword\":\"testingTest3\",\"IsExternal\":true,\"URI\":\"6bff118e-0ad9-4aee-813d-b292df9b9291\",\"UserURI\":\"user@domain.omm\"},{\"ID\":\"87ef8769-56bf-4e9b-a90a-ecf3b24d5fc7\",\"UserID\":\"user@domain.omm\",\"CreatedAt\":\"2014-04-01T13:13:42.757042+02:00\",\"DatabaseServer\":\"10.5.6.1\",\"DatabasePort\":5432,\"DatabaseName\":\"beta_b0326548084b05a80a79ba\",\"ApplicationServer\":\"test.dsl-platform.com\",\"ApplicationName\":\"ApplicationNameValue1\",\"ApplicationPoolName\":\"ApplicationPoolNameValue1\",\"Nick\":\"RedPigeon\",\"URI\":\"87ef8769-56bf-4e9b-a90a-ecf3b24d5fc7\",\"UserURI\":\"user@domain.omm\"}]";

        List<Project> projectList = ProjectJsonDeserialization.fromJsonArray(new JsonReader(new StringReader(json)));

        Project[] expected = {
                new Project(
                        UUID.fromString("40443b90-44d8-48ee-b65b-0992cf642637"),
                        "user@domain.omm",
                        new DateTime("2014-04-01T13:13:57.480884+02:00"),
                        "BrownBison",
                        new ApplicationServer(
                                "test.dsl-platform.com",
                                "ApplicationNameValue0",
                                "ApplicationPoolName0"),
                        null
                ),
                new Project(
                        UUID.fromString("6bff118e-0ad9-4aee-813d-b292df9b9291"),
                        "user@domain.omm",
                        new DateTime("2014-03-12T16:48:58.740447+01:00"),
                        "dccRobiTest",
                        new ApplicationServer(
                                "",
                                "",
                                "ExternalApplicationPoolName"
                        ),
                        new DatabaseConnection(
                                "10.5.13.1",
                                5432,
                                "dccTest",
                                "dccTest",
                                "testingTest3".toCharArray())
                ),
                new Project(
                        UUID.fromString("87ef8769-56bf-4e9b-a90a-ecf3b24d5fc7"),
                        "user@domain.omm",
                        new DateTime("2014-04-01T13:13:42.757042+02:00"),
                        "RedPigeon",
                        new ApplicationServer(
                                "test.dsl-platform.com",
                                "ApplicationNameValue1",
                                "ApplicationPoolNameValue1"),
                        null
                )
        };

        assertArrayEquals(expected, projectList.toArray());
    }
}
