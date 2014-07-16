package com.dslplatform.compiler.client;

public interface CompileParameter {
	public boolean check(final Context context);

	public void run(final Context context);

	public String getShortDescription();

	public String getDetailedDescription();
}
