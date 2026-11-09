package com.dslplatform.ideaplugin;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the DslFile refresh state machine: identical text must not trigger a second refresh, stale results are dropped, skipped/failed refreshes can be retried.
 */
public class DslFileRefreshTest extends LightPlatformCodeInsightFixture4TestCase {

	private com.intellij.openapi.vfs.VirtualFile stubVirtualFile;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		stubVirtualFile = myFixture.addFileToProject("Stub.dsl", "module A {\n}\n").getVirtualFile();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private DslFile newFile() {
		try {
			Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
			DslFile file = (DslFile) unsafe.allocateInstance(DslFile.class);
			// no PSI behind this instance; unsafe allocation skips the PsiFileImpl constructor, so myManager must be set up too
			StubViewProvider provider = new StubViewProvider(
					com.intellij.psi.PsiManager.getInstance(getProject()), stubVirtualFile);
			installViewProvider(file, provider);
			setInheritedField(DslFile.class, file, "myManager", provider.getManager());
			return file;
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	// PsiFileImpl casts its provider to AbstractFileViewProvider, so a plain interface proxy is not enough
	private static final class StubViewProvider extends com.intellij.psi.AbstractFileViewProvider {
		StubViewProvider(com.intellij.psi.PsiManager manager, com.intellij.openapi.vfs.VirtualFile virtualFile) {
			super(manager, virtualFile, false);
		}

		@Override
		protected PsiFile getPsiInner(Language language) {
			return null;
		}

		@Override
		public PsiFile getCachedPsi(Language language) {
			return null;
		}

		@Override
		public java.util.List<PsiFile> getCachedPsiFiles() {
			return java.util.Collections.emptyList();
		}

		@Override
		public java.util.List<com.intellij.lang.FileASTNode> getKnownTreeRoots() {
			return java.util.Collections.emptyList();
		}

		@Override
		public Language getBaseLanguage() {
			return null;
		}

		@Override
		public java.util.Set<Language> getLanguages() {
			return java.util.Collections.emptySet();
		}

		@Override
		public java.util.List<PsiFile> getAllFiles() {
			return java.util.Collections.emptyList();
		}

		@Override
		public com.intellij.psi.PsiElement findElementAt(int offset) {
			return null;
		}

		@Override
		public com.intellij.psi.PsiReference findReferenceAt(int offset) {
			return null;
		}

		@Override
		public com.intellij.psi.PsiElement findElementAt(int offset, java.lang.Class<? extends Language> languageClass) {
			return null;
		}

		@Override
		public FileViewProvider createCopy(com.intellij.openapi.vfs.VirtualFile virtualFile) {
			return this;
		}
	}

	private static void setInheritedField(Class<?> start, Object target, String name, Object value) throws Exception {
		Class<?> type = start;
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignore) {
				type = type.getSuperclass();
			}
		}
		throw new IllegalStateException("field " + name + " not found on " + start.getName() + " hierarchy");
	}

	private static void installViewProvider(DslFile file, FileViewProvider provider) throws Exception {
		Class<?> type = DslFile.class;
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				if (FileViewProvider.class.isAssignableFrom(field.getType())) {
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
