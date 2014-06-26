package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.response.Source;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class IOTest {

    /**
     * Purpose here is to test IO.updateFiles only since all other methods just proxy to corresponding method in FileUtils.
     */
    private static List<Source> firstSources;
    private static List<Source> secondSources;
    private static Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger("dcc-test-io");

    public static File tempPath = new File(System.getProperty("java.io.tmpdir"), String.format("dcc-test%d", random.nextInt(11111)));

    private static final String language1 = "lang1";
    private static final String language2 = "lang2";
    private static final String filename1 = "file1";
    private static final String filename2 = "file2";
    private static final String filenameDiffFirst = "diffFileFirst";
    private static final String filenameDiffSecond = "diffFileSecond";

    static {
        firstSources = new LinkedList<Source>();
        secondSources = new LinkedList<Source>();

        firstSources.add(new Source(language1, filename1, generateRandomBytes()));
        firstSources.add(new Source(language1, filename2, generateRandomBytes()));
        firstSources.add(new Source(language2, filename1, generateRandomBytes()));
        secondSources.addAll(firstSources);
        firstSources.add(new Source(language1, filenameDiffFirst, generateRandomBytes()));
        secondSources.add(new Source(language2, filenameDiffSecond, generateRandomBytes()));
    }

    private static File makeLangFile(File root, Source source) {
        return new File(root, source.language + "/" + source.path);
    }

    @Test
    public void writeFirst() throws IOException {
        IO.updateFiles(logger, firstSources, tempPath);
        assertThat(firstSources, everyItem(exists()));

    }

    @Test
    public void writeSecond() throws IOException {
        IO.updateFiles(logger, secondSources, tempPath);
        assertThat(secondSources, everyItem(exists()));
        final File pathToDeletedFile = makeLangFile(tempPath, firstSources.get(3));
        assertFalse(pathToDeletedFile.exists());
    }

    @AfterClass
    public static void tearDown() {
        FileUtils.deleteQuietly(tempPath);
    }

    private static byte[] generateRandomBytes() {
        final byte [] buffer = new byte[256];
        random.nextBytes(buffer);
        return buffer;
    }

    //----------- Matchers

    static class Exists extends TypeSafeMatcher<Source> {

        public Exists() {
        }

        @Override
        protected boolean matchesSafely(Source source) {
            File file = makeLangFile(tempPath, source);
            return file.exists();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(" file missing");
        }
    }

    public static Matcher<Source> exists() {
        return new Exists();
    }
}
