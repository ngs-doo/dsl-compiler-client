package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum Download implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Download library dependencies if not available.";
	}

	@Override
	public String getDetailedDescription() {
		return "Always download missing dependencies.";
	}
}
