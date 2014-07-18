package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.DotNet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

class DotNetCompilation {

	static Either<String> compile(
			final String[] references,
			final File libraries,
			final File source,
			final File output,
			final Context context) {
		if (output.exists() && !output.isDirectory()) {
			if (!output.delete()) {
				return Either.fail("Failed to remove previous .NET model: " + output.getAbsolutePath());
			}
		} else if (output.exists() && output.isDirectory()) {
			return Either.fail("Expecting to find file. Found folder at: " + output.getAbsolutePath());
		}
		final Either<String> tryCompiler = DotNet.findCompiler(context);
		if (!tryCompiler.isSuccess()) {
			return Either.fail(tryCompiler.whyNot());
		}
		final String compiler = tryCompiler.get();
		final String[] dependencies = libraries.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		final char escapeChar = Utils.isWindows() ? '/' : '-';
		final char separatorChar = Utils.isWindows() ? '\\' : '/';
		final List<String> arguments = new ArrayList<String>();
		arguments.add(escapeChar + "target:library");
		arguments.add(escapeChar + "optimize+");
		arguments.add(escapeChar + "out:\"" + output.getAbsolutePath() + "\"");
		for(final String r : references) {
			arguments.add(escapeChar + "r:\"" + r + "\"");
		}
		for(final String d : dependencies) {
			arguments.add(escapeChar + "r:\"" + d + "\"");
		}
		arguments.add(escapeChar + "lib:\"" + libraries.getAbsolutePath() + "\"");
		arguments.add(escapeChar + "recurse:\"" + source.getAbsolutePath() + separatorChar + "*.cs\"");
		context.show("Compiling Revenj library ...");
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(context, compiler, source, arguments);
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
		return Either.success(compilation.output);
	}
}
