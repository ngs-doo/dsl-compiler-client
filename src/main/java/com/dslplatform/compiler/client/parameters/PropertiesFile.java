package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PropertiesFile implements CompileParameter {

	private final List<CompileParameter> parameters;

	public PropertiesFile(List<CompileParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String getAlias() { return "properties"; }
	@Override
	public String getUsage() { return "file"; }

	@Override
	public boolean check(final Context context) {
		if (context.contains(this)) {
			final String properties = context.get(this);
			if (properties == null || properties.length() == 0) {
				context.error("Incorrectly defined .properties file");
				return false;
			}
			final File file = new File(properties);
			if (!file.exists()) {
				context.error("Can't find specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final Either<String> content = Utils.readFile(file);
			if (!content.isSuccess()) {
				context.error("Error reading specified properties file: " + file.getAbsolutePath());
				return false;
			}
			final List<ParameterParser> customParsers = new ArrayList<ParameterParser>();
			for (final CompileParameter cp : parameters) {
				if (cp instanceof ParameterParser) {
					customParsers.add((ParameterParser) cp);
				}
			}
			final List<String> errors = new ArrayList<String>();
			for (final String row : content.get().split("\n")) {
				final String line = row.trim();
				if (line.length() == 0 || line.startsWith("#") || line.startsWith("//")) {
					continue;
				}
				final int eq = line.indexOf('=');
				final String name = line.substring(0, eq != -1 ? eq : line.length());
				final String value = eq == -1 ? null : line.substring(eq + 1);
				final CompileParameter cp = from(name);
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
								errors.add("Expecting " + cp.getUsage() + " after = for " + line);
							}
						} else {
							errors.add("Expecting " + cp.getUsage() + " after = for " + line);
						}
					} else {
						context.put(cp, value);
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

	private CompileParameter from(final String value) {
		for (final CompileParameter cp : parameters) {
			if (cp.getAlias().equalsIgnoreCase(value)) {
				return cp;
			}
		}
		return null;
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
