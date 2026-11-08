package com.dslplatform.ideaplugin;

import com.intellij.psi.FileViewProvider;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the DslFile refresh state machine.
 * <p>
 * The "forever refresh" bug lived in exactly this contract: every highlight pass requested a
 * region refresh, and without deduplication each refresh triggered another one. These tests pin
 * down that identical text never triggers a second refresh, stale results are dropped, and a
 * skipped/failed refresh can be retried.
 */
public class DslFileRefreshTest {

	private static DslFile newFile() {
		try {
			Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
			DslFile file = (DslFile) unsafe.allocateInstance(DslFile.class);
			// no PSI behind this instance - a null-returning view provider keeps
			// getProject()/getViewProvider() based code paths safe
			installViewProvider(file, (FileViewProvider) Proxy.newProxyInstance(
				DslFileRefreshTest.class.getClassLoader(), new Class<?>[]{FileViewProvider.class},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						return method.getReturnType() == boolean.class ? Boolean.FALSE : null;
					}
				}));
			return file;
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static void installViewProvider(DslFile file, FileViewProvider provider) throws Exception {
		Class<?> type = DslFile.class;
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (field.getType() == FileViewProvider.class) {
					field.setAccessible(true);
					field.set(file, provider);
					return;
				}
			}
			type = type.getSuperclass();
		}
		throw new IllegalStateException("no FileViewProvider field found on DslFile hierarchy");
	}

	private static List<DslCompilerService.RuleRegion> regions(int start, int end, String name) {
		List<DslCompilerService.RuleRegion> list = new ArrayList<DslCompilerService.RuleRegion>();
		list.add(new DslCompilerService.RuleRegion(start, end, name));
		return list;
	}

	@Test
	public void repeatedRefreshForSameTextIsDeduplicated() {
		DslFile file = newFile();
		assertTrue(file.requestRefresh("module A {\n}\n"));
		assertFalse("identical text must not trigger another refresh", file.requestRefresh("module A {\n}\n"));
	}

	@Test
	public void inFlightRefreshIsNotRequestedTwice() {
		DslFile file = newFile();
		String text = "module A {\n}\n";
		assertTrue(file.requestRefresh(text));
		assertFalse("a refresh for this text is already in flight", file.requestRefresh(text));

		file.storeRegions(text, regions(0, text.lastIndexOf('}'), "A"));
		assertTrue(file.hasFreshRegions(text));
		assertFalse("text with fresh regions must not be refreshed again", file.requestRefresh(text));
	}

	@Test
	public void newerRefreshSupersedesOlderOne() {
		DslFile file = newFile();
		assertTrue(file.requestRefresh("old text"));
		assertTrue("user typed again before the first refresh finished", file.requestRefresh("new text"));

		file.storeRegions("old text", regions(0, 8, "A"));
		assertFalse("stale result must not be stored as fresh", file.hasFreshRegions("new text"));
	}

	@Test
	public void skippedOrFailedRefreshAllowsRetry() {
		DslFile file = newFile();
		assertTrue(file.requestRefresh("text"));
		file.refreshSkipped("text");
		assertTrue("after a skip the same text may be refreshed again", file.requestRefresh("text"));

		file.refreshFailed("text");
		assertTrue("after a failure the same text may be refreshed again", file.requestRefresh("text"));
	}

	@Test
	public void enclosingRuleIsOnlyReportedForFreshText() {
		DslFile file = newFile();
		String text = "module Foo {\n}\n";
		file.storeRegions(text, regions(0, text.lastIndexOf('}'), "Foo"));

		assertEquals("Foo", file.findEnclosingRule(text, 5));
		assertNull("stale text must not be answered from old regions", file.findEnclosingRule("module Foobar {\n}\n", 5));
	}

	@Test
	public void bestEffortLookupToleratesTypingBeforeRefresh() {
		DslFile file = newFile();
		String oldText = "module Foo {\n}\n";
		file.storeRegions(oldText, regions(0, oldText.lastIndexOf('}'), "Foo"));

		// user typed "bar" after "Foo" before the refresh finished - the offset must be mapped back
		String newText = "module Foobar {\n}\n";
		assertEquals("Foo", file.findEnclosingRuleBestEffort(newText, 12));
	}

	@Test
	public void guessContextIgnoresBracesInCommentsAndStrings() {
		DslFile file = newFile();
		String text = "module A {\n  // }\n  /* } */\n  String s = '}' ;\n}\n";
		int beforeClosingBrace = text.lastIndexOf('}');

		assertEquals("module_rule", file.guessContext(text, beforeClosingBrace));
		assertEquals("", file.guessContext(text, text.length()));
		assertEquals("depth must not go below zero", "", file.guessContext("}\n", 1));
	}
}
