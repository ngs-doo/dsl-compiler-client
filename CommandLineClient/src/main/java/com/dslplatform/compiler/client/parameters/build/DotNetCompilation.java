package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Utils;
import com.dslplatform.compiler.client.parameters.DotNet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DotNetCompilation {

	static Either<String> compileLegacy(
			final String[] references,
			final File libraries,
			final File source,
			final File output,
			final Context context,
			final boolean force32Bit) {
		String outError = checkOutput(output, context);
		if (outError != null) return Either.fail(outError);
		final Either<String> tryCompiler = force32Bit ? DotNet.findCompiler(context, DotNet.CompilerVersion.Legacy32bit) : DotNet.findCompiler(context);
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
		final List<String> arguments = new ArrayList<String>();
		arguments.add(escapeChar + "target:library");
		arguments.add(escapeChar + "optimize+");
		if (force32Bit) {
			arguments.add(escapeChar + "platform:x86");
		}
		arguments.add(escapeChar + "out:" + output.getAbsolutePath());
		for (final String r : references) {
			arguments.add(escapeChar + "r:" + r);
		}
		if (dependencies != null) {
			for (final String d : dependencies) {
				arguments.add(escapeChar + "r:" + d);
			}
		}
		arguments.add(escapeChar + "lib:" + libraries.getAbsolutePath());
		arguments.add(escapeChar + "warn:0");
		arguments.add(escapeChar + "recurse:*.cs");
		context.notify("CSC", arguments);
		return runCompilation(source, context, compiler, arguments);
	}

	static Either<String> compileNewDotnet(
			final Map<String, String> nugets,
			final File libraries,
			final File source,
			final File output,
			final Context context) {
		String outError = checkOutput(output, context);
		if (outError != null) return Either.fail(outError);
		final Either<String> tryCompiler = DotNet.findCompiler(context, DotNet.CompilerVersion.NewDotNet);
		if (!tryCompiler.isSuccess()) {
			return Either.fail(tryCompiler.whyNot());
		}
		final String compiler = tryCompiler.get();
		final String[] dependencies = libraries == null ? null : libraries.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".dll");
			}
		});
		String projectName = output.getName().toLowerCase().endsWith(".dll")
				? output.getName().substring(0, output.getName().length() - 4)
				: output.getName();
		final StringBuilder csproj = new StringBuilder();
		csproj.append("<Project Sdk=\"Microsoft.NET.Sdk\">\n");
		csproj.append("\t<PropertyGroup>\n");
		csproj.append("\t\t<TargetFramework>netstandard2.0</TargetFramework>\n");
		csproj.append("\t\t<GenerateAssemblyInfo>false</GenerateAssemblyInfo>\n");
		csproj.append("\t\t<AssemblyName>").append(projectName).append("</AssemblyName>\n");
		csproj.append("\t\t<AppendTargetFrameworkToOutputPath>false</AppendTargetFrameworkToOutputPath>\n");
		csproj.append("\t</PropertyGroup>\n");
		csproj.append("\t<ItemGroup>\n");
		for (String key : nugets.keySet()) {
			csproj.append("\t\t<PackageReference Include=\"").append(key).append("\" Version=\"").append(nugets.get(key)).append("\" />\n");
		}
		if (dependencies != null) {
			for (String d : dependencies) {
				String fileName = new File(d).getName();
				String depName = fileName.toLowerCase().endsWith(".dll") ? fileName.substring(0, fileName.length() - 4) : fileName;
				csproj.append("\t\t<Reference Include=\"").append(depName).append("\">\n");
				csproj.append("\t\t\t<HintPath>").append(d).append("</HintPath>\n");
				csproj.append("\t\t</Reference>\n");
			}
		}
		csproj.append("\t</ItemGroup>\n");
		csproj.append("</Project>");
		final File projFile = new File(source, projectName + ".csproj");
		try {
			Utils.saveFile(context, projFile, csproj.toString());
		} catch (IOException e) {
			return Either.fail("Unable to create csproj file: " + e.getMessage(), e);
		}
		final List<String> arguments = new ArrayList<String>();
		arguments.add("build");
		arguments.add("--configuration");
		arguments.add("Release");
		arguments.add("--output");
		arguments.add(output.getParentFile().getAbsolutePath());
		arguments.add(projFile.getAbsolutePath());
		context.notify("dotnet", arguments);
		return runCompilation(source, context, compiler, arguments);
	}

	private static Either<String> runCompilation(File source, Context context, String compiler, List<String> arguments) {
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
		} else if (compilation.output.contains(" Errors(s)") && !compilation.output.contains("0 Errors(s)")) {
			return Either.fail(compilation.output);
		}
		return Either.success(compilation.output);
	}

	private static String checkOutput(File output, Context context) {
		if (output.exists() && !output.isDirectory()) {
			if (!output.delete()) {
				return "Failed to remove previous .NET model: " + output.getAbsolutePath();
			}
		} else if (output.exists() && output.isDirectory()) {
			return "Expecting to find file. Found folder at: " + output.getAbsolutePath();
		}
		if (output.getParentFile() != null && !output.getParentFile().exists()) {
			context.show("Output folder not found. Will create one in: " + output.getParent());
			if (!output.getParentFile().mkdirs()) {
				return "Unable to create output folder for: " + output.getAbsolutePath();
			}
		}
		return null;
	}
}
