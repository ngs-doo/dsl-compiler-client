package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

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
		final String compiler;
		if (parameters.containsKey(InputParameter.DOTNET)) {
			compiler = parameters.get(InputParameter.DOTNET);
		} else {
			final Either<String> tryCompiler = findDotNetCompiler();
			if (tryCompiler.isSuccess()) {
				compiler = tryCompiler.get();
			} else {
				return Either.fail("Unable to find .NET/Mono compiler. Use dotnet compilation option to specify exact location.");
			}
		}
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
		try {
			final Process compilation = Runtime.getRuntime().exec(command.toString());
			final String result = compilation.getInputStream() != null ? Utils.read(compilation.getInputStream()) : "";
			final String error = compilation.getErrorStream() != null ? Utils.read(compilation.getErrorStream()) : "";
			compilation.waitFor();
			if (error.length() > 0) {
				return Either.fail(error);
			}
			if (result.contains("error")) {
				final StringBuilder sb = new StringBuilder();
				for(final String e : result.split("\n")) {
					sb.append(e).append("\n");
				}
				if (sb.length() > 0) {
					return Either.fail(sb.toString());
				}
				return Either.fail(result);
			}
			return Either.success(result);
		}catch (Exception ex) {
			return Either.fail(ex.getMessage());
		}
	}

	private static boolean testCommand(final String command, final String contains) {
		try {
			final Process compilation = Runtime.getRuntime().exec(command);
			final String result = compilation.getInputStream() != null ? Utils.read(compilation.getInputStream()) : "";
			final String error = compilation.getErrorStream() != null ? Utils.read(compilation.getErrorStream()) : "";
			compilation.waitFor();
			return error.length() == 0 && result.contains(contains);
		} catch (Exception ex) {
			return false;
		}
	}

	private static Either<String> findDotNetCompiler() {
		final boolean isWindows = System.getProperty("os.name").contains("Windows");
		final boolean is32Bit = System.getProperty("os.arch").equals("x86");
		if (isWindows) {
			if (testCommand("csc.exe", ".NET Framework")) {
				return Either.success("csc.exe");
			}
			final String framework = is32Bit ? "Framework" : "Framework64";
			final String msDotNet4 = System.getenv("WINDIR") + "\\Microsoft.NET\\" + framework + "\\v4.0.30319\\csc.exe";
			if (testCommand(msDotNet4, ".NET Framework")) {
				return Either.success(msDotNet4);
			}
			return Either.fail("Unable to find csc.exe. Add it to path or specify dotnet compile option.");
		} else {
			if (testCommand("dmcs", "Mono")) {
				return Either.success("dmcs");
			}
			return Either.fail("Unable to find dmcs. Add it to path or specify dotnet compile option.");
		}
	}
}
