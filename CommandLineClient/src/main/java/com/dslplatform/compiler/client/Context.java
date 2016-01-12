package com.dslplatform.compiler.client;

import com.dslplatform.compiler.client.parameters.DisableColors;
import com.dslplatform.compiler.client.parameters.LogOutput;
import com.dslplatform.compiler.client.parameters.Prompt;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Context implements Closeable {
	private final Map<String, String> parameters = new HashMap<String, String>();
	private final Map<String, Object> cache = new HashMap<String, Object>();

	private PrintStream console = AnsiConsole.out();

	private boolean withLog;
	private boolean noPrompt;
	private boolean withColor = true;

	public void put(final CompileParameter parameter, final String value) {
		if (parameter instanceof Prompt) {
			noPrompt = true;
		} else if (parameter instanceof LogOutput) {
			withLog = true;
		} else if (parameter instanceof DisableColors) {
			withColor = false;
			console = System.out;
		}
		parameters.put(parameter.getAlias(), value);
	}

	public void put(final String parameter, final String value) {
		parameters.put(parameter.toLowerCase(), value);
	}

	public boolean contains(final CompileParameter parameter) {
		return parameters.containsKey(parameter.getAlias());
	}

	public boolean contains(final String parameter) {
		return parameters.containsKey(parameter);
	}

	public String get(final CompileParameter parameter) {
		return parameters.get(parameter.getAlias());
	}

	public String get(final String parameter) {
		return parameters.get(parameter.toLowerCase());
	}

	public void cache(final String name, final Object value) {
		cache.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T load(final String name) {
		return (T) cache.get(name);
	}

	private static synchronized void write(final PrintStream console, final boolean newLine, final String... values) {
		if (values.length == 0) {
			console.println();
		} else {
			if (newLine) {
				for (final String v : values) {
					console.println(v);
				}
			} else {
				for (final String v : values) {
					console.print(v);
				}
			}
		}
		console.flush();
	}

	public void show(final String... values) {
		write(console, true, values);
	}

	public static String inColor(final Ansi.Color color, final String message) {
		return Ansi.ansi().fg(color).a(message).reset().toString();
	}

	public void log(final String value) {
		if (withLog) {
			write(console, true, withColor ? inColor(Color.YELLOW, value) : value);
		}
	}

	public void log(final char[] value, final int len) {
		if (withLog) {
			final String msg = new String(value, 0, len);
			write(console, false, withColor ? inColor(Color.YELLOW, msg) : msg);
		}
	}

	public void error(final String value) {
		write(console, true, withColor ? inColor(Color.RED, value) : value);
	}

	public void error(final Exception ex) {
		error(ex.getMessage());
		if (withLog) {
			final StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			error(sw.toString());
		}
	}

	public boolean canInteract() {
		return System.console() != null && !noPrompt;
	}

	public String ask(final String question) {
		if (withColor) {
			write(console, false, Ansi.ansi().fgBright(Ansi.Color.WHITE).bold().a(question + " ").boldOff().reset().toString());
		} else {
			write(console, false, question + " ");
		}
		return System.console().readLine();
	}

	public char[] askSecret(final String question) {
		if (withColor) {
			write(console, false, Ansi.ansi().fgBright(Ansi.Color.CYAN).bold().a(question + " ").boldOff().reset().toString());
		} else {
			write(console, false, question + " ");
		}
		return System.console().readPassword();
	}

	@Override
	public void close() {
		for (Object it : cache.values()) {
			if (it instanceof Closeable) {
				try {
					((Closeable) it).close();
				} catch (IOException e) {
					error(e);
				}
			}
		}
	}
}
