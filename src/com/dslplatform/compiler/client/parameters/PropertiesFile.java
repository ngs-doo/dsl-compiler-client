package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum PropertiesFile implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.PROPERTIES)) {
			final String value = context.get(InputParameter.PROPERTIES);
			if (value == null || value.length() == 0) {
				context.error("Incorrectly defined .properties file");
				return false;
			}
			final File file = new File(value);
			if (!file.exists()) {
				context.error("Can't find specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final Either<String> content = Utils.readFile(file);
			if (!content.isSuccess()) {
				context.error("Error reading specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final List<String> errors = new ArrayList<String>();
			for (final String row : content.get().split("\n")) {
				final String line = row.trim();
				if (line.length() == 0 || line.startsWith("#") || line.startsWith("//")) {
					continue;
				}
				final int eq = line.indexOf('=');
				final String name = line.substring(0, eq != -1 ? eq : line.length());
				final InputParameter cp = InputParameter.from(name);
				if (cp == null) {
					if (Targets.Option.from(name) != null) {
						context.put(name, eq == -1 ? null : line.substring(eq + 1));
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
						errors.add("Expecting " + cp.usage + " after = for " + line);
					} else {
						context.put(cp, eq == -1 ? null : line.substring(eq + 1));
					}
				}
			}
			if (errors.size() > 0) {
				context.error("Errors found in properties file: " + file.getAbsolutePath());
				for (final String err : errors) {
					context.error(err);
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Context context) {
	}

	@Override
	public String getShortDescription() {
		return "use file with predefined arguments";
	}

	@Override
	public String getDetailedDescription() {
		return "Instead of passing arguments special file can be used instead.\n" +
				"A simple properties file with the same exact names for properties.\n" +
				"\n" +
				"Example compile_java.properties:\n" +
				"	target=java_client\n" +
				"	db=localhost/Database?user=user&password=password\n" +
				"	download\n" +
				"	migration\n" +
				"	apply\n";
	}
}
