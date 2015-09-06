package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.UtilsTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

public class JavaPathTest {
	private static String fakeJavaPath;

	@BeforeClass
	public static void initPath() throws IOException {
		fakeJavaPath = UtilsTest.getScriptPath() + "/fake-java";
	}

	private Context context;

	@Before
	public void initContext() {
		context = new Context();
		// context.put(LogOutput.INSTANCE, null);
	}

	@Test
	public void testJavaPathExe() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/exe");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathExeAndFolder() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/exe-and-folder");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	@Test
	public void testJavaPathExeAndEmpty() {
		assumeTrue(Utils.isWindows());
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/exe-and-empty");
		assertTrue(JavaPath.INSTANCE.check(context));
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
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/empty");
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	// TODO: Test JAVA_HOME/JDK_HOME environment settings by spawning a new process?
}
