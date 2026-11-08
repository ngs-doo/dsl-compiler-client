package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.dslplatform.compiler.client.parameters.FakeDslCompiler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Regression tests for the DSL lexer that drives syntax highlighting.
 * <p>
 * These reproduce, without a human in front of the IDE, the bugs that used to be only visible in
 * the UI:
 * <ul>
 *   <li>"forever refresh": every highlight pass re-parsed and re-set the document text, so editing never settled;</li>
 *   <li>"file not loading at start": a file opened before the compiler was ready stayed unhighlighted until closed and re-opened;</li>
 *   <li>"lexer out of range": tokens beyond the end of the document made the highlighter crash.</li>
 * </ul>
 * The invariant asserted here is exactly what IntelliJ's highlighter requires: the token stream
 * must be contiguous, non-overlapping, never empty and always within [0, text.length()].
 */
public class DslLexerParserTest extends LightPlatformCodeInsightFixture4TestCase {

	private static final String MODULE = "module Foo {\n  String someString;\n}\n";

	private FakeDslCompiler fake;
	private DslCompilerService service;
	private VirtualFile file;
	private DslLexerParser lexer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fake = new FakeDslCompiler();
		service = (DslCompilerService) ApplicationManager.getApplication().getService(DslCompilerService.class);
		injectParser(service, fake);
		file = myFixture.addFileToProject("Sample.dsl", MODULE);
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

	private static void injectParser(DslCompilerService service, FakeDslCompiler parser) throws Exception {
		Field field = DslCompilerService.class.getDeclaredField("tokenParser");
		field.setAccessible(true);
		field.set(service, parser);
	}

	/** Generic handler: every "module &lt;Name&gt; {" line yields a keyword + identifier token. */
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
		return PsiDocumentManager.getInstance(getProject()).getDocument(myFixture.getFile());
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

	/** Wait until the background analysis has been applied to the lexer. */
	private void waitForRealTokens(String text, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (hasRealTokens(text)) return;
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

	/** The invariant IntelliJ's highlighter requires - violations used to crash the UI. */
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
		makeReady();
		assertFullCoverage(MODULE);   // holds even before the async analysis lands
		waitForRealTokens(MODULE, 10000);
		assertFullCoverage(MODULE);   // and still after it does
	}

	@Test
	public void testDocumentChangeKeepsFullCoverage() throws Exception {
		makeReady();
		waitForRealTokens(MODULE, 10000);

		String shorter = "module Foo {\n}\n";
		setDocumentText(shorter);
		assertFullCoverage(shorter);   // rebase path: coverage must hold immediately

		String longer = shorter + "module Bar {\n}\n";
		setDocumentText(longer);
		assertFullCoverage(longer);
		waitForRealTokens(longer, 10000);
		assertFullCoverage(longer);   // real tokens for both modules, still fully covered
	}

	@Test
	public void testOutOfRangeParserTokensAreDropped() throws Exception {
		makeReady();
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				if (!MODULE.equals(dsl)) return Either.fail("unexpected text: " + dsl);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "someString", 2, 9),
						// runs past the end of the document
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, MODULE + "EXTRA", 1, 0),
						// line 9 does not exist in a 3-line document
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Ghost", 9, 0));
			}
		};

		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 10000);
		assertFullCoverage(MODULE);   // garbage in - still a fully covered, in-range token stream out
	}

	@Test
	public void testRepeatedHighlightPassesDoNotReanalyze() throws Exception {
		makeReady();
		assertFullCoverage(MODULE);
		waitForRealTokens(MODULE, 10000);
		int parsesAfterFirstAnalysis = fake.parseCount.get();
		assertTrue(parsesAfterFirstAnalysis >= 1);

		// IntelliJ runs the highlighter over and over; none of these passes may re-parse the same text
		for (int i = 0; i < 5; i++) {
			assertFullCoverage(MODULE);
			Thread.sleep(100);
		}
		assertEquals("repeated highlight passes must not re-parse the same text", parsesAfterFirstAnalysis, fake.parseCount.get());
	}

	@Test
	public void testFileOpenedBeforeCompilerIsReadyRecoversWithoutReopen() throws Exception {
		parseModules();
		Field ready = DslCompilerService.class.getDeclaredField("parserVerified");
		ready.setAccessible(true);
		ready.setBoolean(service, false);
		assertFalse("service starts out not ready", service.isReady());

		assertFullCoverage(MODULE);   // placeholder coverage must be immediate - no crash, no blank file
		assertFalse("no real tokens before the compiler is ready", hasRealTokens(MODULE));

		// the compiler finishes starting up while the file stays open
		service.warmUpParser();
		assertTrue(service.isReady());
		assertFullCoverage(MODULE);   // another highlight pass now picks the analysis up
		waitForRealTokens(MODULE, 15000);
		assertTrue("real tokens must arrive without closing and re-opening the file", hasRealTokens(MODULE));
	}
}
