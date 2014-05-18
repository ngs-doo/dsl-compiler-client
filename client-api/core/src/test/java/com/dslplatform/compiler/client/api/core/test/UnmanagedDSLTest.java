package com.dslplatform.compiler.client.api.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import com.dslplatform.compiler.client.api.model.Migration;

public class UnmanagedDSLTest {

    private DataSource datasource = new PGSimpleDataSource() {{
        setServerName("localhost");
        setPortNumber(5432);
        setDatabaseName("dccTest");
        setUser("dccTest");
        setPassword("testingTest3");
        setSsl(false);
    }};

    @Test
    public void testGetLastDSL() throws SQLException {
        UnmanagedDSL u = new UnmanagedDSLImpl();
        Migration migration = u.getLastUnmanagedDSL(datasource);
        assertTrue(migration.dsls.get("One.dsl") != null);
        assertTrue(migration.dsls.get("Two.dsl") != null);
    }

    @Test
    public void testGetAllDSL() throws SQLException {
        UnmanagedDSL u = new UnmanagedDSLImpl();
        List<Migration> migrations = u.getAllUnmanagedDSL(datasource);

        final Migration thirdMigration = migrations.get(3);
        assertEquals(4, migrations.get(3).ordinal);
        assertTrue(thirdMigration.dsls.containsKey("One.dsl"));
    }
}
