package com.dslplatform.compiler.client;

import java.util.Properties;

public class Main {
	public static void main(final String[] args) {
		final Context context = new Context();
		if (InputParameter.parse(args, context)) {
			processContext(context);
		}
	}

	public static boolean processContext(final Context context) {
		try {
			for (final CompileParameter cp : InputParameter.getPlugins()) {
				if (!cp.check(context)) {
					if (cp.getDetailedDescription() != null) {
						context.show();
						context.show();
						context.show(cp.getDetailedDescription());
					}
					return false;
				}
			}
			for (final CompileParameter cp : InputParameter.getPlugins()) {
				cp.run(context);
			}
			return true;
		} catch (ExitException ex) {
			return false;
		}
	}

	private static final Properties versionInfo = new Properties();

	private static String getVersionInfo(final String section) {
		if (versionInfo.isEmpty()) {
			try {
				versionInfo.load(Main.class.getResourceAsStream("dsl-clc.properties"));
			} catch (final Throwable t) {
				t.printStackTrace();
			}
		}

		return versionInfo.getProperty(section);
	}

	public static String getVersion() {
		return getVersionInfo("version");
	}

	public static String getReleaseDate() {
		return getVersionInfo("date");
	}
}
