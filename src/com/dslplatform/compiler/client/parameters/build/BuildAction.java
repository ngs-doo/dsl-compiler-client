package com.dslplatform.compiler.client.parameters.build;

import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.ExitException;

import java.io.File;

public interface BuildAction {
	public boolean check(final Context context) throws ExitException;

	public void build(final File sources, final Context context) throws ExitException;
}
