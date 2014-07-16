package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.DotNet;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

public class DotNetCompilation {

	public static Either<String> compile(
			final String[] references,
			final File libraries,
			final File source,
			final File output,
			final Map<InputParameter, String> parameters) {
		final Either<String> tryCompiler = DotNet.findCompiler(parameters);
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
		final char escapeChar = System.getProperty("os.name").contains("Windows") ? '/' : '-';
		final char separatorChar = System.getProperty("os.name").contains("Windows") ? '\\' : '/';
		final StringBuilder command = new StringBuilder(compiler);
		command.append(" ").append(escapeChar).append("target:library ");
		command.append(escapeChar).append("optimize+ ");
		command.append(escapeChar).append("out:\"").append(output.getAbsoluteFile()).append("\" ");
		for(final String r : references) {
			command.append(escapeChar).append("r:\"").append(r).append("\" ");
		}
		for(final String d : dependencies) {
			command.append(escapeChar).append("r:\"").append(d).append("\" ");
		}
		command.append(escapeChar).append("lib:\"").append(libraries.getAbsolutePath()).append("\" ");
		command.append(escapeChar).append("recurse:\"").append(source.getAbsolutePath()).append(separatorChar).append("*.cs\" ");
		System.out.println("Compiling Revenj library...");
		final Either<Utils.CommandResult> execCompile = Utils.runCommand(command.toString(), source);
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
