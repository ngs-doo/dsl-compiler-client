package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

import java.io.File;
import java.io.IOException;

public enum Dependencies implements CompileParameter {
	INSTANCE;

	@Override
	public String getAlias() {
		return "dependencies";
	}

	@Override
	public String getUsage() {
		return "path";
	}

	public static File getDependencies(final Context context, final String name, final String library) throws ExitException {
		return getDependencies(context, name, library, null, false);
	}

	public static File getDependenciesIf(final Context context, final String name, final String library, final boolean forceCheck) throws ExitException {
		if (forceCheck) {
			return getDependencies(context, name, library, null, false);
		}
		final boolean hasFolderSpecified = context.contains("dependency:" + library);
		if (hasFolderSpecified) {
			return new File(context.get("dependency:" + library));
		}
		final String depsParam = context.get(Dependencies.INSTANCE);
		return new File(depsParam != null ? depsParam : "./", library.replace('.', '_'));
	}

	public static File getDependencies(
			final Context context,
			final String name,
			final String library,
			final String zip,
			final boolean check) throws ExitException {
		final File dependencies;
		final boolean hasFolderSpecified = context.contains("dependency:" + library);
		if (hasFolderSpecified) {
			dependencies = new File(context.get("dependency:" + library));
		} else {
			final String depsParam = context.get(INSTANCE);
			dependencies = new File(depsParam != null ? depsParam : "./", library.replace('.', '_'));
		}
		if (!dependencies.exists()) {
			if (!dependencies.mkdirs()) {
				context.error("Failed to create " + name + " dependency folder: " + dependencies.getAbsolutePath());
				throw new ExitException();
			}
		} else if (check && context.contains(Download.INSTANCE)) {
			final Either<Long> modified = Download.lastModified(context, zip, name, dependencies.lastModified());
			if (hasFolderSpecified && modified.isSuccess() && dependencies.lastModified() != modified.get()) {
				context.show("Custom dependency folder specified for " + name + ". Skipping update for: " + dependencies.getAbsolutePath());
			} else if (modified.isSuccess() && dependencies.lastModified() != modified.get()) {
				context.show("Different dependencies found in: " + dependencies.getAbsolutePath());
				if (context.contains(Force.INSTANCE) || context.canInteract()) {
					if (context.contains(Force.INSTANCE)) {
						context.show("Due to force option, different dependencies will be removed.");
					} else {
						final String input = context.ask("Clear different dependencies for " + name + " (y/N):");
						if (!"y".equalsIgnoreCase(input)) {
							return dependencies;
						}
					}
					try {
						Utils.deletePath(dependencies);
					} catch (IOException ex) {
						context.error(ex);
						throw new ExitException();
					}
				} else {
					context.show("Unable to interact; skipping clearing different dependencies.");
					context.show("Use force parameter to automatically force latest dependency download.");
				}
			} else if (!modified.isSuccess()) {
				context.warning(modified.whyNot());
			}
		}
		return dependencies;
	}

	@Override
	public boolean check(final Context context) {
		final String value = context.get(INSTANCE);
		if (value != null && value.length() > 0) {
			final File dependenciesPath = new File(value);
			if (!dependenciesPath.exists()) {
				context.error("Dependencies path provided (" + value + ") but not found. Fix the path before continuing compilation");
				return false;
			}
			if (!dependenciesPath.isDirectory()) {
				context.error("Provided dependencies path (" + value + ") is not a directory. Check provided value");
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
		return "Root path to custom dependencies for compilation. Specific target directory will be used";
	}

	@Override
	public String getDetailedDescription() {
		return "DSL client compiler will locally compile sources created by the DSL Platform compiler.\n" +
				"Developers are encouraged to use compiled DLLs and jars instead of embedding code into their project.\n" +
				"Compilation depends on external libraries which are located in the specified path.\n";
	}
}
