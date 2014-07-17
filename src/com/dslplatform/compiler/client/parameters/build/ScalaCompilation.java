package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.ExitException;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.JavaPath;
import com.dslplatform.compiler.client.parameters.ScalaPath;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class ScalaCompilation {

	private static List<File> findNonEmptyDirsFiles(final File path) {
		final List<File> foundFiles = new LinkedList<File>();
		findNonEmptyDirsFiles(path, foundFiles);
		return foundFiles;
	}

	private static void findNonEmptyDirsFiles(final File path, final List<File> foundFiles) {
		for (final String fn : path.list()) {
			final File f = new File(path, fn);
			if (f.isDirectory()) {
				findNonEmptyDirsFiles(f, foundFiles);
				if (f.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".scala");
					}
				}).length > 0) {
					foundFiles.add(f);
				}
			}
		}
	}

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
		final String javac = tryCompiler.get();
		final File classOut = new File(source, "locally-compiled");
		if (classOut.exists() && !classOut.delete()) {
			return Either.fail("Can't remove folder with compiled files: " + classOut.getAbsolutePath());
		}
		if (!classOut.mkdirs()) {
			return Either.fail("Error creating temporary folder for Scala class files: " + classOut.getAbsolutePath());
		}
		final int len = source.getAbsolutePath().length() + 1;
		final char classpathSeparator = Utils.isWindows() ? ';' : ':';
		final char separatorChar = Utils.isWindows() ? '\\' : '/';
		final List<File> scalaDirs = findNonEmptyDirsFiles(source);

		final StringBuilder scalacCommand = new StringBuilder(javac);
		scalacCommand.append(" -encoding UTF8 ");
		final File[] externalJars = libraries.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		scalacCommand.append("-d locally-compiled -cp .");
		for (final File j : externalJars) {
			scalacCommand.append(classpathSeparator).append("\"").append(j.getAbsolutePath()).append("\"");
		}
		for (final File f : scalaDirs) {
			scalacCommand.append(" ").append(f.getAbsolutePath().substring(len)).append(separatorChar).append("*.scala");
		}

		context.show("Running scalac for " + output.getName() + " ...");
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(scalacCommand.toString(), source);
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

		final Either<Utils.CommandResult> tryArchive =
				JavaPath.makeArchive(context, source, classOut, output, scalaDirs);
		if (!tryArchive.isSuccess()) {
			return Either.fail(tryArchive.whyNot());
		}
		return Either.success(compilation.output);
	}
}
