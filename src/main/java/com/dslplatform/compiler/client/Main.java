package com.dslplatform.compiler.client;

public class Main {
	public static void main(final String[] args) {
		final Context context = new Context();
		if (InputParameter.parse(args, context)) {
			processContext(context);
		}
	}

	public static boolean processContext(final Context context) {
		try {
			for (final InputParameter ip : InputParameter.values()) {
				if (!ip.parameter.check(context)) {
					if (ip.parameter.getDetailedDescription() != null) {
						context.show();
						context.show();
						context.show(ip.parameter.getDetailedDescription());
					}
					return false;
				}
			}
			for (final InputParameter ip : InputParameter.values()) {
				ip.parameter.run(context);
			}
			return true;
		} catch (ExitException ex) {
			return false;
		}
	}
}
