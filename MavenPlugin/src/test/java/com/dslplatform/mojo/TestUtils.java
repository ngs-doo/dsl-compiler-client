package com.dslplatform.mojo;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestUtils {
    static void assertDir(String path) {
        File dir = new File(path);
        Assert.assertTrue("Does not exist: " + path, dir.exists());
        Assert.assertTrue("Is not a directory: " + path, dir.isDirectory());
    }

    static void assertFile(String path) {
        File file = new File(path);
        Assert.assertTrue("Does not exist: " + path, file.exists());
        Assert.assertTrue("Is not a directory: " + path, file.isFile());
    }

    /**
     * Starts an embedded Postgres on a random free port (binaries are downloaded from Central)
     * and creates the given database in it. Same approach as the Revenj java tests.
     */
    public static EmbeddedPostgres startPostgres(String dbName) throws Exception {
        EmbeddedPostgres postgres = EmbeddedPostgres.builder().setPort(0).start();
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + postgres.getPort() + "/postgres", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE \"" + dbName + "\"");
        }
        return postgres;
    }

    public static String jdbcUrl(EmbeddedPostgres postgres, String dbName) {
        return "jdbc:postgresql://localhost:" + postgres.getPort() + "/" + dbName + "?user=postgres&password=postgres";
    }

    /** Connection string in the format expected by dsl-clc (host:port/db?user=..&password=..) */
    public static String connectionParam(EmbeddedPostgres postgres, String dbName) {
        return "localhost:" + postgres.getPort() + "/" + dbName + "?user=postgres&password=postgres";
    }

    /**
     * Copy of the properties/migration.properties test resource with the postgres
     * connection rewritten to point at the given embedded server.
     */
    public static File migrationProperties(EmbeddedPostgres postgres, String dbName) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = TestUtils.class.getResourceAsStream("/properties/migration.properties")) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
            }
        }
        String content = sb.toString().replaceFirst("(?m)^postgres=.*$", "postgres=" + connectionParam(postgres, dbName));
        File out = new File("target", "migration-" + postgres.getPort() + ".properties");
        Files.write(out.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return out;
    }
}
