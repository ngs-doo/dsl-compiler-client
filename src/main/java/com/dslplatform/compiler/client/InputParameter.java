package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;
import sun.misc.Service;

import java.util.*;

public class InputParameter {
	private static CompileParameter[] DEFAULT_PARAMETERS = new CompileParameter[] {
		Help.INSTANCE,
		PropertiesFile.INSTANCE,
		Username.INSTANCE,
		Password.INSTANCE,
		DslPath.INSTANCE,
		SqlPath.INSTANCE,
		Download.INSTANCE,
		IncludeSources.INSTANCE,
		Dependencies.INSTANCE,
		DotNet.INSTANCE,
		Mono.INSTANCE,
		TempPath.INSTANCE,
		DslCompiler.INSTANCE,
		Maven.INSTANCE,
		JavaPath.INSTANCE,
		ScalaPath.INSTANCE,
		Namespace.INSTANCE,
		Settings.INSTANCE,
		DbConnection.INSTANCE,
		Prompt.INSTANCE,
		Parse.INSTANCE,
		Diff.INSTANCE,
		Targets.INSTANCE,
		ForceMigration.INSTANCE,
		Migration.INSTANCE,
		ApplyMigration.INSTANCE,
		DisableColors.INSTANCE,
		LogOutput.INSTANCE
	};

	private static ArrayList<CompileParameter> allParameters;

	static {
		allParameters = new ArrayList<CompileParameter>(DEFAULT_PARAMETERS.length);
		Collections.addAll(allParameters, DEFAULT_PARAMETERS);
		ServiceLoader<CompileParameter> plugins = ServiceLoader.load(CompileParameter.class);
		for (CompileParameter cp : plugins) {
			allParameters.add(cp);
		}
	}

	public static List<CompileParameter> getPlugins() {
		return allParameters;
	}

	public static CompileParameter from(final String value) {
		for (final CompileParameter cp : allParameters) {
			if (cp.getAlias().equalsIgnoreCase(value)) {
				return cp;
			}
		}
		return null;
	}

	public static boolean parse(final String[] args, final Context context) {
		if (args.length == 1 && ("/?".equals(args[0]) || "-?".equals(args[0]) || "?".equals(args[0]))) {
			showHelpAndExit(context, true);
			return false;
		}
		final List<ParameterParser> customParsers = new ArrayList<ParameterParser>();
		for (final CompileParameter cp : allParameters) {
			if (cp instanceof ParameterParser) {
				customParsers.add((ParameterParser) cp);
			}
		}
		final List<String> errors = new ArrayList<String>();
		for (String a : args) {
			if (a.startsWith("-") || a.startsWith("/")) a = a.substring(1);
			final int eq = a.indexOf('=');
			final String name = a.substring(0, eq != -1 ? eq : a.length());
			final String value = eq == -1 ? null : a.substring(eq + 1);
			final CompileParameter cp = from(name);
			if (cp == null) {
				boolean matched = false;
				for (final ParameterParser parser : customParsers) {
					final Either<Boolean> tryParse = parser.tryParse(name, value, context);
					if (!tryParse.isSuccess()) {
						errors.add(tryParse.explainError());
						matched = true;
						break;
					} else if (tryParse.get()) {
						matched = true;
						break;
					}
				}
				if (!matched) {
					errors.add("Unknown parameter: " + name);
				}
			} else {
				if (eq == -1 && cp.getUsage() != null) {
					if (cp instanceof ParameterParser) {
						Either<Boolean> tryParse = ((ParameterParser) cp).tryParse(name, null, context);
						if (tryParse.isSuccess() && tryParse.get()) {
							context.put(cp, null);
						} else {
							errors.add("Expecting " + cp.getUsage() + " after = for " + a);
						}
					} else {
						errors.add("Expecting " + cp.getUsage() + " after = for " + a);
					}
				} else {
					context.put(cp, value);
				}
			}
		}
		if (args.length == 0 || errors.size() > 0) {
			for (final String err : errors) {
				context.error(err);
			}
			showHelpAndExit(context, args.length == errors.size());
			return false;
		}
		return true;
	}

	private static void showHelpAndExit(final Context context, final boolean headers) {
		if (headers) {
			context.show("DSL Platform - Command-Line Client (" + Main.getVersion() + ")");
			context.show("This tool allows you to compile provided DSL to various languages such as Java, Scala, PHP, C#, etc... or create a SQL migration between two DSL models.");
		}
		context.show();
		context.show();
		context.show("Command parameters:");
		int max = 0;
		for (final CompileParameter cp : allParameters) {
			if (cp.getShortDescription() == null) {
				continue;
			}
			int width = cp.getAlias().length();
			if (cp.getUsage() != null) {
				width += 1 + cp.getUsage().length();
			}
			if (max < width) {
				max = width;
			}
		}
		max += 2;
		for (final CompileParameter cp : allParameters) {
			if (cp.getShortDescription() == null) {
				continue;
			}
			final StringBuilder sb = new StringBuilder();
			sb.append(" -").append(cp.getAlias());
			int len = max - cp.getAlias().length();
			if (cp.getUsage() != null) {
				sb.append("=").append(cp.getUsage());
				len -= cp.getUsage().length() + 1;
			}
			for (; len >= 0; len--) {
				sb.append(' ');
			}
			sb.append(cp.getShortDescription());
			context.show(sb.toString());
		}
		context.show();
		context.show("Example usages:");
		context.show("\t-target=java_client,revenj -db=localhost/Database?user=postgres");
		context.show("\t/java_client=model.jar /revenj=Model.dll /db=localhost/Database?user=postgres");
		context.show("\t/properties=development.props /download");
	}
}
