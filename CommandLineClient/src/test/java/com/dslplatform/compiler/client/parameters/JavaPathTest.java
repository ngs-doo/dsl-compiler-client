package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class JavaPathTest {
	private static String fakeJavaPath;
	private static File fakeJavaFolder;

	public static String getScriptPath() {
		final String testClasses = JavaPathTest.class.getResource("/").getPath();
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
		fakeJavaPath = getScriptPath() + "/fake-java";
	}

	private Context context;

	@Before
	public void initContext() {
		context = new Context();
	}

	@Test
	public void testJavaPathBat() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/bat");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathBatAndFolder() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/bat-and-folder");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathBatAndEmpty() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/bat-and-empty");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathCmd() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/cmd");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathCmdAndFolder() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/cmd-and-folder");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathCmdAndEmpty() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/cmd-and-empty");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathEmpty() {
		assumeFalse(Utils.isWindows());

		// chmod +x (GitHub doesn't store permissions)
		File emptyJavaPath = new File(fakeJavaPath, "empty");
		File javacFile = new File(emptyJavaPath, "javac");
		File jarFile = new File(emptyJavaPath, "jar");
		javacFile.setExecutable(true);
		jarFile.setExecutable(true);

		context.put(JavaPath.INSTANCE, emptyJavaPath.getAbsolutePath());
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Before
	public void assumeWindows() {
		context = new Context();
	}

	@Test
	public void testSh() {
		assumeFalse(Utils.isWindows());
		final File javacSh = new File(fakeJavaFolder, "sh/javac.sh");
		assertTrue(javacSh.exists());
		context.put(JavaPath.INSTANCE, new File(fakeJavaFolder, "sh").getPath());
		assertTrue(JavaPath.findCompiler(context).isSuccess());
	}

	@Test
	public void testEmpty() {
		assumeFalse(Utils.isWindows());
		final File javacEmpty = new File(fakeJavaFolder, "empty/javac");
		assertTrue(javacEmpty.exists());
		context.put(JavaPath.INSTANCE, new File(fakeJavaFolder, "empty").getPath());
		assertTrue(JavaPath.findCompiler(context).isSuccess());
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	public void testCmd() {
		assumeTrue(Utils.isWindows());
		final File javacCmd = new File(fakeJavaFolder, "cmd/javac");
		assertFalse(javacCmd.exists()); // does not exist!
		context.put(JavaPath.INSTANCE, new File(fakeJavaFolder, "cmd").getPath());
		assertTrue(JavaPath.findCompiler(context).isSuccess());
	}

	@Test
	public void testBat() {
		assumeTrue(Utils.isWindows());
		final File javacBat = new File(fakeJavaFolder, "bat/javac");
		assertFalse(javacBat.exists()); // does not exist!
		context.put(JavaPath.INSTANCE, new File(fakeJavaFolder, "bat").getPath());
		assertTrue(JavaPath.findCompiler(context).isSuccess());
	}
}
