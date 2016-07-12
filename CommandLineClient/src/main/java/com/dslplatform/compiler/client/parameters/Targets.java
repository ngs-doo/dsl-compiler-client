package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.build.*;

import java.io.*;
import java.util.*;

public enum Targets implements CompileParameter, ParameterParser {
	INSTANCE;

	@Override
	public String getAlias() {
		return "target";
	}

	@Override
	public String getUsage() {
		return "options";
	}

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

	public enum Option {
		REVENJ_JAVA("revenj.java", "Revenj.Java server for Postgres", ".java", new CompileRevenjJava("revenj.java", "revenj-java", "revenj-servlet", "revenj-storage"), true),
		REVENJ_JAVA_POSTGRES("java_server_postgres", "Revenj.Java server for Postgres", ".java", new CompileRevenjJava("java_server_postgres", "revenj-java", "revenj-servlet", "revenj-storage"), true),
		REVENJ_SPRING("revenj.spring", "Revenj.Java server for Spring with Postgres", ".java", new CompileRevenjJava("revenj.spring", "revenj-spring", "revenj-spring", "revenj-storage"), true),
		JAVA_CLIENT("java_client", "Java client", ".java", new CompileJavaClient("Java client", "java-client", "java_client", "com/dslplatform", "dsl-client-java", "./generated-client-java.jar"), true),
		JAVA_POJO("java_pojo", "Plain Old Java Object", ".java", new CompileJavaClient("Java POJO", "java-pojo", "java_pojo", null, null, "./generated-model-java.jar"), true),
		JAVA_EXTERNAL_JSON("java_external_json", "Java JSON serializers for an existing POJO", ".java", new CompileJavaClient("Java JSON", "java-pojo", "java_external_json", null, null, "./generated-json-java.jar"), true),
		ANDORID("android", "Android", ".java", new CompileJavaClient("Android", "android", "android", "com/dslplatform", "dsl-client-java", "./generated-model-android.jar"), true),
		ANDORID_EXTERNAL_JSON("android_external_json", "Android JSON serializers for an existing POJO", ".java", new CompileJavaClient("Android JSON", "android", "android_external_json", null, null, "./generated-json-android.jar"), true),
		REVENJ_NET("revenj.net", "Revenj.NET server for Postgres", ".cs", new CompileRevenjNet("revenj.net", null), false),
		REVENJ_NET_POSTGRES("dotnet_server_postgres", "Revenj.NET server for Postgres", ".cs", new CompileRevenjNet("dotnet_server_postgres", null), false),
		REVENJ_NET_ORACLE_32("dotnet_server_oracle_32", "Revenj.NET server for Oracle with 32bit client driver", ".cs", new CompileRevenjNet("dotnet_server_oracle_32", "oracle-driver-32bit"), false),
		REVENJ_NET_ORACLE_64("dotnet_server_oracle_64", "Revenj.NET server for Oracle with 64bit client driver", ".cs", new CompileRevenjNet("dotnet_server_oracle_64", "oracle-driver-64bit"), false),
		DOTNET_POCO("dotnet_poco", "Plain Old C# Object", ".cs", new CompileCsClient(".NET POCO", null, "dotnet_poco", "./GeneratedModel.dll", DOTNET_CLIENT_DEPENDENCIES, false), false),
		DOTNET_CLIENT("dotnet_client", ".NET client", ".cs", new CompileCsClient(".NET client", "client", "dotnet_client", "./ClientModel.dll", DOTNET_CLIENT_DEPENDENCIES, false), false),
		DOTNET_PORTABLE("dotnet_portable", ".NET portable", ".cs", new CompileCsClient(".NET portable", "portable", "dotnet_portable", "./PortableModel.dll", new String[0], false), false),
		DOTNET_WPF("wpf", ".NET WPF GUI", ".cs", new CompileCsClient(".NET WPF GUI", "wpf", "wpf", "./WpfModel.dll", DOTNET_WPF_DEPENDENCIES, true), false),
		PHP("php_client", "PHP client", ".php", new PrepareSources("PHP", "php_client", "Generated-PHP"), true),
		PHP_UI("php_ui", "PHP UI client", "", new PreparePhpUI("PHP UI", "php_ui", "Generated-PHP-UI"), true),
		KNOCKOUT("knockout", "Knockout", ".js", new PrepareSources("knockout", "knockout", "Generated-Knockout"), true),
		SCALA_CLIENT("scala_client", "Scala client", ".scala", new CompileScalaClient("Scala client", "scala-client", "scala_client", "dsl-client-scala_2.11", "./generated-model-scala-client.jar"), true),
		SCALA_POSO("scala_poso", "Plain Old Scala Object", ".scala", new CompileScalaClient("Scala", "scala-poso", "scala_client", null, "./generated-model-scala.jar"), true),
		REVENJ_SCALA("revenj.scala", "Revenj.Scala server for Postgres", ".scala", new CompileRevenjScala("revenj.scala", "revenj-scala"), true),
		REVENJ_SCALA_POSTGRES("scala_server_postgres", "Revenj.Scala server for Postgres", ".scala", new CompileRevenjScala("scala_server_postgres", "revenj-scala"), true),
		SCALA_SERVER_ORACLE("scala_server_oracle", "Scala server (Oracle)", ".scala", new CompileScalaClient("Scala server (Oracle)", "scala-poso", "scala_server_oracle", null, "./generated-scala-oracle.jar"), true);

		private final String value;
		private final String description;
		private final String extension;
		private final BuildAction action;
		private final boolean convertToPath;

		Option(
				final String value,
				final String description,
				final String extension,
				final BuildAction action,
				final boolean convertToPath) {
			this.value = value;
			this.description = description;
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

		@Override
		public String toString() {
			return value;
		}
	}

	private static void listOptions(final Context context) {
		for (final Option o : Option.values()) {
			context.show(o.value + " - " + o.description);
		}
		context.show("Example usages:");
		context.show("\t-target=java_client,revenj.net");
		context.show("\t-java_client -revenj.net=./model/SeverModel.dll");
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
		if (context.contains(INSTANCE)) {
			final String value = context.get(INSTANCE);
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
			if (context.contains(INSTANCE)) {
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
			context.error("Please check your DSL folder: " + context.get(DslPath.INSTANCE));
			return false;
		}
		if (!Settings.hasSourceOnly(context)) {
			for (final Option o : options) {
				if (!o.action.check(context)) {
					return false;
				}
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
		compile(context, targets);
	}

	private void compile(Context context, List<Option> targets) throws ExitException {
		final List<File> dsls = DslPath.getDslPaths(context);
		final List<String> settings = Settings.get(context);
		final String temp = TempPath.getTempProjectPath(context).getAbsolutePath();
		final boolean sourceOnly = Settings.hasSourceOnly(context);
		for (final Option t : targets) {
			Map<String, String> files =
					DslCompiler.compile(
							context,
							t.value,
							settings,
							context.get(Namespace.INSTANCE),
							context.get(Version.INSTANCE),
							dsls);
			try {
				for (final Map.Entry<String, String> kv : files.entrySet()) {
					final String fullName = t.name() + "/" + kv.getKey() + t.extension;
					saveFile(context, temp, t.convertToPath, fullName, kv.getValue());
				}
			} catch (IOException e) {
				context.error("Can't create temporary target file. Compilation results can't be saved locally.");
				context.error(e);
				throw new ExitException();
			}
			context.notify("TARGET", t);
			if (!sourceOnly && t.action != null) {
				t.action.build(new File(temp, t.name()), context);
			}
		}
	}

	private static void saveFile(
			final Context context,
			final String temp,
			final boolean escapeName,
			final String name,
			final String content) throws ExitException, IOException {
		final String cleanName = name.replace(':', '_').replace('<', '_').replace('>', '_');
		final String nameOnly = cleanName.contains(".") ? cleanName.substring(0, cleanName.lastIndexOf('.')) : cleanName;
		final File file = escapeName
				? new File(temp, nameOnly.replace(".", "/").replace("\\", "/") + cleanName.substring(nameOnly.length()))
				: new File(temp, cleanName);
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
		Utils.saveFile(context, file, content);
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