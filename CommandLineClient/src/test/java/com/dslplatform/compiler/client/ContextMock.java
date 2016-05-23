package com.dslplatform.compiler.client;

public class ContextMock extends Context {
	public String message;
	public String warning;
	public String error;
	public boolean hasLog;
	public boolean hasWarning;
	public boolean hasError;

	@Override
	public void show(final String... values) {
		if (values.length > 0) {
			message = values[0];
		}
	}

	@Override
	public void log(final String value) {
		hasLog = true;
	}

	@Override
	public void log(final char[] value, final int len) {
		hasLog = true;
	}

	@Override
	public void warning(final String value) {
		hasWarning = true;
		warning = value;
	}

	@Override
	public void warning(final Exception ex) {
		hasWarning = true;
		warning = ex.getMessage();
	}

	@Override
	public void error(final String value) {
		hasError = true;
		error = value;
	}

	@Override
	public void error(final Exception ex) {
		hasError = true;
		error = ex.getMessage();
	}

	@Override
	public boolean canInteract() {
		return false;
	}

	@Override
	public <T> T notify(final String action, final T target) {
		return target;
	}
}