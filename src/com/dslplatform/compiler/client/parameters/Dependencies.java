package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;

public enum Dependencies implements CompileParameter {
	INSTANCE;

	public static File getDependenciesRoot(final Context context) {
		final String depsParam = context.get(InputParameter.DEPENDENCIES);
		return new File(depsParam != null ? depsParam : "./");
	}

	@Override
	public boolean check(final Context context) {
		final String value = context.get(InputParameter.DEPENDENCIES);
		if (value != null && value.length() > 0) {
			final File dependenciesPath = new File(value);
			if (!dependenciesPath.exists()) {
				context.error("Dependencies path provided (" + value + ") but not found. Fix the path before continuing compilation");
				return false;
			}
			if (!dependenciesPath.isDirectory()) {
				context.error("Provided dependencies path (" + value + ") is not a directory. Check provided value");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
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
