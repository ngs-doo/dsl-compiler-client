package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum InputParameter {
	USERNAME("u", "username", Username.INSTANCE),
	PASSWORD("p", "password", Password.INSTANCE),
	DSL("dsl", "path", DslPath.INSTANCE),
	PARSE("parse", null, Parse.INSTANCE),
	TARGET("target", "options", Targets.INSTANCE),
	NAMESPACE("namespace", "value", Namespace.INSTANCE),
	SETTINGS("settings", "options", Settings.INSTANCE),
	CONNECTION_STRING("db", "connection_string", DbConnection.INSTANCE),
	DIFF("diff", null, Diff.INSTANCE),
	REVENJ("revenj", "path", RevenjPath.INSTANCE),
	SQL("sql", "path", SqlPath.INSTANCE);

	public final String alias;
	public final String usage;
	public final CompileParameter parameter;

	InputParameter(final String alias, final String usage, final CompileParameter parameter) {
		this.alias = alias;
		this.usage = usage;
		this.parameter = parameter;
	}

	private static InputParameter from(final String value) {
		for(final InputParameter cp : InputParameter.values()) {
			if (cp.alias.equalsIgnoreCase(value)) {
				return cp;
			}
		}
		return null;
	}

	public static Map<InputParameter, String> parse(String[] args) {
		final Map<InputParameter, String> options = new HashMap<InputParameter, String>();
		final List<String> errors = new ArrayList<String>();
		for(final String a : args) {
			if (a.charAt(0) != '-') {
				errors.add("Invalid parameter: " + a + ". Expecting - at the beginning.");
				continue;
			}
			final int eq = a.indexOf('=');
			final String name = a.substring(0, eq != -1 ? eq : a.length()).substring(1);
			final InputParameter cp = InputParameter.from(name);
			if (cp == null) {
				errors.add("Unknown parameter: " + name);
			} else {
				if (eq == -1 && cp.usage != null) {
					errors.add("Expecting " + cp.usage + " after = for " + a);
				} else {
					options.put(cp, name.length() + 1 == a.length() ? null : a.substring(eq + 1));
				}
			}
		}
		if (options.size() == 0 || errors.size() > 0) {
			for(final String err : errors) {
				System.out.println(err);
			}
			showHelpAndExit(options.size() == 0);
		}
		return options;
	}

	public static void showHelpAndExit(final boolean headers) {
		if (headers) {
			System.out.println("DSL Platform command line client.");
			System.out.println("This tool allows you to compile provided DSL to various languages such as Java, Scala, PHP, C#, etc... or create an SQL migration.");
		}
		System.out.println();
		System.out.println();
		System.out.println("Command parameters:");
		int max = 0;
		for (final InputParameter ip : InputParameter.values()) {
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
			System.out.print(" -" + ip.alias);
			int len = max - ip.alias.length();
			if (ip.usage != null) {
				System.out.print("=" + ip.usage);
				len -= ip.usage.length() + 1;
			}
			for(;len >= 0; len--) {
				System.out.print(' ');
			}
			System.out.println(ip.parameter.getShortDescription());
		}
		System.exit(0);
	}

}
