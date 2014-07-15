package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonValue;

import java.util.Map;

public enum Help implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.HELP)) {
			final String value = parameters.get(InputParameter.HELP);
			final InputParameter input = InputParameter.from(value);
			if (input == null) {
				System.out.println("Unknown command: " + value);
				System.exit(0);
			}
			final String help = input.parameter.getDetailedDescription();
			if (help == null) {
				System.out.println("Sorry, no detailed info about:" + value);
			} else {
				final int len;
				if (input.parameter.getShortDescription() != null) {
					len = value.length() + 2 + input.parameter.getShortDescription().length();
					System.out.println(value + ": " + input.parameter.getShortDescription());
				} else {
					len = value.length();
					System.out.println(value);
				}
				for(int i=0;i<len;i++) {
					System.out.print("=");
				}
				System.out.println();
				System.out.println(input.parameter.getDetailedDescription());
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.PARSE)) {
			final JsonValue json = Utils.toJson(DslPath.getCurrentDsl(parameters));
			final Either<String> result = DslServer.put("Platform.svc/parse", parameters, json);
			if (result.isSuccess()) {
				System.out.println("Parse successful.");
			} else {
				System.out.print(result.whyNot());
				System.exit(0);
			}
		}
	}

	@Override
	public String getShortDescription() {
		return "Show detailed description of a command";
	}

	@Override
	public String getDetailedDescription() {
		return "Recursion detected...";
	}
}
