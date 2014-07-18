package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.JavaPath;
import com.dslplatform.compiler.client.parameters.ScalaPath;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

class ScalaCompilation {

	static Either<String> compile(
			final File libraries,
			final File source,
			final File output,
			final Context context) {
		if (output.exists() && !output.isDirectory()) {
			if (!output.delete()) {
				return Either.fail("Failed to remove previous Scala model: " + output.getAbsolutePath());
			}
		} else if (output.exists() && output.isDirectory()) {
			return Either.fail("Expecting to find file. Found folder at: " + output.getAbsolutePath());
		}
		final Either<String> tryCompiler = ScalaPath.findCompiler(context);
		if (!tryCompiler.isSuccess()) {
			return Either.fail(tryCompiler.whyNot());
		}
		final String scalac = tryCompiler.get();
		final File classOut = new File(source, "locally-compiled");
		if (classOut.exists() && !classOut.delete()) {
			return Either.fail("Can't remove folder with compiled files: " + classOut.getAbsolutePath());
		}
		if (!classOut.mkdirs()) {
			return Either.fail("Error creating temporary folder for Scala class files: " + classOut.getAbsolutePath());
		}
		final char classpathSeparator = Utils.isWindows() ? ';' : ':';
		final File[] externalJars = libraries.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (externalJars.length == 0) {
			return Either.fail("Unable to find dependencies in: " + libraries.getAbsolutePath());
		}

		final List<String> scalacArguments = new ArrayList<String>();
		scalacArguments.add("-encoding");
		scalacArguments.add("UTF8");
		scalacArguments.add("-optimise");
		scalacArguments.add("-d");
		scalacArguments.add("locally-compiled");
		scalacArguments.add("-classpath");
		final StringBuilder classPath = new StringBuilder(".");
		for (final File j : externalJars) {
			classPath.append(classpathSeparator).append(j.getAbsolutePath());
		}
		scalacArguments.add("\"" + classPath.toString() + "\"");
		scalacArguments.add("*.scala");

		context.show("Running scalac for " + output.getName());
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(context, scalac, source, scalacArguments);
		if (!execCompile.isSuccess()) {
			return Either.fail(execCompile.whyNot());
		}
		final Utils.CommandResult compilation = execCompile.get();
		if (compilation.error.length() > 0) {
			return Either.fail(compilation.error);
		}
		if (compilation.output.contains("error")) {
			final StringBuilder sb = new StringBuilder();
			for (final String e : compilation.output.split("\n")) {
				if (e.contains("error")) {
					sb.append(e).append("\n");
				}
			}
			if (sb.length() > 0) {
				return Either.fail(sb.toString());
			}
			return Either.fail(compilation.output);
		}

		final Either<Utils.CommandResult> tryArchive = JavaPath.makeArchive(context, source, classOut, output);
		if (!tryArchive.isSuccess()) {
			return Either.fail(tryArchive.whyNot());
		}
		return Either.success(compilation.output);
	}
}
