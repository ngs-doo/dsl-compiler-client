package com.dslplatform.compiler.client;

public class Either<T> {
	private final T value;
	private final String error;

	private Either(T value, String error) {
		this.value = value;
		this.error = error;
	}

	public boolean isSuccess() {
		return error == null;
	}

	public T get() {
		return value;
	}

	public String whyNot() {
		return error;
	}

	public static <S> Either<S> success(S value) {
		return new Either<S>(value, null);
	}

	public static <F> Either<F> fail(String error) {
		return new Either<F>(null, error);
	}
}
