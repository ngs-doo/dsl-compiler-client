package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.UtilsTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ScalaPathTest {
	private static String fakeScalaPath;

	@BeforeClass
	public static void initPath() throws IOException {
		fakeScalaPath = UtilsTest.getScriptPath() + "/fake-scala";
	}

	@Test
	public void testJavaPathParameter() {
		final Context context = new Context();
		// context.put(LogOutput.INSTANCE, null);
		final String scriptPath = Utils.isWindows() ? "bat/scalac.bat" : "empty/scalac";
		context.put(ScalaPath.INSTANCE, fakeScalaPath + "/" + scriptPath);
		assertTrue(ScalaPath.INSTANCE.check(context));
	}

	// TODO: Test SCALA_HOME environment setting by spawning a new process?
}
