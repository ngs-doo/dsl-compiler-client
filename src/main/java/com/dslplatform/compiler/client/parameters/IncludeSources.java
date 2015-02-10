package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

public enum IncludeSources implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "include-sources"; }
	@Override
	public String getUsage() { return null; }

	@Override
	public boolean check(final Context context) {
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Make jar/zip file containing sources.";
	}

	@Override
	public String getDetailedDescription() {
		return "Make jar/zip file containing sources.";
	}
}
