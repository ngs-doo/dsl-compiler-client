package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.JavaPath;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class JavaCompilation {

	static Either<String> compile(
			final String name,
			final File libraries,
			final File source,
			final File output,
			final Context context) {
		if (output.exists() && !output.isDirectory()) {
			if (!output.delete()) {
				return Either.fail("Failed to remove previous Java model: " + output.getAbsolutePath());
			}
		} else if (output.exists() && output.isDirectory()) {
			return Either.fail("Expecting to find file. Found folder at: " + output.getAbsolutePath());
		}
		final Either<String> tryCompiler = JavaPath.findCompiler(context);
		if (!tryCompiler.isSuccess()) {
			return Either.fail(tryCompiler.whyNot());
		}
		final String javac = tryCompiler.get();
		final File classOut = new File(source, name);
		if (classOut.exists() && !classOut.delete()) {
			return Either.fail("Can't remove folder with compiled files: " + classOut.getAbsolutePath());
		}
		if (!classOut.mkdirs()) {
			return Either.fail("Error creating temporary folder for Java class files: " + classOut.getAbsolutePath());
		}
		final int len = source.getAbsolutePath().length() + 1;
		final char classpathSeparator = Utils.isWindows() ? ';' : ':';
		final char separatorChar = Utils.isWindows() ? '\\' : '/';
		final File[] externalJars = libraries.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		if (externalJars.length == 0) {
			return Either.fail("Unable to find dependencies in: " + libraries.getAbsolutePath());
		}

		final List<String> javacArguments = new ArrayList<String>();
		javacArguments.add("-encoding");
		javacArguments.add("UTF8");
		javacArguments.add("-d");
		javacArguments.add(name);
		javacArguments.add("-cp");
		final StringBuilder classPath = new StringBuilder(".");
		for (final File j : externalJars) {
			classPath.append(classpathSeparator).append(j.getAbsolutePath());
		}
		javacArguments.add(classPath.toString());
		if(Utils.isWindows()) {
			final List<File> javaDirs = Utils.findNonEmptyDirs(source, ".java");
			if(javaDirs.size() == 0) {
				return Either.fail("Unable to find Java generated sources in: " + source.getAbsolutePath());
			}
			for (final File f : javaDirs) {
				javacArguments.add(f.getAbsolutePath().substring(len) + separatorChar + "*.java");
			}
		} else {
			final List<File> javaFiles = Utils.findFiles(source, Arrays.asList(".java"));
			for (final File f : javaFiles) {
				javacArguments.add(f.getAbsolutePath().substring(len));
			}
		}
		context.show("Running javac for " + output.getName() + " ...");
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(context, javac, source, javacArguments);
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
