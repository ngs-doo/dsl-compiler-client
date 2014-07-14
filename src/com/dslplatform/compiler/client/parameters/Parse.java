package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.json.JsonValue;
import com.dslplatform.compiler.client.DslServer;
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
			}
			else {
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
