package com.dslplatform.compiler.client.parameters;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;

public class ScalaPathTest {
	private static String fakeScalaPath;

	@BeforeClass
	public static void initPath() throws IOException {
		fakeScalaPath = JavaPathTest.getScriptPath() + "/fake-scala";
	}

	private Context context;

	@Before
	public void initContext() {
		context = new Context();
	}

	@Test
	public void testScalaPathBat() {
		assumeTrue(Utils.isWindows());
		context.put(ScalaPath.INSTANCE, fakeScalaPath + "/bat/scalac.bat");
		assertTrue(ScalaPath.INSTANCE.check(context));
	}

	@Test
	public void testScalaPathEmpty() {
		assumeFalse(Utils.isWindows());
		
		// chmod +x (GitHub doesn't store permissions)
		File scalacFile = new File(fakeScalaPath + "/empty/scalac");
		scalacFile.setExecutable(true);
		
		context.put(ScalaPath.INSTANCE, scalacFile.getAbsolutePath());
		assertTrue(ScalaPath.INSTANCE.check(context));
	}
}
