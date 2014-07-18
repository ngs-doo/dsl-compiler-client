package com.dslplatform.compiler.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class Context {
	private final Map<String, String> parameters = new HashMap<String, String>();
	private final Map<String, Object> cache = new HashMap<String, Object>();

	private boolean withLog;
	private boolean noPrompt;

	public void put(final InputParameter parameter, final String value) {
		if(parameter.equals(InputParameter.NO_PROMPT)) {
			noPrompt = true;
		}
		else if(parameter.equals(InputParameter.LOG)) {
			withLog = true;
		}
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

	private static synchronized void write(final boolean newLine, final String... values) {
		if (values.length == 0) {
			System.out.println();
		} else {
			if (newLine) {
				for (final String v : values) {
					System.out.println(v);
				}
			} else {
				for (final String v : values) {
					System.out.print(v);
				}
			}
		}
		System.out.flush();
	}

	public void show(final String... values) {
		write(true, values);
	}

	public void log(final String value) {
		if (withLog) {
			write(true, value);
		}
	}

	public void log(final char[] value, final int len) {
		if (withLog) {
			write(false, new String(value, 0, len));
		}
	}

	public void error(final String value) {
		write(true, value);
	}

	public void error(final Exception ex) {
		write(true, ex.getMessage());
		if(withLog) {
			final StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			write(true, sw.toString());
		}
	}

	public boolean canInteract() {
		return System.console() != null && !noPrompt;
	}

	public String ask(final String question) {
		write(false, question + " ");
		return System.console().readLine();
	}

	public char[] askSecret(final String question) {
		write(false, question + " ");
		return System.console().readPassword();
	}
}
