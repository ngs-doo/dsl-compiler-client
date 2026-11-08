package com.dslplatform.compiler.client;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class MainParseTest {

	private static boolean parse(final Context context, final List<CompileParameter> parameters, final String... args) throws Exception {
		final Method method = Main.class.getDeclaredMethod("parse", String[].class, Context.class, List.class);
		method.setAccessible(true);
		return (Boolean) method.invoke(null, args, context, parameters);
	}

	@Test
	public void doubleDashArgumentsAreAccepted() throws Exception {
		final ContextMock context = new ContextMock();
		final List<CompileParameter> parameters = Main.initializeParameters(context, ".");
		assertTrue(parse(context, parameters, "--log"));
	}

	@Test
	public void unknownParameterIsReportedWithCleanName() throws Exception {
		final ContextMock context = new ContextMock();
		final List<CompileParameter> parameters = Main.initializeParameters(context, ".");
		assertFalse(parse(context, parameters, "--bogus"));
		assertTrue(context.error.contains("Unknown parameter: bogus"));
	}
}
