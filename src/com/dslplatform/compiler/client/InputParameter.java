package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;

import java.util.ArrayList;
import java.util.List;

public enum InputParameter {
	HELP("help", "command", Help.INSTANCE),
	PROPERTIES("properties", "file", PropertiesFile.INSTANCE),
	USERNAME("u", "username", Username.INSTANCE),
	PASSWORD("p", "password", Password.INSTANCE),
	DSL("dsl", "path", DslPath.INSTANCE),
	SQL("sql", "path", SqlPath.INSTANCE),
	DOWNLOAD("download", null, Download.INSTANCE),
	DEPENDENCIES("dependencies", "path", Dependencies.INSTANCE),
	DOTNET("dotnet", "path", DotNet.INSTANCE),
	MAVEN("maven", "path", Maven.INSTANCE),
	JAVA("java", "path", JavaPath.INSTANCE),
	SCALAC("scalac", "file", ScalaPath.INSTANCE),
	NAMESPACE("namespace", "value", Namespace.INSTANCE),
	SETTINGS("settings", "options", Settings.INSTANCE),
	CONNECTION_STRING("db", "connection_string", DbConnection.INSTANCE),
	NO_PROMPT("no-prompt", null, Prompt.INSTANCE),
	TEMP("temp", "path", TempPath.INSTANCE),
	PARSE("parse", null, Parse.INSTANCE),
	DIFF("diff", null, Diff.INSTANCE),
	TARGET("target", "options", Targets.INSTANCE),
	FORCE_MIGRATION("force", null, ForceMigration.INSTANCE),
	MIGRATION("migration", null, Migration.INSTANCE),
	APPLY_MIGRATION("apply", null, ApplyMigration.INSTANCE),
	LOG("log", null, LogOutput.INSTANCE);

	public final String alias;
	public final String usage;
	public final CompileParameter parameter;

	InputParameter(final String alias, final String usage, final CompileParameter parameter) {
		this.alias = alias;
		this.usage = usage;
		this.parameter = parameter;
	}

	public static InputParameter from(final String value) {
		for (final InputParameter cp : InputParameter.values()) {
			if (cp.alias.equalsIgnoreCase(value)) {
				return cp;
			}
		}
		return null;
	}

	public static boolean parse(final String[] args, final Context context) {
		if (args.length == 1 && ("/?".equals(args[0]) || "-?".equals(args[0]))) {
			showHelpAndExit(context, true);
			return false;
		}
		final List<String> errors = new ArrayList<String>();
		for (final String a : args) {
			if (a.charAt(0) != '-' && a.charAt(0) != '/') {
				errors.add("Invalid parameter: " + a + ". Expecting - or / at the beginning.");
				continue;
			}
			final int eq = a.indexOf('=');
			final String name = a.substring(0, eq != -1 ? eq : a.length()).substring(1);
			final InputParameter cp = InputParameter.from(name);
			if (cp == null) {
				if (Targets.Option.from(name) != null) {
					context.put(name, eq == -1 ? null : a.substring(eq + 1));
				} else if (Settings.Option.from(name) != null) {
					if (eq != -1) {
						errors.add("Settings parameter detected, but settings don't support arguments. Parameter: " + name);
					}
					context.put(name, null);
				} else {
					errors.add("Unknown parameter: " + name);
				}
			} else {
				if (eq == -1 && cp.usage != null) {
					errors.add("Expecting " + cp.usage + " after = for " + a);
				} else {
					context.put(cp, eq == -1 ? null : a.substring(eq + 1));
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
			context.show("DSL Platform command line client.");
			context.show("This tool allows you to compile provided DSL to various languages such as Java, Scala, PHP, C#, etc... or create a SQL migration between two DSL models.");
		}
		context.show();
		context.show();
		context.show("Command parameters:");
		int max = 0;
		for (final InputParameter ip : InputParameter.values()) {
			if (ip.parameter.getShortDescription() == null) {
				continue;
			}
			int width = ip.alias.length();
			if (ip.usage != null) {
				width += 1 + ip.usage.length();
			}
			if (max < width) {
				max = width;
			}
		}
		max += 2;
		for (final InputParameter ip : InputParameter.values()) {
			if (ip.parameter.getShortDescription() == null) {
				continue;
			}
			final StringBuilder sb = new StringBuilder();
			sb.append(" -").append(ip.alias);
			int len = max - ip.alias.length();
			if (ip.usage != null) {
				sb.append("=").append(ip.usage);
				len -= ip.usage.length() + 1;
			}
			for (; len >= 0; len--) {
				sb.append(' ');
			}
			sb.append(ip.parameter.getShortDescription());
			context.show(sb.toString());
		}
		context.show();
		context.show("Example usages:");
		context.show("	-target=java_client,revenj -db=localhost/Database?user=postgres");
		context.show("	/java_client=model.jar /revenj=Model.dll /db=localhost/Database?user=postgres");
		context.show("	/properties=development.props /download");
	}
}
