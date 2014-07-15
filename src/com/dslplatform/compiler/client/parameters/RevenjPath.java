package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public enum RevenjPath implements CompileParameter { //TODO: overwries revenj target dependency?
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.REVENJ);
		final boolean withRevenj = parameters.containsKey(InputParameter.TARGET) && parameters.get(InputParameter.TARGET).contains("revenj");
		if (value == null && !withRevenj) {
			return true;
		}
		final File revenjPath;
		if (value == null || value.length() == 0) {
			revenjPath = new File("./revenj");
			if (!revenjPath.exists()) {
				System.out.println("Revenj path not provided, but Revenj used as target compilation. Can't use default path (./revenj) since it doesn't exists");
				return false;
			}
			parameters.put(InputParameter.REVENJ, "./revenj");
		} else {
			revenjPath = new File(value);
			if (!revenjPath.exists()) {
				System.out.println("Revenj path provided (" + value + ") but not found. Fix the path before continuing compilation");
				return false;
			}
		}
		if (!revenjPath.isDirectory()) {
			System.out.println("Provided Revenj path (" + value + ") is not a directory. Check provided value");
			return false;
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Path to Revenj dlls. When compiling Mono server, Revenj dependencies are required";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
