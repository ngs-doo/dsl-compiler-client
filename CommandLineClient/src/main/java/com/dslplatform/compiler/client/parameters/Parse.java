package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.util.Map;

public enum Parse implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "parse";
	}

	@Override
	public String getUsage() {
		return null;
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			final Map<String, String> dslMap = DslPath.getCurrentDsl(context);
			if (dslMap.size() == 0) {
				context.error("DSL files not found in: '" + context.get(DslPath.INSTANCE) + "'. At least one DSL file required.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		if (context.contains(INSTANCE)) {
			context.show("Validating DSL ...");
			final Either<Boolean> result = DslCompiler.parse(context, DslPath.getDslPaths(context));
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
		return "This library uses external parser for DSL. Parser can be started through: DslCompiler.setupServer method.\n" +
				"Eclipse, IntelliJ IDEA and other IDE plugins use such setup for syntax highlighting.\n\n" +
				"DSL Platform compiler can be used to validate current DSL. Error descriptions will be provided in case of errors.";
	}
}
