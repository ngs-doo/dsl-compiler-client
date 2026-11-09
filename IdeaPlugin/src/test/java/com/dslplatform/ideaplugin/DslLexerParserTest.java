package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.dslplatform.compiler.client.parameters.FakeDslCompiler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Regression tests for the DSL lexer: the token stream must be contiguous, non-overlapping and within [0, text.length()] - violations used to crash the highlighter.
 */
public class DslLexerParserTest extends LightPlatformCodeInsightFixture4TestCase {

	/** Valid in the real compiler's default DSL. */
	private static final String MODULE = "module Foo {\n}\n";
	private static final String TWO_MODULES = MODULE + "module Bar {\n}\n";

	private static DslCompiler.TokenParser real;

	private FakeDslCompiler fake;
	private DslCompilerService service;
	private com.intellij.psi.PsiFile psiFile;
	private VirtualFile file;
	private DslLexerParser lexer;

	@BeforeClass
	public static void bootRealCompiler() {
		try {
			real = RealDslCompiler.create();
		} catch (Exception e) {
			System.err.println("DSL compiler unavailable - integration tests will be skipped: " + e.getMessage());
		}
	}

	private static void assumeRealCompiler() {
		Assume.assumeTrue("real DSL compiler not available (mono/dotnet + dsl-compiler.exe required)", real != null);
	}

	@AfterClass
	public static void stopRealCompiler() throws Exception {
		if (real != null) real.close();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fake = new FakeDslCompiler();
		service = (DslCompilerService) ApplicationManager.getApplication().getService(DslCompilerService.class);
		injectParser(service, fake);
		psiFile = myFixture.addFileToProject("Sample.dsl", MODULE);
		file = psiFile.getVirtualFile();
		lexer = new DslLexerParser(getProject(), file);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (lexer != null) lexer.stop();
		} finally {
			super.tearDown();
		}
	}

	private static void injectParser(DslCompilerService service, DslCompiler.TokenParser parser) throws Exception {
		Field field = DslCompilerService.class.getDeclaredField("tokenParser");
		field.setAccessible(true);
		field.set(service, parser);
	}

	private void parseModules() {
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				List<DslCompiler.SyntaxConcept> tokens = new ArrayList<DslCompiler.SyntaxConcept>();
				String[] lines = dsl.split("\n", -1);
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];
					if (line.startsWith("module ")) {
						tokens.add(FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, "module", i + 1, 0));
						int nameStart = line.indexOf(' ') + 1;
						int nameEnd = line.indexOf('{');
						String name = nameEnd > nameStart ? line.substring(nameStart, nameEnd).trim() : "";
						if (name.length() > 0) {
							tokens.add(FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, name, i + 1, nameStart));
						}
					}
				}
				return FakeDslCompiler.tokens(tokens.toArray(new DslCompiler.SyntaxConcept[0]));
			}
		};
	}

	private void makeReady() {
		parseModules();
		service.warmUpParser();
		assertTrue("service must become ready after warmup", service.isReady());
	}

	private Document document() {
		return PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
	}

	private void setDocumentText(final String text) {
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				document().setText(text);
				PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
			}
		});
	}

	private void waitForRealTokens(String text, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (hasRealTokens(text)) return;
			// results are applied via invokeLater - in a headless test the IDE queue must be dispatched manually
			com.intellij.testFramework.PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
			Thread.sleep(50);
		}
		fail("lexer never received real tokens for: " + text.replace("\n", "\\n"));
	}

	private boolean hasRealTokens(String text) {
		lexer.start(text, 0, text.length(), 0);
		while (lexer.getTokenType() != null) {
			if (!TokenType.IGNORED.equals(lexer.getTokenType())) return true;
			lexer.advance();
		}
		return false;
	}

	private void assertFullCoverage(String text) {
		lexer.start(text, 0, text.length(), 0);
		int expected = 0;
		int count = 0;
		while (lexer.getTokenType() != null) {
			int start = lexer.getTokenStart();
			int end = lexer.getTokenEnd();
			assertTrue("token start " + start + " out of range for document of " + text.length(), start >= 0 && start <= text.length());
			assertTrue("token end " + end + " out of range for document of " + text.length(), end >= 0 && end <= text.length());
			assertTrue("tokens must not overlap or leave gaps (expected " + expected + ", got " + start + ")", start == expected);
			assertTrue("tokens must not be empty", end > start);
			expected = end;
			lexer.advance();
			count++;
			if (count > text.length() + 16) {
				fail("lexer produced an unbounded number of tokens - stuck in a loop");
			}
		}
		assertEquals("tokens must cover the whole document", text.length(), expected);
	}

	@Test
	public void testFirstHighlightingCoversWholeDocument() throws Exception {
		assumeRealCompiler();
		injectParser(service, real);
		service.warmUpParser();
		assertTrue("service must become ready after warmup", service.isReady());
		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 15000);
		assertFullCoverage(MODULE);
	}

	@Test
	public void testDocumentChangeKeepsFullCoverage() throws Exception {
		assumeRealCompiler();
		injectParser(service, real);
		service.warmUpParser();
		assertTrue("service must become ready after warmup", service.isReady());
		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 15000);
		assertTrue("real tokens must include the module name", containsTokenValue("Foo"));

		setDocumentText(TWO_MODULES);
		assertFullCoverage(TWO_MODULES);

		waitForTokenValue("Bar", 15000);
		assertFullCoverage(TWO_MODULES);
	}

	@Test
	public void testOutOfRangeParserTokensAreDropped() throws Exception {
		makeReady();
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				if (!MODULE.equals(dsl)) return Either.fail("unexpected text: " + dsl);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, "module", 1, 0),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, MODULE + "EXTRA", 1, 0),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Ghost", 9, 0));
			}
		};

		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 10000);
		assertFullCoverage(MODULE);
	}

	@Test
	public void testRepeatedHighlightPassesDoNotReanalyze() throws Exception {
		makeReady();
		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 10000);
		int parsesAfterFirstAnalysis = fake.parseCount.get();
		assertTrue(parsesAfterFirstAnalysis >= 1);

		for (int i = 0; i < 5; i++) {
			assertFullCoverage(MODULE);
			com.intellij.testFramework.PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
			Thread.sleep(100);
		}
		assertEquals("repeated highlight passes must not re-parse the same text", parsesAfterFirstAnalysis, fake.parseCount.get());
	}

	@Test
	public void testFileOpenedBeforeCompilerIsReadyRecoversWithoutReopen() throws Exception {
		assumeRealCompiler();
		injectParser(service, real);
		Field ready = DslCompilerService.class.getDeclaredField("parserVerified");
		ready.setAccessible(true);
		ready.setBoolean(service, false);
		assertFalse("service starts out not ready", service.isReady());

		assertFullCoverage(MODULE);
		assertFalse("no real tokens before the compiler is ready", hasRealTokens(MODULE));

		service.warmUpParser();
		assertTrue(service.isReady());
		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 15000);
		assertTrue("real tokens must arrive without closing and re-opening the file", hasRealTokens(MODULE));
	}

	private void waitForTokenValue(String value, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (containsTokenValue(value)) return;
			com.intellij.testFramework.PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
			Thread.sleep(50);
		}
		fail("lexer never produced a real token for: " + value);
	}

	private boolean containsTokenValue(String value) {
		String text = document().getText();
		lexer.start(text, 0, text.length(), 0);
		while (lexer.getTokenType() != null) {
			if (!TokenType.IGNORED.equals(lexer.getTokenType())
					&& value.equals(lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString())) {
				return true;
			}
			lexer.advance();
		}
		return false;
	}
}
