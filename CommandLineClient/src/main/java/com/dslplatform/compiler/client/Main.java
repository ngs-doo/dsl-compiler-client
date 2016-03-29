package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.*;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class Main {
	public static void main(final String[] args) {
		final Context context = new Context();
		final List<CompileParameter> parameters = initializeParameters(context, ".");
		final int returnCode = parse(args, context, parameters) ? (processContext(context, parameters) ? 0 : 1) : 2;
		context.close();
		System.exit(returnCode);
	}

	public static List<CompileParameter> initializeParameters(final Context context, final String path) {
		final List<CompileParameter> parameters = new ArrayList<CompileParameter>(DEFAULT_PARAMETERS.length + 2);
		parameters.add(new Help(parameters));
		parameters.add(new PropertiesFile(parameters));
		Collections.addAll(parameters, DEFAULT_PARAMETERS);
		final File loc = new File(path); //TODO: allow custom plugin path
		final File[] jars = loc.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getPath().toLowerCase().endsWith(".jar");
			}
		});
		final List<URL> urls = new ArrayList<URL>(jars.length);
		for (final File j : jars) {
			try {
				urls.add(j.toURI().toURL());
			} catch (MalformedURLException ex) {
				context.error(ex);
			}
		}
		final URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]));
		final ServiceLoader<CompileParameter> plugins = ServiceLoader.load(CompileParameter.class, ucl);
		for (final CompileParameter cp : plugins) {
			parameters.add(cp);
		}
		//HACK: service loader will lock jars on Windows
		//close is not available in Java6, so use reflection to invoke it
		if (Utils.isWindows()) {
			try {
				final Method close = ucl.getClass().getMethod("close");
				if (close != null) {
					close.invoke(ucl);
				}
			} catch (Exception ignore) {
			}
		}
		return parameters;
	}

	private static CompileParameter[] DEFAULT_PARAMETERS = new CompileParameter[]{
			DslPath.INSTANCE,
			SqlPath.INSTANCE,
			Download.INSTANCE,
			Dependencies.INSTANCE,
			DotNet.INSTANCE,
			Mono.INSTANCE,
			TempPath.INSTANCE,
			DslCompiler.INSTANCE,
			Maven.INSTANCE,
			JavaPath.INSTANCE,
			ScalaPath.INSTANCE,
			Namespace.INSTANCE,
			Version.INSTANCE,
			Settings.INSTANCE,
			PostgresConnection.INSTANCE,
			OracleConnection.INSTANCE,
			Prompt.INSTANCE,
			Parse.INSTANCE,
			Diff.INSTANCE,
			Targets.INSTANCE,
			Force.INSTANCE,
			Migration.INSTANCE,
			ApplyMigration.INSTANCE,
			DisableColors.INSTANCE,
			LogOutput.INSTANCE,
			VarraySize.INSTANCE,
			GrantRole.INSTANCE
	};

	public static boolean processContext(final Context context, final List<CompileParameter> parameters) {
		try {
			for (final CompileParameter cp : parameters) {
				if (!cp.check(context)) {
					if (cp.getDetailedDescription() != null) {
						context.show();
						context.show();
						context.show(cp.getDetailedDescription());
					}
					return false;
				}
			}
			for (final CompileParameter cp : parameters) {
				cp.run(context);
			}
			return true;
		} catch (ExitException ex) {
			return false;
		}
	}

	private static CompileParameter from(final String value, final List<CompileParameter> parameters) {
		for (final CompileParameter cp : parameters) {
			if (cp.getAlias().equalsIgnoreCase(value)) {
				return cp;
			}
		}
		return null;
	}

	private static boolean parse(final String[] args, final Context context, final List<CompileParameter> parameters) {
		if (args.length == 1 && ("/?".equals(args[0]) || "-?".equals(args[0]) || "?".equals(args[0]))) {
			showHelpAndExit(context, true, parameters);
			return false;
		}
		final List<ParameterParser> customParsers = new ArrayList<ParameterParser>();
		for (final CompileParameter cp : parameters) {
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
			final CompileParameter cp = from(name, parameters);
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
			showHelpAndExit(context, args.length == errors.size(), parameters);
			return false;
		}
		return true;
	}

	private static void showHelpAndExit(final Context context, final boolean headers, final List<CompileParameter> parameters) {
		if (headers) {
			context.show("DSL Platform - Command-Line Client (" + Main.getVersion() + ")");
			context.show("This tool allows you to compile provided DSL to various languages such as Java, Scala, PHP, C#, etc... or create an SQL migration between two DSL models.");
		}
		context.show();
		context.show();
		context.show("Command parameters:");
		int max = 0;
		for (final CompileParameter cp : parameters) {
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
		for (final CompileParameter cp : parameters) {
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
		context.show("\ttarget=java_client,revenj.java postgres=localhost/Database?user=postgres");
		context.show("\tjava_client=model.jar revenj.net=Model.dll postgres=localhost/Database?user=postgres");
		context.show("\tproperties=development.props download");
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
