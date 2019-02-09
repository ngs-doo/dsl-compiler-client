package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;

import java.util.HashMap;
import java.util.Map;

public enum Nuget implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() { return "nuget"; }
	@Override
	public String getUsage() { return "dependencies"; }

	private static final String CACHE = "nuget_dependencies";

	public static Map<String, String> getNugets(final Context context) {
		return context.load(CACHE);
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(INSTANCE)) {
			final Either<String> tooling = DotNet.findCompiler(context, DotNet.CompilerVersion.NewDotNet);
			if (!tooling.isSuccess()) {
				context.error("New dotnet tooling is required for nuget to work.");
				context.error(tooling.whyNot());
				return false;
			}
			final Map<String, String> deps = new HashMap<String, String>();
			final String arguments = context.get(INSTANCE);
			if (arguments != null && !arguments.isEmpty()) {
				final String[] dependencies = arguments.split(",");
				for (String d : dependencies) {
					if (d.isEmpty()) {
						context.error("Empty dependencies argument detected in " + arguments);
						return false;
					} else if (d.contains("\"")) {
						context.error("Invalid character detected in " + arguments);
						return false;
					}
					final String[] parts = d.split(":");
					if (parts.length != 2) {
						context.error("Invalid pattern used for dependencies. Expecting dependency:version. Found: " + d);
						return false;
					} else if (deps.containsKey(parts[0])) {
						context.error("Dependency specified multiple times: " + parts[0]);
						return false;
					} else if (parts[0].isEmpty()) {
						context.error("Empty dependency value in: " + d);
						return false;
					} else if (parts[1].isEmpty()) {
						context.error("Empty dependency argument for: " + parts[0]);
						return false;
					}
					deps.put(parts[0], parts[1]);
				}
			}
			context.cache(CACHE, deps);
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "Specify Nuget dependencies for new Dotnet tooling";
	}

	@Override
	public String getDetailedDescription() {
		return "To build the DLL using the new dotnet tooling, specify nuget dependencies.\n" +
				"If dotnet is not in path, when this option is specified, dotnet option will probe for dotnet tooling.\n" +
				"\n" +
				"Examples:\n" +
				"\trevenj:1.5.0,custom:[0.5.4]";
	}
}
