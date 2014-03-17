package com.dslplatform.compiler.client.api.core.test;

import com.dslplatform.compiler.client.api.core.Migration;
import com.dslplatform.compiler.client.api.core.UnmanagedDSL;
import com.dslplatform.compiler.client.api.core.impl.UnmanagedDSLImpl;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

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
    public void testGetLastDSL() {
        UnmanagedDSL u = new UnmanagedDSLImpl();
        Migration migration = u.getLastUnmanagedDSL(datasource);
        for (Map.Entry<String, String> migraiton_dsl : migration.dsls.entrySet()) {
            System.out.println(migraiton_dsl.getKey());
            System.out.println(migraiton_dsl.getValue());
        }
    }

    @Test
    public void testGetAllDSL() {
        UnmanagedDSL u = new UnmanagedDSLImpl();
        List<Migration> migrations = u.getAllUnmanagedDSL(datasource);

        for (Migration mig : migrations) {
            System.out.println("ordinal: " + mig.ordinal);
            for (Map.Entry<String, String> migraiton_dsl : mig.dsls.entrySet()) {
                System.out.println(migraiton_dsl.getKey());
                System.out.println(migraiton_dsl.getValue());
            }
        }
    }
}
