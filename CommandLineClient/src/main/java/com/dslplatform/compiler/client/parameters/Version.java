package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum Version implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "version"; }
	@Override
	public String getUsage() { return "value"; }

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Specify assembly/jar version. If unspecified, current date/time will be used";
	}

	@Override
	public String getDetailedDescription() {
		return "DLL/jar version number.\n" +
				"\n" +
				"\n" +
				"Example:\n" +
				"\t1.0.2";
	}
}
