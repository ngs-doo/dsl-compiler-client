package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public enum Download implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "Download library dependencies if not available.";
	}

	@Override
	public String getDetailedDescription() {
		return "If no-prompt option is used, force download of missing dependencies.";
	}
}
