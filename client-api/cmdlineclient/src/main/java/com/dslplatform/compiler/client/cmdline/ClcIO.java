package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.io.DSLLoader;
import com.dslplatform.compiler.client.params.DSL;
import org.apache.commons.codec.Charsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ClcIO implements IO {

    final Logger logger;

    public ClcIO(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void write(File file, byte[] content) throws IOException {
        FileUtils.writeByteArrayToFile(file, content);
    }

    @Override
    public void write(File file, String content, Charset encoding) throws IOException {
        FileUtils.write(file, content, encoding);
    }

    @Override
    public void move(File fromFile, File toFile) throws IOException {
        FileUtils.moveFile(fromFile, toFile);
    }

    @Override
    public void copy(File fromFile, File toFile) throws IOException {
        FileUtils.copyFile(fromFile, toFile);
    }

    @Override
    public void copyToDir(File fromFile, File toFile) throws IOException {
        if (fromFile.isDirectory()) FileUtils.copyDirectory(fromFile, toFile);
        else FileUtils.copyFile(fromFile, new File(toFile, fromFile.getName()));
    }

    @Override
    public void delete(File fileToDelete) {
        FileUtils.deleteQuietly(fileToDelete);
    }

    @Override
    public DSL readDSL(File from) throws IOException {
        return new DSL(new DSLLoader(logger, Charsets.UTF_8).addPath(from.getAbsolutePath()).getDSL());
    }

    @Override
    public void mkdirs(File dir) throws IOException {
        dir.mkdirs();
    }
}
