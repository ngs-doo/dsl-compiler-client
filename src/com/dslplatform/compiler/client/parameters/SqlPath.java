package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public enum SqlPath implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.SQL)) {
			final String value = parameters.get(InputParameter.SQL);
			if (value != null && value.length() > 0) {
				final File sqlPath = new File(value);
				if (!sqlPath.exists()) {
					System.out.println("SQL path provided (" + sqlPath.getAbsolutePath() + ") but doesn't exists.");
					System.out.println("Specify existing path or remove parameter to use temporary folder.");
					return false;
				}

			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Where to save SQL migration";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
