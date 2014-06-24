package com.dslplatform.compiler.client.cmdline.tools;

import com.dslplatform.compiler.client.api.core.mock.MockData;
import com.dslplatform.compiler.client.cmdline.IO;
import com.dslplatform.compiler.client.params.DSL;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class IOMock implements IO {

    public List<MockedIO> stack;

    public IOMock() {
        stack = new LinkedList<MockedIO>();
    }

    @Override
    public void write(File file, byte[] content) throws IOException {
        stack.add(mockedIO(MockedAction.Write, file, content));
    }

    @Override
    public void write(File file, String content, Charset encoding) throws IOException {
        stack.add(mockedIO(MockedAction.Write, file, content.getBytes(encoding)));
    }

    @Override
    public void move(File fromFile, File toFile) throws IOException {
        stack.add(mockedIO(MockedAction.Move, fromFile, toFile));
    }

    @Override
    public void copy(File fromFile, File toFile) throws IOException {
        stack.add(mockedIO(MockedAction.Copy, fromFile, toFile));
    }

    @Override
    public void delete(File fileToDelete) {
        stack.add(mockedIO(MockedAction.Delete, fileToDelete));
    }

    public static enum MockedAction {
        Copy, Move, Delete, Write
    }

    @Override
    public DSL readDSL(File from) throws IOException {
        return new DSL(MockData.dsl_test_migration_single_2);
    }

    public boolean contains(MockedIO contained) {
        for (MockedIO stacked : stack) {
            if (contained.equals(stacked)) return true;
        }

        return false;
    }

    public boolean contains(MockedAction mockedAction) {
        for (MockedIO stacked : stack) {
            if (stacked.mockedAction.equals(mockedAction)) return true;
        }
        return false;
    }

    public boolean contains(MockedAction mockedAction, File from) {
        for (MockedIO stacked : stack) {
            if (stacked.mockedAction.equals(mockedAction) && stacked.from.equals(from)) return true;
        }
        return false;
    }


    public static MockedIO mockedIO(MockedAction mockedAction) {
        return new MockedIO(mockedAction, null, null, null);
    }

    public static MockedIO mockedIO(MockedAction mockedAction, File from) {
        return new MockedIO(mockedAction, from, null, null);
    }

    public static MockedIO mockedIO(MockedAction mockedAction, File from, File to) {
        return new MockedIO(mockedAction, from, null, null);
    }

    public static MockedIO mockedIO(MockedAction mockedAction, File from, byte[] content) {
        return new MockedIO(mockedAction, from, null, content);
    }

    public static class MockedIO {
        public final MockedAction mockedAction;
        public final File from;
        public final File to;
        public final byte[] content;


        public MockedIO(MockedAction mockedAction, File from, File to, byte[] content) {
            this.mockedAction = mockedAction;
            this.from = from;
            this.to = to;
            this.content = content;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MockedIO)) return false;
            if (obj == this) return true;
            MockedIO other = (MockedIO) obj;
            return (mockedAction == null || mockedAction.equals(other.mockedAction)) &&
                    (from == null || from.equals(other.from)) &&
                    (to == null || to.equals(other.to)) &&
                    (content == null || content.equals(other.content));
        }
    }
}
