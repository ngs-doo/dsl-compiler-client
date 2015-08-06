package com.dslplatform.compiler.client;

public final class Either<T> {
	private final T value;
	private final Exception error;

	private Either(final T value, final Exception error) {
		this.value = value;
		this.error = error;
	}

	public boolean isSuccess() {
		return error == null;
	}

	public T get() {
		return value;
	}

	public Exception whyNot() {
		return error;
	}

	public String explainError() { return error.getMessage(); }

	public static <S> Either<S> success(final S value) {
		return new Either<S>(value, null);
	}

	public static <F> Either<F> fail(final String error) {
		return new Either<F>(null, new Exception(error));
	}

	public static <F> Either<F> fail(final Exception error) {
		return new Either<F>(null, error);
	}

	public static <F> Either<F> fail(final String description, final Exception error) {
		return new Either<F>(null, new Exception(description, error));
	}
}
