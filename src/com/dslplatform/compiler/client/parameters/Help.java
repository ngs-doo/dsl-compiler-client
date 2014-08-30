package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

public enum Help implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) throws ExitException {
		if (context.contains(InputParameter.HELP)) {
			final String value = context.get(InputParameter.HELP);
			final InputParameter input = InputParameter.from(value);
			if (input == null) {
				context.error("Unknown command: " + value);
				throw new ExitException();
			}
			final String help = input.parameter.getDetailedDescription();
			if (help == null) {
				context.error("Sorry, no detailed info about:" + value);
			} else {
				final int len;
				if (input.parameter.getShortDescription() != null) {
					len = value.length() + 2 + input.parameter.getShortDescription().length();
					context.show(value + ": " + input.parameter.getShortDescription());
				} else {
					len = value.length();
					context.show(value);
				}
				final StringBuilder sb = new StringBuilder(len);
				for (int i = 0; i < len; i++) {
					sb.append("=");
				}
				context.show(sb.toString());
				context.show();
				context.show(input.parameter.getDetailedDescription());
			}
			throw new ExitException();
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
