package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;

public enum Namespace implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "namespace"; }
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
		return "Specify root namespace for target language (supported by some languages)";
	}

	@Override
	public String getDetailedDescription() {
		return "Namespaces are supported in languages such as: PHP, Java, Scala.\n" +
				"\n" +
				"\n" +
				"Examples:\n" +
				"	com.company.project\n" +
				"	Shipping.Model";
	}
}
