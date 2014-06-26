package com.dslplatform.compiler.client.cmdline;

import com.dslplatform.compiler.client.params.DSL;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public interface IO {
    public void write(File file, byte[] content) throws IOException;

    public void write(File file, String content, Charset encoding) throws IOException;

    public void move(File fromFile, File toFile) throws IOException;

    public void copy(File fromFile, File toFile) throws IOException;

    public void copyToDir(File fromFile, File toFile) throws IOException;

    public void mkdirs(File dir) throws IOException;

    public void delete(File fileToDelete);

    public DSL readDSL(File from) throws IOException;
}