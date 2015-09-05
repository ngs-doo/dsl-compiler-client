package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.Mono;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilsTest {
	private static Context context;
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
		context = new Context();
		// context.put(LogOutput.INSTANCE, null);

		fakeJavaFolder = new File(getScriptPath(), "fake-java");
		assertTrue(fakeJavaFolder.exists());

		expected = "Usage: javac" + System.getProperty("line.separator");
	}

	private static Either<Utils.CommandResult> runInJavac(final String command, final String... arguments) {
		return Utils.runCommand(context, command, fakeJavaFolder, Arrays.asList(arguments));
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	public void testBatDirectly() {
		final File javacBat = new File(fakeJavaFolder, "bat/javac.bat");
		assertTrue(javacBat.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacBat.getPath());
		assertEquals(Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testEmptyDirectly() {
		final File javacEmpty = new File(fakeJavaFolder, "empty/javac");
		assertTrue(javacEmpty.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacEmpty.getPath());
		assertEquals(!Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testExeDirectly() {
		final File javacExe = new File(fakeJavaFolder, "exe/javac.exe");
		assertTrue(javacExe.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacExe.getPath());
		assertEquals(Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testNetDirectly() {
		final File javacNet = new File(fakeJavaFolder, "net/javac.exe");
		assertTrue(javacNet.exists());

		if (Utils.isWindows()) {
			final Either<Utils.CommandResult> result = runInJavac(javacNet.getPath());
			assertTrue(result.isSuccess());
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
		else if (Mono.INSTANCE.check(context)) {
			final Either<Utils.CommandResult> result = runInJavac("mono", javacNet.getPath());
			assertTrue(result.isSuccess());
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testShDirectly() {
		final File javacSh = new File(fakeJavaFolder, "sh/javac.sh");
		assertTrue(javacSh.exists());

		final Either<Utils.CommandResult> result = runInJavac(javacSh.getPath());
		assertEquals(!Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	public void testBatNoExtension() {
		final File javacBat = new File(fakeJavaFolder, "bat/javac");
		assertFalse(javacBat.exists()); // does not exist!

		// %ComSpec% is required for running .bat files without specifying their extension
		final Either<Utils.CommandResult> result = runInJavac("cmd", "/c", javacBat.getPath());
		assertEquals(Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testExeNoExtension() {
		final File javacExe = new File(fakeJavaFolder, "exe/javac");
		assertFalse(javacExe.exists()); // does not exist!

		final Either<Utils.CommandResult> result = runInJavac(javacExe.getPath());
		assertEquals(Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}

	@Test
	public void testNetNoExtension() {
		final File javacNet = new File(fakeJavaFolder, "net/javac");
		assertFalse(javacNet.exists()); // does not exist!

		final Either<Utils.CommandResult> result = runInJavac(javacNet.getPath());
		assertEquals(Utils.isWindows(), result.isSuccess());
		if (result.isSuccess()) {
			assertEquals(expected, result.get().output);
			assertEquals("", result.get().error);
		}
	}
}
