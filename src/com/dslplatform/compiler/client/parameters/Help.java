package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonValue;

public enum Help implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.HELP)) {
			final String value = context.get(InputParameter.HELP);
			final InputParameter input = InputParameter.from(value);
			if (input == null) {
				context.error("Unknown command: " + value);
				System.exit(0);
			}
			final String help = input.parameter.getDetailedDescription();
			if (help == null) {
				context.error("Sorry, no detailed info about:" + value);
			} else {
				final int len;
				if (input.parameter.getShortDescription() != null) {
					len = value.length() + 2 + input.parameter.getShortDescription().length();
					context.log(value + ": " + input.parameter.getShortDescription());
				} else {
					len = value.length();
					context.log(value);
				}
				final StringBuilder sb = new StringBuilder(len);
				for (int i = 0; i < len; i++) {
					sb.append("=");
				}
				context.log(sb.toString());
				context.log();
				context.log(input.parameter.getDetailedDescription());
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
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
