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
				if (input.parameter.getShortDescription() != null) {
					System.out.print(value + ": " + input.parameter.getShortDescription());
				} else {
					System.out.println(value);
				}
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
		return "Parse current DSL to check for errors";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
