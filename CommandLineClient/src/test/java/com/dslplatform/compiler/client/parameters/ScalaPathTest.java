package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

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
		context.put(ScalaPath.INSTANCE, fakeScalaPath + "/empty/scalac");
		assertTrue(ScalaPath.INSTANCE.check(context));
	}
}
