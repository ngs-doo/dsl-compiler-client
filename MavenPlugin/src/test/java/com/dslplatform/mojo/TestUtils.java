package com.dslplatform.mojo;

import org.junit.Assert;

import java.io.File;

public class TestUtils {
    static void assertDir(String path) {
        File dir = new File(path);
        Assert.assertTrue("Does not exist: " + path, dir.exists());
        Assert.assertTrue("Is not a directory.", dir.isDirectory());
    }

    static void assertFile(String path) {
        File file = new File(path);
        Assert.assertTrue("Does not exist: " + path, file.exists());
        Assert.assertTrue("Is not a directory.", file.isFile());
    }
}
