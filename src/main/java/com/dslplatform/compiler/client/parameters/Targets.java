package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonObject;
import com.dslplatform.compiler.client.parameters.build.*;

import java.io.*;
import java.util.*;

public enum Targets implements CompileParameter, ParameterParser {
	INSTANCE;

	private final static String[] DOTNET_CLIENT_DEPENDENCIES = {
			"System.dll",
			"System.Core.dll",
			"System.Dynamic.dll",
			"System.ComponentModel.Composition.dll",
			"System.Configuration.dll",
			"System.Data.dll",
			"System.Drawing.dll",
			"System.Xml.dll",
			"System.Xml.Linq.dll",
			"System.Runtime.Serialization.dll"
	};

	private final static String[] DOTNET_WPF_DEPENDENCIES = {
			"System.dll",
			"System.Core.dll",
			"System.Dynamic.dll",
			"System.ComponentModel.Composition.dll",
			"System.Configuration.dll",
			"System.Data.dll",
			"System.Drawing.dll",
			"System.Xml.dll",
			"System.Xml.Linq.dll",
			"System.Runtime.Serialization.dll",
			"Microsoft.CSharp.dll",
			"System.Xaml.dll",
			"gac/PresentationFramework",
			"gac/WindowsBase",
			"gac/PresentationCore"
	};

	public static enum Option {
		JAVA_CLIENT("java_client", "Java client", "Java", ".java", new CompileJavaClient("Java client", "java-client", "java_client", "dsl-client-java", "./generated-model-java.jar"), true),
		ANDORID("android", "Android", "Android", ".java", new CompileJavaClient("Android", "android", "android", "dsl-client-java", "./generated-model-android.jar"), true),
		REVENJ("revenj", "Revenj .NET server", "CSharpServer", ".cs", new CompileRevenj(), false),
		DOTNET_CLIENT("dotnet_client", ".NET client", "CSharpClient", ".cs", new CompileCsClient(".NET client", "client", "dotnet_client", "./ClientModel.dll", DOTNET_CLIENT_DEPENDENCIES, false), false),
		DOTNET_PORTABLE("dotnet_portable", ".NET portable", "CSharpPortable", ".cs", new CompileCsClient(".NET portable", "portable", "dotnet_portable", "./PortableModel.dll", new String[0], false), false),
		DOTNET_WPF("wpf", ".NET WPF GUI", "Wpf", ".cs", new CompileCsClient(".NET WPF GUI", "wpf", "dotnet_wpf", "./WpfModel.dll", DOTNET_WPF_DEPENDENCIES, true), false),
		PHP("php_client", "PHP client", "Php", ".php", new PrepareSources("PHP", "php_client", "Generated-PHP"), true),
		PHP_UI("php_ui", "PHP UI client", "PhpUI", "", new PreparePhpUI("PHP UI", "php_ui", "Generated-PHP-UI"), true),
		SCALA_CLIENT("scala_client", "Scala client", "ScalaClient", ".scala", new CompileScalaClient(), false),
		SCALA_SERVER("scala_server", "Scala server", "ScalaServer", ".scala", new PrepareSources("Scala server", "scala_server", "Generated-Scala-Server"), true);

		private final String value;
		private final String description;
		private final String platformName;
		private final String extension;
		private final BuildAction action;
		private final boolean convertToPath;

		Option(
				final String value,
				final String description,
				final String platformName,
				final String extension,
				final BuildAction action,
				final boolean convertToPath) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
			this.extension = extension;
			this.action = action;
			this.convertToPath = convertToPath;
		}

		private static Option from(final String value) {
			for (final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}
	}

	private static void listOptions(final Context context) {
		for (final Option o : Option.values()) {
			context.show(o.value + " - " + o.description);
		}
		context.show("Example usages:");
		context.show("	-target=java_client,revenj");
		context.show("	-java_client -revenj=./model/SeverModel.dll");
	}

	private static final String CACHE_NAME = "target_option_cache";

	@Override
	public Either<Boolean> tryParse(final String name, final String value, final Context context) {
		if (Option.from(name) != null) {
			context.put(name, value == null || value.length() == 0 ? null : value);
			return Either.success(true);
		} else {
			for (final Option o : Option.values()) {
				if (("dependencies:" + o.value).equalsIgnoreCase(name) || ("dependency:" + o.value).equalsIgnoreCase(name)) {
					if (value == null || value.length() == 0) {
						return Either.fail("Target dependency parameter detected, but it's missing path as argument. Parameter: " + name);
					}
					final File path = new File(value);
					if (path.exists() && !path.isDirectory()) {
						return Either.fail("Target dependency path found, but it's not a directory. Parameter: " + name);
					}
					context.put("dependency:" + o.value, value);
					return Either.success(true);
				}
			}
		}
		return Either.success(false);
	}

	@Override
	public boolean check(final Context context) throws ExitException {
		final List<String> targets = new ArrayList<String>();
		final Set<String> distinctTargets = new HashSet<String>();
		if (context.contains(InputParameter.TARGET)) {
			final String value = context.get(InputParameter.TARGET);
			if (value == null || value.length() == 0) {
				context.error("Targets not provided. Available targets: ");
				listOptions(context);
				return false;
			}
			for (final String t : value.split(",")) {
				if (distinctTargets.add(t.toLowerCase())) {
					targets.add(t);
				}
			}
		}
		for (final Option o : Option.values()) {
			final String lc = o.value.toLowerCase();
			if (context.contains(o.value) && !distinctTargets.contains(lc)) {
				targets.add(o.value);
				distinctTargets.add(lc);
			}
		}
		if (targets.size() == 0) {
			if (context.contains(InputParameter.TARGET)) {
				context.error("Targets not provided. Available targets: ");
				listOptions(context);
				return false;
			}
			return true;
		}
		final List<Option> options = new ArrayList<Option>(targets.size());
		for (final String name : targets) {
			final Option o = Option.from(name);
			if (o == null) {
				context.error("Unknown target: " + name);
				listOptions(context);
				return false;
			}
			options.add(o);
		}
		final Map<String, String> dsls = DslPath.getCurrentDsl(context);
		if (dsls.size() == 0) {
			context.error("Can't compile DSL to targets since no DSL was provided.");
			context.error("Please check your DSL folder: " + context.get(InputParameter.DSL));
			return false;
		}
		for (final Option o : options) {
			if (!o.action.check(context)) {
				return false;
			}
		}
		context.cache(CACHE_NAME, options);
		return true;
	}

	@Override
	public void run(final Context context) throws ExitException {
		final List<Option> targets = context.load(CACHE_NAME);
		if (targets == null) {
			return;
		}
		if (context.contains(InputParameter.COMPILER)) {
			compileOffline(context, targets);
		} else {
			compileOnline(context, targets);
		}
	}

	private void compileOffline(Context context, List<Option> targets) throws ExitException {
		final List<File> dsls = DslPath.getDslPaths(context);
		final List<Settings.Option> settings = Settings.get(context);
		final String temp = TempPath.getTempProjectPath(context).getAbsolutePath();
		final File compiler = new File(context.get(InputParameter.COMPILER));
		for (final Option t : targets) {
			Map<String, String> files =
					DslCompiler.compile(
							context,
							compiler,
							t.value,
							settings,
							context.get(InputParameter.NAMESPACE),
							dsls);
			try {
				for (final Map.Entry<String, String> kv : files.entrySet()) {
					final String fullName = t.value + "/" + kv.getKey() + t.extension;
					saveFile(context, temp, t.convertToPath, fullName, kv.getValue());
				}
			} catch (IOException e) {
				context.error("Can't create temporary target file. Compilation results can't be saved locally.");
				context.error(e);
				throw new ExitException();
			}
			if (t.action != null) {
				t.action.build(new File(temp, t.value), context);
			}
		}
	}

	private static void saveFile(
			final Context context,
			final String temp,
			final boolean escapeName,
			final String name,
			final String content) throws ExitException, IOException {
		final String nameOnly = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
		final File file = escapeName
				? new File(temp, nameOnly.replace(".", "/") + name.substring(nameOnly.length()))
				: new File(temp, name);
		final File parentPath = file.getParentFile();
		if (!parentPath.exists()) {
			if (!parentPath.mkdirs()) {
				context.error("Failed creating path for target file: " + parentPath.getAbsolutePath());
				throw new ExitException();
			}
		}
		if (!file.createNewFile()) {
			context.error("Failed creating target file: " + file.getAbsolutePath());
			throw new ExitException();
		}
		Utils.saveFile(file, content);
	}

	private void compileOnline(Context context, List<Option> targets) throws ExitException {
		final StringBuilder sb = new StringBuilder();
		final Set<String> addedTargets = new HashSet<String>();
		for (final Option t : targets) {
			if (!addedTargets.contains(t.platformName)) {
				sb.append(t.platformName);
				sb.append(',');
				addedTargets.add(t.platformName);
			}
		}
		final Map<String, String> dsls = DslPath.getCurrentDsl(context);
		final StringBuilder url = new StringBuilder("Platform.svc/unmanaged/source?targets=");
		url.append(sb.substring(0, sb.length() - 1));
		if (context.contains(InputParameter.NAMESPACE)) {
			url.append("&namespace=").append(context.get(InputParameter.NAMESPACE));
		}
		final String settings = Settings.parseAndConvert(context);
		if (settings.length() > 0) {
			url.append("&options=").append(settings);
		}
		context.show("Compiling DSL online...");
		final Either<String> response = DslServer.put(url.toString(), context, Utils.toJson(dsls));
		if (!response.isSuccess()) {
			context.error("Error compiling DSL to specified target.");
			context.error(response.whyNot());
			throw new ExitException();
		}
		final JsonObject files = JsonObject.readFrom(response.get());
		final String temp = TempPath.getTempProjectPath(context).getAbsolutePath();
		final Set<String> escapeNames = new HashSet<String>();
		for (final Option t : targets) {
			if (t.convertToPath) {
				escapeNames.add(t.platformName);
			}
		}
		try {
			for (final String name : files.names()) {
				final boolean escapeName = name.contains("/") && escapeNames.contains(name.substring(0, name.indexOf("/")));
				saveFile(context, temp, escapeName, name, files.get(name).asString());
			}
		} catch (IOException e) {
			context.error("Can't create temporary target file. Compilation results can't be saved locally.");
			context.error(e);
			throw new ExitException();
		}
		for (final Option t : targets) {
			if (t.action != null) {
				t.action.build(new File(temp, t.platformName), context);
			}
		}
	}

	@Override
	public String getShortDescription() {
		return "Convert DSL to specified target (Java client, PHP, Revenj server, ...)";
	}

	@Override
	public String getDetailedDescription() {
		final StringBuilder sb = new StringBuilder();
		sb.append("DSL Platform converts DSL model to various target sources which are then locally compiled (if possible).\n\n");
		sb.append("Custom output name can be specified with as -java_client=/home/model.jar,revenj=/home/revenj.dll\n\n");
		sb.append("Custom dependency path can be specified as -dependencies:java_client=/home/java_libs\n\n");
		sb.append("This option specifies which target sources are available.\n");
		sb.append("---------------------------------------------------------\n");
		for (final Option o : Option.values()) {
			sb.append(o.value).append(" - ").append(o.description).append("\n");
		}
		return sb.toString();
	}
}
