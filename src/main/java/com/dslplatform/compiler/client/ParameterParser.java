package com.dslplatform.compiler.client;

public interface ParameterParser {
	Either<Boolean> tryParse(final String name, final String value, final Context context);
}
