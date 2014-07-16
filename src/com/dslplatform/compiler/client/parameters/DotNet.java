package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.InputParameter;
import com.dslplatform.compiler.client.Utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum DotNet implements CompileParameter {
	INSTANCE;

	@Override
	public boolean check(final Map<InputParameter, String> parameters) {
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
		return "To compiler .NET Mono/.NET compiler is required.\n" +
				"In Windows csc.exe is usually located in %WINDIR%\\Microsoft.NET\\Framework while on Linux mono is usually available via command line.\n" +
				"If custom path is required this option can be used to specify it.";
	}
}
