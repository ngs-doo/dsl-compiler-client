package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.InputParameter;

import java.io.File;
import java.util.Map;

public interface CompileAction {
	public boolean check(final Map<InputParameter, String> parameters);
	
	public void compile(final File path, final Map<InputParameter, String> parameters);
}
