package com.dslplatform.compiler.client;

import java.util.HashMap;
import java.util.Map;

public class Context {
	private final Map<String, String> parameters = new HashMap<String, String>();
	private final Map<String, Object> cache = new HashMap<String, Object>();

	public void put(final InputParameter parameter, final String value) {
		parameters.put(parameter.alias, value);
	}

	public void put(final String parameter, final String value) {
		parameters.put(parameter, value);
	}

	public boolean contains(final InputParameter parameter) {
		return parameters.containsKey(parameter.alias);
	}

	public boolean contains(final String parameter) {
		return parameters.containsKey(parameter);
	}

	public String get(final InputParameter parameter) {
		return parameters.get(parameter.alias);
	}

	public String get(final String parameter) {
		return parameters.get(parameter);
	}

	public void cache(final String name, final Object value) {
		cache.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T load(final String name) {
		return (T) cache.get(name);
	}

	public void show(final String... values) {
		if (values.length == 0) {
			System.out.println();
		} else {
			for (final String v : values) {
				System.out.println(v);
			}
		}
	}

	public void error(final String value) {
		System.out.println(value);
	}

	public void error(final Exception ex) {
		System.out.println(ex.getMessage());
	}

	public boolean canInteract() {
		return System.console() != null && !parameters.containsKey(InputParameter.NO_PROMPT.alias);
	}

	public String ask(final String question) {
		System.out.print(question);
		System.out.print(" ");
		return System.console().readLine();
	}

	public char[] askSecret(final String question) {
		System.out.print(question);
		System.out.print(" ");
		return System.console().readPassword();
	}
}
