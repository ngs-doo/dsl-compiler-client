package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.util.Map;

public enum Namespace implements CompileParameter {
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
		return "Specify root namespace for target language (supported by some languages)";
	}

	@Override
	public String getDetailedDescription() {
		return "Namespaces are supported in languages such as: PHP, Java, Scala.\n" +
				"\n" +
				"\n" +
				"Examples:\n" +
				"	com.company.project\n" +
				"	Shipping";
	}
}
