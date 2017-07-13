package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.DslCompilerMonoRuntimeDetectionTest;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TestUtils {
    public static String fileContent(String filename) {
        try {
            InputStream inputStream = DslCompilerMonoRuntimeDetectionTest.class.getResourceAsStream(filename);
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();
            while (result != -1) {
                buf.write((byte) result);
                result = bis.read();
            }
            return buf.toString("UTF-8");
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
