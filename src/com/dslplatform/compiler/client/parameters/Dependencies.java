package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public enum Dependencies implements CompileParameter {
	INSTANCE;

	private static File cache;

	public static File getDependenciesRoot(final Map<InputParameter, String> parameters) {
		if (cache != null) {
			return cache;
		}
		final String depsParam = parameters.get(InputParameter.DEPENDENCIES);
		final File depsRoot = new File(depsParam != null ? depsParam : "./");
		return cache = depsRoot;
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.DEPENDENCIES);
		if (value != null && value.length() > 0) {
			final File dependenciesPath = new File(value);
			if (!dependenciesPath.exists()) {
				System.out.println("Dependencies path provided (" + value + ") but not found. Fix the path before continuing compilation");
				return false;
			}
			if (!dependenciesPath.isDirectory()) {
				System.out.println("Provided dependencies path (" + value + ") is not a directory. Check provided value");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Root path to custom dependencies for compilation. Specific target directory will be used";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL client compiler will locally compile sources downloaded from DSL Platform.\n" +
				"Developers are encouraged to use compiled dlls and jars instead of embedding code into their project.\n" +
				"Compilation depends on external libraries which are located in the specified path.\n";
	}
}
