package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonObject;
import com.dslplatform.compiler.client.parameters.compilation.CompileAction;
import com.dslplatform.compiler.client.parameters.compilation.CompileJavaClient;
import com.dslplatform.compiler.client.parameters.compilation.CompileRevenj;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Targets implements CompileParameter {
	INSTANCE;

	public static enum Option {
		JAVA_CLIENT("java_client", "Java client", "Java", new CompileJavaClient(), true),
		REVENJ("revenj", "Revenj .NET server", "CSharpServer", new CompileRevenj(), false),
		PHP("php", "PHP client", "PHP", null, true),
		SCALA_CLIENT("scala_client", "Scala client", "ScalaClient", null, true);

		private final String value;
		private final String description;
		private final String platformName;
		private final CompileAction action;
		private final boolean convertToPath;

		Option(
				final String value,
				final String description,
				final String platformName,
				final CompileAction action,
				final boolean convertToPath) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
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

	private static void listOptions() {
		for (final Option o : Option.values()) {
			System.out.println(o.value + " - " + o.description);
		}
		System.out.println("Example usage: -target=java_client,revenj");
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (!parameters.containsKey(InputParameter.TARGET)) {
			return true;
		}
		final String value = parameters.get(InputParameter.TARGET);
		final String[] targets = value != null ? value.split(",") : new String[0];
		if (targets.length == 0) {
			System.out.println("Targets not provided. Available targets: ");
			listOptions();
			return false;
		}
		final Option[] options = new Option[targets.length];
		for (int i = 0; i < targets.length; i++) {
			final String t = targets[i];
			final Option o = Option.from(t);
			if (o == null) {
				System.out.println("Unknown target: " + t);
				listOptions();
				return false;
			}
			options[i] = o;
		}
		final Map<String, String> dsls = DslPath.getCurrentDsl(parameters);
		if (dsls.size() == 0) {
			System.out.println("Can't compile DSL to targets since no DSL was provided.");
			System.out.println("Please check your DSL folder: " + parameters.get(InputParameter.DSL));
			return false;
		}
		for(final Option o : options) {
			if (!o.action.check(parameters)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
		if (!parameters.containsKey(InputParameter.TARGET)) {
			return;
		}
		final String[] targetsInputs = parameters.get(InputParameter.TARGET).split(",");
		final Option[] targets = new Option[targetsInputs.length];
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < targets.length; i++) {
			targets[i] = Option.from(targetsInputs[i]);
			sb.append(targets[i].platformName);
			sb.append(',');
		}
		final Map<String, String> dsls = DslPath.getCurrentDsl(parameters);
		final StringBuilder url = new StringBuilder("Platform.svc/unmanaged/source?targets=");
		url.append(sb.substring(0, sb.length() - 1));
		if (parameters.containsKey(InputParameter.NAMESPACE)) {
			url.append("&namespace=").append(parameters.get(InputParameter.NAMESPACE));
		}
		final String settings = Settings.parseAndConvert(parameters);
		if (settings.length() > 0) {
			url.append("&options=").append(settings);
		}
		final Either<String> response = DslServer.put(url.toString(), parameters, Utils.toJson(dsls));
		if (!response.isSuccess()) {
			System.out.println("Error compiling DSL to specified target.");
			System.out.println(response.whyNot());
			System.exit(0);
		}
		final JsonObject files = JsonObject.readFrom(response.get());
		final String temp = TempPath.getTempPath().getAbsolutePath();
		final Set<String> escapeNames = new HashSet<String>();
		for (final Option t : targets) {
			if (t.convertToPath) {
				escapeNames.add(t.platformName);
			}
		}
		try {
			for (final String name : files.names()) {
				final String nameOnly = name.substring(0, name.lastIndexOf('.'));
				final File file = name.contains("/") && escapeNames.contains(name.substring(0, name.indexOf("/")))
					? new File(temp, nameOnly.replace(".", "/") + name.substring(nameOnly.length()))
					: new File(temp, name);
				final File parentPath = file.getParentFile();
				if (!parentPath.exists()) {
					if (!parentPath.mkdirs()) {
						System.out.println("Failed creating path for target file: " + parentPath.getAbsolutePath());
						System.exit(0);
					}
				}
				if (!file.createNewFile()) {
					System.out.println("Failed creating target file: " + file.getAbsolutePath());
					System.exit(0);
				}
				Utils.saveFile(file, files.get(name).asString());
			}
		} catch (IOException e) {
			System.out.println("Can't create temporary target file. Compilation results can't be saved locally.");
			System.out.println(e.getMessage());
			System.exit(0);
		}
		for (final Option t : targets) {
			if (t.action != null) {
				t.action.compile(new File(temp, t.platformName), parameters);
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
		sb.append("DSL Platform converts DSL model to various target sources which are then locally compiled (if possible).\n");
		sb.append("This option specifies which target sources are available.\n");
		sb.append("---------------------------------------------------------\n");
		for (final Option o : Option.values()) {
			sb.append(o.value).append(" - ").append(o.description).append("\n");
		}
		return sb.toString();
	}
}
