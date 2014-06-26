package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.api.core.mock.MockData;
import com.dslplatform.compiler.client.params.RevenjPath;
import com.dslplatform.compiler.client.params.RevenjVersion;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class CacheRevenjTest extends MockData {

    final File dccTestRevenjPath = new File(System.getProperty("java.io.tmpdir"), "dcc-test/revenj");
    @Test
    public void testCopy_v_1_0_1_Test() {
        RevenjVersion version = new RevenjVersion("1.0.1");
        Api api = new ApiImpl(null, null, null);
        final RevenjPath revenjPath = new RevenjPath(new File(dccTestRevenjPath, version.version));
        api.cacheRevenj(version, revenjPath);
    }
}
