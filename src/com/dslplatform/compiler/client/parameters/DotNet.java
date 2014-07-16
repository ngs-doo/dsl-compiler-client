package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.util.Map;

public enum DotNet implements CompileParameter {
	INSTANCE;

	public static Either<String> findCompiler(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.DOTNET)) {
			return Either.success(parameters.get(InputParameter.DOTNET));
		} else {
			final boolean isWindows = System.getProperty("os.name").contains("Windows");
			final boolean is32Bit = System.getProperty("os.arch").equals("x86");
			if (isWindows) {
				if (Utils.testCommand("csc.exe", ".NET Framework")) {
					return Either.success("csc.exe");
				}
				final String framework = is32Bit ? "Framework" : "Framework64";
				final String msDotNet4 = System.getenv("WINDIR") + "\\Microsoft.NET\\" + framework + "\\v4.0.30319\\csc.exe";
				if (Utils.testCommand(msDotNet4, ".NET Framework")) {
					return Either.success(msDotNet4);
				}
				return Either.fail("Unable to find csc.exe (.NET C# compiler). Add it to path or specify dotnet compile option.");
			}
			if (Utils.testCommand("dmcs", "Mono")) {
				return Either.success("dmcs");
			}
			return Either.fail("Unable to find dmcs (Mono C# compiler). Add it to path or specify dotnet compile option.");
		}
	}

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
		if (parameters.containsKey(InputParameter.DOTNET)) {
			final String compiler = parameters.get(InputParameter.DOTNET);
			final boolean isWindows = System.getProperty("os.name").contains("Windows");
			if (isWindows && !Utils.testCommand(compiler, ".NET Framework") && !Utils.testCommand(compiler, "Mono")
					|| !isWindows && !Utils.testCommand(compiler, "Mono")) {
				System.out.println("dotnet parameter is set, but .NET/Mono compiler not found/doesn't work. Please check specified dotnet parameter.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void run(final Map<InputParameter, String> parameters) {
	}

	@Override
	public String getShortDescription() {
		return "specify custom .NET/Mono compiler";
	}

	@Override
	public String getDetailedDescription() {
		return "To compile .NET libraries Mono/.NET compiler is required.\n" +
				"In Windows csc.exe is usually located in %WINDIR%\\Microsoft.NET\\Framework while on Linux mono is usually available via command line.\n" +
				"If custom path is required this option can be used to specify it.\n" +
				"\n" +
				"Example:\n" +
				"	/var/user/mono-3.4/dmsc";
	}
}
