package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.*;

public enum DotNet implements CompileParameter {
	INSTANCE;

	public static Either<String> findCompiler(final Context context) {
		if (context.contains(InputParameter.DOTNET)) {
			return Either.success(context.get(InputParameter.DOTNET));
		} else {
			final boolean isWindows = Utils.isWindows();
			final boolean is32Bit = System.getProperty("os.arch").equals("x86");
			if (isWindows) {
				if (Utils.testCommand(context, "csc.exe", ".NET Framework")) {
					return Either.success("csc.exe");
				}
				final String framework = is32Bit ? "Framework" : "Framework64";
				final String msDotNet4 = System.getenv("WINDIR") + "\\Microsoft.NET\\" + framework + "\\v4.0.30319\\csc.exe";
				if (Utils.testCommand(context, msDotNet4, ".NET Framework")) {
					return Either.success(msDotNet4);
				}
				return Either.fail("Unable to find csc.exe (.NET C# compiler). Add it to path or specify dotnet compile option.");
			}
			if (Utils.testCommand(context, "dmcs", "Mono")) {
				return Either.success("dmcs");
			}
			return Either.fail("Unable to find dmcs (Mono C# compiler). Add it to path or specify dotnet compile option.");
		}
	}

	@Override
	public boolean check(final Context context) {
		if (context.contains(InputParameter.DOTNET)) {
			final String compiler = context.get(InputParameter.DOTNET);
			final boolean isWindows = Utils.isWindows();
			//TODO: should we even ask for Mono on Windows?
			if (isWindows && !Utils.testCommand(context, compiler, ".NET Framework") && !Utils.testCommand(context, compiler, "Mono")
					|| !isWindows && !Utils.testCommand(context, compiler, "Mono")) {
				context.error("dotnet parameter is set, but .NET/Mono compiler not found/doesn't work. Please check specified dotnet parameter.");
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
