package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public enum RevenjPath implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		final String value = parameters.get(InputParameter.REVENJ);
		final boolean withRevenj = parameters.containsKey(InputParameter.TARGET)
				? parameters.get(InputParameter.TARGET).contains("revenj")
				: false;
		if (value == null && !withRevenj) {
			return true;
		}
		if (value == null || value.length() == 0) {
			final File revenjPath = new File("./revenj");
			if (!revenjPath.exists()) {
				System.out.println("Revenj path not provided, but Revenj used as target compilation. Can't use default path (./revenj) since it doesn't exists");
				return false;
			}
			parameters.put(InputParameter.REVENJ, "./revenj");
		} else {
			final File revenjPath = new File("./revenj");
			if (!revenjPath.exists()) {
				System.out.println("Revenj path not provided. Can't use default path (./revenj) since it doesn't exists");
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
		return "Path to Revenj dlls. When compiling Mono server, Revenj dependencies are required";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
