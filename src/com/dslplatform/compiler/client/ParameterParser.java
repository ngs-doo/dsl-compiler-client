package com.dslplatform.compiler.client;

public interface ParameterParser {
	public Either<Boolean> tryParse(final String name, final String value, final Context context);
}
