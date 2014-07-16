package com.dslplatform.compiler.client;

public class Main {
	public static void main(final String[] args) {
		final Context context = new Context();
		InputParameter.parse(args, context);
		processContext(context);
	}

	public static void processContext(final Context context) {
		for (final InputParameter ip : InputParameter.values()) {
			if (!ip.parameter.check(context)) {
				if (ip.parameter.getDetailedDescription() != null) {
					context.log();
					context.log();
					context.log(ip.parameter.getDetailedDescription());
				}
				System.exit(0);
			}
		}
		for (final InputParameter ip : InputParameter.values()) {
			ip.parameter.run(context);
		}
	}
}
