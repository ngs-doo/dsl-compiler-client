package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum PropertiesFile implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.PROPERTIES)) {
			final String value = parameters.get(InputParameter.PROPERTIES);
			if (value == null || value.length() == 0) {
				System.out.println("Incorrectly defined .properties file");
				return false;
			}
			final File file = new File(value);
			if (!file.exists()) {
				System.out.println("Can't find specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final Either<String> content = Utils.readFile(file);
			if (!content.isSuccess()) {
				System.out.println("Error reading specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final Map<InputParameter, String> options = new HashMap<InputParameter, String>();
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
					errors.add("Unknown parameter: " + name);
				} else {
					if (eq == -1 && cp.usage != null) {
						errors.add("Expecting " + cp.usage + " after = for " + line);
					} else {
						options.put(cp, name.length() + 1 == line.length() ? null : line.substring(eq + 1));
					}
				}
			}
			if (errors.size() > 0) {
				System.out.println("Errors found in properties file: " + file.getAbsolutePath());
				for (final String err : errors) {
					System.out.println(err);
				}
				return false;
			}
			if (options.size() == 0) {
				System.out.println("Properties file defined, but no arguments detected: " + file.getAbsolutePath());
				return false;
			}
			for(final Map.Entry<InputParameter, String> kv : options.entrySet()) {
				if (!parameters.containsKey(kv.getKey())) {
					parameters.put(kv.getKey(), kv.getValue());
				}
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
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
