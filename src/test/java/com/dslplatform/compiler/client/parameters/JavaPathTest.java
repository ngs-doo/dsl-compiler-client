package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.UtilsTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class JavaPathTest {
	private static String fakeJavaPath;

	@BeforeClass
	public static void initPath() throws IOException {
		fakeJavaPath = UtilsTest.getScriptPath() + "/fake-java";
	}

	@Test
	public void testJavaPathParameter() {
		final Context context = new Context();
		// context.put(LogOutput.INSTANCE, null);
		final String exeType = Utils.isWindows() ? "exe" : "empty";
		context.put(JavaPath.INSTANCE, fakeJavaPath + "/" + exeType);
		assertTrue(JavaPath.INSTANCE.check(context));
	}

	// TODO: Test JAVA_HOME/JDK_HOME environment settings by spawning a new process?
}
