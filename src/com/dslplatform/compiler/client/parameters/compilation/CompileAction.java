package com.dslplatform.compiler.client.parameters.compilation;

import com.dslplatform.compiler.client.Context;

import java.io.File;

public interface CompileAction {
	public boolean check(final Context context);
	
	public void compile(final File path, final Context context);
}
