package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.json.JsonObject;
import com.dslplatform.compiler.client.parameters.compilation.CompileAction;
import com.dslplatform.compiler.client.parameters.compilation.CompileJavaClient;
import com.dslplatform.compiler.client.parameters.compilation.CompileRevenj;

import java.io.*;
import java.util.Map;

public enum Targets implements CompileParameter {
	INSTANCE;

	public static enum Option {
		JAVA_CLIENT("java_client", "Java client", "Java", new CompileJavaClient()),
		REVENJ("revenj", "Revenj .NET server", "CSharpServer", new CompileRevenj()),
		PHP("php", "PHP client", "PHP", null),
		SCALA_CLIENT("scala_client", "Scala client", "ScalaClient", null);

		private final String value;
		private final String description;
		private final String platformName;
		private final CompileAction action;

		Option(final String value, final String description, final String platformName, final CompileAction action) {
			this.value = value;
			this.description = description;
			this.platformName = platformName;
			this.action = action;
		}

		private static Option from(final String value) {
			for(final Option o : Option.values()) {
				if (o.value.equalsIgnoreCase(value)) {
					return o;
				}
			}
			return null;
		}
	}

	private static void listOptions() {
		for(final Option o : Option.values()) {
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
		for(final String t : targets) {
			final Option o = Option.from(t);
			if (o == null) {
				System.out.println("Unknown target: " + t);
				listOptions();
				return false;
			}
		}
		final Map<String, String> dsls = DslPath.getCurrentDsl(parameters);
		if (dsls.size() == 0) {
			System.out.println("Can't compile DSL to targets since no DSL was provided. Please check your DSL folder.");
			return false;
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
		for (int i=0;i<targets.length;i++) {
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
			System.out.println("Error compiling DSL to specified targets:");
			System.out.println(response.whyNot());
			System.exit(0);
		}
		final JsonObject files = JsonObject.readFrom(response.get());
		final String temp = TempPath.getTempPath().getAbsolutePath();
		try {
			for(final String name : files.names()) {
				final File file = new File(temp + "/" + name);
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
		for(final Option t : targets) {
			if (t.action != null) {
				t.action.compile(new File(temp + "/" + t.platformName), parameters);
			}
		}
	}

	@Override
	public String getShortDescription() {
		return "Convert DSL to specified targets (Java client, PHP, Revenj server, ...)";
	}

	@Override
	public String getDetailedDescription() {
		return null;
	}
}
