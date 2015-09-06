package com.dslplatform.compiler.client;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class UtilsTest {
	private static File fakeJavaFolder;
	private static String expected;

	public static String getScriptPath() {
		final String testClasses = UtilsTest.class.getResource("/").getPath();
		final File scripts = new File(testClasses + "/../../src/test/scripts");
		try {
			return scripts.getCanonicalPath();
		}
		catch (final IOException e) {
			return scripts.getAbsolutePath();
		}
	}

	@BeforeClass
	public static void initPath() throws IOException {
		fakeJavaFolder = new File(getScriptPath(), "fake-java");
		assertTrue(fakeJavaFolder.exists());

		expected = "Usage: javac" + System.getProperty("line.separator");
	}

	private Context context;

	@Before
	public void assumeWindows() {
		context = new Context();
		// context.put(LogOutput.INSTANCE, null);
	}

	private Either<Utils.CommandResult> runInJavac(final String command, final String... arguments) {
		return Utils.runCommand(context, command, fakeJavaFolder, Arrays.asList(arguments));
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	public void testExeDirectly() {
		assumeTrue(Utils.isWindows());
		final File javacExe = new File(fakeJavaFolder, "exe/javac.exe");
		assertTrue(javacExe.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacExe.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	@Test
	public void testCmdDirectly() {
		assumeTrue(Utils.isWindows());
		final File javacCmd = new File(fakeJavaFolder, "cmd/javac.cmd");
		assertTrue(javacCmd.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacCmd.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}
	
	@Test
	public void testBatDirectly() {
		assumeTrue(Utils.isWindows());
		final File javacBat = new File(fakeJavaFolder, "bat/javac.bat");
		assertTrue(javacBat.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacBat.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	@Test
	public void testShDirectly() {
		assumeFalse(Utils.isWindows());
		final File javacSh = new File(fakeJavaFolder, "sh/javac.sh");
		assertTrue(javacSh.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacSh.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	@Test
	public void testEmptyDirectly() {
		assumeFalse(Utils.isWindows());
		final File javacEmpty = new File(fakeJavaFolder, "empty/javac");
		assertTrue(javacEmpty.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacEmpty.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	public void testExeNoExtension() {
		assumeTrue(Utils.isWindows());
		final File javacExe = new File(fakeJavaFolder, "exe/javac");
		assertFalse(javacExe.exists()); // does not exist!

		final Either<Utils.CommandResult> result = runInJavac(javacExe.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	@Test
	public void testCmdNoExtension() {
		assumeTrue(Utils.isWindows());
		final File javacCmd = new File(fakeJavaFolder, "cmd/javac");
		assertFalse(javacCmd.exists()); // does not exist!

		// %ComSpec% is required for running .bat files without specifying their extension
		final Either<Utils.CommandResult> result = runInJavac("cmd", "/c", javacCmd.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}

	@Test
	public void testBatNoExtension() {
		assumeTrue(Utils.isWindows());
		final File javacBat = new File(fakeJavaFolder, "bat/javac");
		assertFalse(javacBat.exists()); // does not exist!

		// %ComSpec% is required for running .bat files without specifying their extension
		final Either<Utils.CommandResult> result = runInJavac("cmd", "/c", javacBat.getPath());
		assertTrue(result.isSuccess());
		assertEquals(expected, result.get().output);
		assertEquals("", result.get().error);
	}
}
