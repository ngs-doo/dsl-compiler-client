package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.JavaPath;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

class JavaCompilation {

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
						return name.endsWith(".java");
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
		final Either<String> tryCompiler = JavaPath.findCompiler(context);
		final Either<String> tryArchive = JavaPath.findArchive(context);
		if(!tryCompiler.isSuccess()) {
			return Either.fail(tryCompiler.whyNot());
		}
		final String javac = tryCompiler.get();
		final String jar = tryArchive.get();
		final File classOut = new File(source, "locally-compiled");
		if (classOut.exists() && !classOut.delete()) {
			return Either.fail("Can't remove folder with compiled files: " + classOut.getAbsolutePath());
		}
		if (!classOut.mkdirs()) {
			return Either.fail("Error creating temporary folder for Java class files: " + classOut.getAbsolutePath());
		}
		final int len = source.getAbsolutePath().length() + 1;
		final char classpathSeparator = Utils.isWindows() ? ';' : ':';
		final char separatorChar = Utils.isWindows() ? '\\' : '/';
		final List<File> javaDirs = findNonEmptyDirsFiles(source);

		final StringBuilder javacCommand = new StringBuilder(javac);
		javacCommand.append(" -encoding UTF8 ");
		final File[] externalJars = libraries.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
		javacCommand.append("-d locally-compiled -cp .");
		for(final File j : externalJars) {
			javacCommand.append(classpathSeparator).append("\"").append(j.getAbsolutePath()).append("\"");
		}
		for(final File f : javaDirs) {
			javacCommand.append(" ").append(f.getAbsolutePath().substring(len)).append(separatorChar).append("*.java");
		}

		final StringBuilder jarCommand = new StringBuilder(jar);
		jarCommand.append(" cf \"").append(output.getAbsolutePath()).append("\"");
		for(final File f : javaDirs) {
			jarCommand.append(" ").append(f.getAbsolutePath().substring(len)).append(separatorChar).append("*.class");
		}

		context.show("Running javac for " + output.getName() + " ...");
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(javacCommand.toString(), source);
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

		context.show("Running jar for " + output.getName() + " ...");
		final Either<Utils.CommandResult> execArchive = Utils.runCommand(jarCommand.toString(), classOut);
		if (!execArchive.isSuccess()) {
			return Either.fail(tryArchive.whyNot());
		}
		final Utils.CommandResult archiving = execArchive.get();
		if (archiving.error.length() > 0) {
			return Either.fail(archiving.error);
		}
		return Either.success(compilation.output);
	}
}
