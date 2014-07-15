package com.dslplatform.compiler.client;

import java.util.Map;

public interface CompileParameter {
	public boolean check(final Map<InputParameter, String> parameters);

	public void run(final Map<InputParameter, String> parameters);

	public String getShortDescription();

	public String getDetailedDescription();
}
