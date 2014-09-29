package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonValue;

import java.util.Map;

public enum Parse implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(InputParameter.PARSE)) {
			final Map<String, String> dslMap = DslPath.getCurrentDsl(context);
			if (dslMap.size() == 0) {
				context.error("DSL files not found in: " + context.get(InputParameter.DSL) + ". At least one DSL file required.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(InputParameter.PARSE)) {
			final JsonValue json = Utils.toJson(DslPath.getCurrentDsl(context));
			context.show("Validating DSL...");
			final Either<String> result = DslServer.put("Platform.svc/parse", context, json);
			if (result.isSuccess()) {
				context.show("Parse successful.");
			} else {
				context.error(result.whyNot());
				throw new ExitException();
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
