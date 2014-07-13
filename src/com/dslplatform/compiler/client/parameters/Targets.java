package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;

import java.util.Map;

public enum Targets implements CompileParameter {
	INSTANCE;

	public static enum Option {
		JAVA_CLIENT("java_client", "Java client", "Java"),
		REVENJ("revenj", "Revenj .NET server", "CSharpServer"),
		PHP("php", "PHP client", "PHP"),
		SCALA_CLIENT("scala_client", "Scala client", "ScalaClient");

		private final String value;
		private final String description;
		private final String platformName;
		Option(final String value, final String description, final String platformName) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
		}

		private static Option from(final String value) {
			for(final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}
	}

	private static void listOptions() {
		for(final Option o : Option.values()) {
			System.out.println(o.value + " - " + o.description);
		}
		System.out.println("Example usage: -target=java_client,revenj");
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (!parameters.containsKey(InputParameter.TARGET)) {
			return true;
		}
		final String value = parameters.get(InputParameter.TARGET);
		final String[] targets = value != null ? value.split(",") : new String[0];
		if (targets.length == 0) {
			System.out.println("Targets not provided. Available targets: ");
			listOptions();
			return false;
		}
		for(final String t : targets) {
			final Option o = Option.from(t);
			if (o == null) {
				System.out.println("Unknown target: " + t);
				listOptions();
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
		return "Convert DSL to specified targets (Java client, PHP, Revenj server, ...)";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
