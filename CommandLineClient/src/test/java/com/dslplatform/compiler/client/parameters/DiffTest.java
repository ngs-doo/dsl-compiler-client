package com.dslplatform.compiler.client.parameters;

import com.dslplatform.compiler.client.Context;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiffTest {

	private static void compareDsls(final Context context, final Map<String, String> previous) throws Exception {
		final Method method = Diff.class.getDeclaredMethod("compareDsls", Context.class, Map.class);
		method.setAccessible(true);
		try {
			method.invoke(null, context, previous);
		} catch (final InvocationTargetException ex) {
			final Throwable cause = ex.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new Exception(cause);
		}
	}

	private static Context contextWithCurrent(final String... filesAndContent) {
		final Map<String, String> current = new LinkedHashMap<String, String>();
		for (int i = 0; i < filesAndContent.length; i += 2) {
			current.put(filesAndContent[i], filesAndContent[i + 1]);
		}
		final Context context = new Context();
		context.cache("current_dsl_map_cache", current);
		return context;
	}

	@Test
	public void lastEqualChunkShorterThanFiveLines() throws Exception {
		final Context context = contextWithCurrent("a.dsl", "1\n2\nX\nSAME\n4");
		final Map<String, String> previous = new LinkedHashMap<String, String>();
		previous.put("a.dsl", "1\n2\n3\nSAME\n4");
		compareDsls(context, previous); // used to throw ArrayIndexOutOfBoundsException
	}

	@Test
	public void longEqualChunkWithEllipsis() throws Exception {
		final StringBuilder current = new StringBuilder();
		final StringBuilder previous = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			current.append("line").append(i).append('\n');
			previous.append("line").append(i).append('\n');
		}
		current.append("TAIL1\nTAIL2\n");
		final Context context = contextWithCurrent("big.dsl", current.toString());
		final Map<String, String> previousMap = new LinkedHashMap<String, String>();
		previousMap.put("big.dsl", previous.toString());
		previousMap.put("removed.dsl", "x\ny\n");
		compareDsls(context, previousMap);
	}

	@Test
	public void identicalFilesProduceNoChanges() throws Exception {
		final Context context = contextWithCurrent("a.dsl", "same\ncontent\n");
		final Map<String, String> previous = new LinkedHashMap<String, String>();
		previous.put("a.dsl", "same\ncontent\n");
		compareDsls(context, previous);
	}
}
