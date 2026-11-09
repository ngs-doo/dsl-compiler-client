package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.dslplatform.compiler.client.parameters.FakeDslCompiler;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the compiler service startup paths; happy paths run against the real DSL compiler (see {@link RealDslCompiler}).
 */
public class DslCompilerServiceTest {

	/** Valid in the real compiler's default DSL. */
	private static final String MODULE = "module Foo {\n}\n";

	private static DslCompiler.TokenParser real;

	private FakeDslCompiler fake;
	private DslCompilerService service;

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

	@Before
	public void setUp() throws Exception {
		fake = new FakeDslCompiler();
		service = newServiceWith(fake);
	}

	// Unsafe allocation skips field initializers, so rulesLock must be set explicitly.
	private static DslCompilerService newServiceWith(DslCompiler.TokenParser parser) throws Exception {
		DslCompilerService created = allocateWithoutConstructor(DslCompilerService.class);
		setField(created, "logger", Logger.getInstance("DslCompilerServiceTest"));
		setField(created, "tokenParser", parser);
		setField(created, "rulesLock", new Object());
		return created;
	}

	private static <T> T allocateWithoutConstructor(Class<T> type) {
		try {
			Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
			return type.cast(unsafe.allocateInstance(type));
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static void invokePrivate(Object target, String method) throws Exception {
		java.lang.reflect.Method m = target.getClass().getDeclaredMethod(method);
		m.setAccessible(true);
		m.invoke(target);
	}

	@Test
	public void analyzeFullWithoutParserFailsGracefully() throws Exception {
		DslCompilerService bare = newServiceWith(null);
		Either<DslCompilerService.Analysis> analysis = bare.analyzeFull(MODULE);
		assertFalse("analysis without a parser must fail, not crash", analysis.isSuccess());
		assertEquals("Token parser not ready", analysis.explainError());
	}

	@Test
	public void warmUpParserMarksServiceReady() throws Exception {
		assumeRealCompiler();
		service = newServiceWith(real);
		assertFalse("fresh service must not report readiness", service.isReady());
		service.warmUpParser();
		assertTrue("warmup must flip the service to ready", service.isReady());

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(MODULE);
		assertTrue(analysis.isSuccess());
		// RuleStart/RuleEnd/RuleExtension do not enter the AST
		assertEquals(2, analysis.get().ast.size());
	}

	@Test
	public void emptyDocumentAnalyzesWithoutParsing() {
		makeReady();
		int parsesBefore = fake.parseCount.get();

		Either<DslCompilerService.Analysis> empty = service.analyzeFull("");
		assertTrue(empty.isSuccess());
		assertTrue(empty.get().ast.isEmpty());
		assertTrue(empty.get().regions.isEmpty());

		Either<DslCompilerService.Analysis> blank = service.analyzeFull("   \n\t\n");
		assertTrue(blank.isSuccess());
		assertTrue(blank.get().ast.isEmpty());

		assertEquals("empty/blank files must not round-trip to the compiler", parsesBefore, fake.parseCount.get());
	}

	@Test
	public void analyzeMapsTokensToDocumentOffsets() throws Exception {
		assumeRealCompiler();
		service = newServiceWith(real);
		service.warmUpParser();

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(MODULE);
		assertTrue(analysis.isSuccess());
		List<AST> ast = analysis.get().ast;
		assertEquals(2, ast.size());
		assertToken(ast.get(0), "module", MODULE.indexOf("module"));
		assertToken(ast.get(1), "Foo", MODULE.indexOf("Foo"));
	}

	@Test
	public void analyzeSurvivesCompilerParseError() throws Exception {
		assumeRealCompiler();
		service = newServiceWith(real);
		service.warmUpParser();
		// invalid DSL - assignment does not exist in this form, so the compiler reports an error with partial tokens
		final String invalid = "module Foo {\n  Integer x = 1;\n}\n";
		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(invalid);
		assertTrue(analysis.isSuccess());
		List<AST> ast = analysis.get().ast;
		assertEquals(2, ast.size());
		assertToken(ast.get(0), "module", invalid.indexOf("module"));
		assertToken(ast.get(1), "Foo", invalid.indexOf("Foo"));
	}

	private static void assertToken(AST node, String value, int expectedOffset) {
		assertNotNull(node.concept);
		assertEquals(value, node.concept.value);
		assertEquals("token '" + value + "' must map to its document offset", expectedOffset, node.offset);
		assertEquals(value.length(), node.length);
	}

	@Test
	public void analyzeExtractsRuleRegions() throws Exception {
		assumeRealCompiler();
		service = newServiceWith(real);
		service.warmUpParser();

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(MODULE);
		assertTrue(analysis.isSuccess());
		List<DslCompilerService.RuleRegion> regions = analysis.get().regions;
		assertEquals(1, regions.size());
		DslCompilerService.RuleRegion region = regions.get(0);
		// the region is named after the concept, not the module name
		assertEquals("module_rule", region.name);
		assertEquals(MODULE.indexOf('{'), region.start);
		assertEquals(MODULE.lastIndexOf('}'), region.end);
		assertFalse(region.nested);
	}

	@Test
	public void analyzeToleratesTokensBeyondDocumentEnd() {
		makeReady();
		final String dsl = "module Foo {\n}\n";
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String text) {
				if (!dsl.equals(text)) return Either.fail("unexpected text: " + text);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Foo", 1, 7),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, dsl + "EXTRA", 1, 0));
			}
		};

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(dsl);
		assertTrue("out-of-range tokens must not crash the analysis", analysis.isSuccess());
		boolean foundInRangeToken = false;
		for (AST node : analysis.get().ast) {
			if ("Foo".equals(node.concept != null ? node.concept.value : "")) {
				foundInRangeToken = true;
				assertEquals(dsl.indexOf("Foo"), node.offset);
			}
		}
		assertTrue(foundInRangeToken);
	}

	@Test
	public void analyzeToleratesTokensOnMissingLines() {
		makeReady();
		final String dsl = "module Foo {\n}\n";
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String text) {
				if (!dsl.equals(text)) return Either.fail("unexpected text: " + text);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, "module", 1, 0),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Ghost", 9, 0));
			}
		};

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(dsl);
		assertTrue("tokens on lines beyond the document must be skipped: "
				+ (analysis.isSuccess() ? "" : analysis.explainError()), analysis.isSuccess());
	}

	@Test
	public void rulesLoadOnceAndFindRuleDelegatesToParser() throws Exception {
		assumeRealCompiler();
		service = newServiceWith(real);
		Either<DslCompiler.RuleInfo> early = service.findRule("module_rule");
		assertFalse(early.isSuccess());
		assertEquals("DSL rules not loaded yet", early.explainError());

		invokePrivate(service, "loadRulesInBackground");
		assertTrue(service.areRulesReady());

		Either<DslCompiler.RuleInfo> rule = service.findRule("module_rule");
		assertTrue("findRule must succeed once rules are loaded: " + (rule.isSuccess() ? "" : rule.explainError()), rule.isSuccess());
		assertEquals("module_rule", rule.get().rule);
		assertNotNull(rule.get().description);
		assertFalse(rule.get().description.isEmpty());

		Either<DslCompiler.RuleInfo> unknown = service.findRule("no_such_rule_xyz");
		assertFalse("unknown rules must fail gracefully, not throw", unknown.isSuccess());
	}

	@Test
	public void findRuleMustNotTouchParserBeforeRulesLoaded() {
		int findsBefore = fake.findRuleCount.get();
		Either<DslCompiler.RuleInfo> early = service.findRule("module_rule");
		assertFalse(early.isSuccess());
		assertEquals("DSL rules not loaded yet", early.explainError());
		assertEquals("findRule must not touch the parser before rules are loaded", findsBefore, fake.findRuleCount.get());
	}

	private void makeReady() {
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				return FakeDslCompiler.tokens();
			}
		};
		service.warmUpParser();
		assertTrue("service must become ready after warmup", service.isReady());
	}
}
