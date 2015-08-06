package com.dslplatform.compiler.client;

public interface CompileParameter {
	String getAlias();

	String getUsage();

	boolean check(final Context context) throws ExitException;

	void run(final Context context) throws ExitException;

	String getShortDescription();

	String getDetailedDescription();
}
