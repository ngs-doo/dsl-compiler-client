package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonValue;

import java.util.Map;

public enum Parse implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.PARSE)) {
			final Map<String, String> dslMap = DslPath.getCurrentDsl(parameters);
			if (dslMap.size() == 0) {
				System.out.println("DSL files not found in: " + parameters.get(InputParameter.DSL) + ". At least one DSL file required.");
				System.exit(0);
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
		return "This library doesn't contain parser for DSL.\n" +
				"Use other libraries for Eclipse or Visual Studio which provide syntax highlighting and basic parsing for DSL.\n" +
				"DSL Platform compiler can be used to validate current DSL. Error descriptions will be provided in case of errors.";
	}
}
