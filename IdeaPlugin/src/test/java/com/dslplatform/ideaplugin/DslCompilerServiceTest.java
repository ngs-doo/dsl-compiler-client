package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.dslplatform.compiler.client.parameters.FakeDslCompiler;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the compiler service startup paths.
 * <p>
 * These cover the bugs that used to be only visible in the IDE: a file opened before the compiler
 * was ready had no highlighting at all (until it was closed and re-opened), and analysis while
 * the server was still warming up could crash or loop forever. The service is driven with an
 * in-memory fake parser, so no mono/.NET process is required.
 */
public class DslCompilerServiceTest {

	private FakeDslCompiler fake;
	private DslCompilerService service;

	@Before
	public void setUp() throws Exception {
		fake = new FakeDslCompiler();
		service = newServiceWith(fake);
	}

	// The no-arg constructor would start the real compiler setup in a background thread, which is
	// neither needed nor deterministic in a unit test - allocate the service without running it.
	private static DslCompilerService newServiceWith(FakeDslCompiler parser) throws Exception {
		DslCompilerService created = allocateWithoutConstructor(DslCompilerService.class);
		setField(created, "logger", Logger.getInstance("DslCompilerServiceTest"));
		setField(created, "tokenParser", parser);
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
	public void serviceIsNotReadyBeforeWarmupAndFailsGracefully() {
		assertFalse("fresh service must not report readiness", service.isReady());

		Either<DslCompiler.RuleInfo> tryRule = service.findRule("module_rule");
		assertFalse(tryRule.isSuccess());
		assertEquals("DSL rules not loaded yet", tryRule.explainError());

		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				return Either.fail("Parser not ready");
			}
		};
		Either<DslCompilerService.Analysis> analysis = service.analyzeFull("module Foo {\n}\n");
		assertFalse("analysis while the server is warming up must fail, not crash", analysis.isSuccess());
		assertEquals("Parser not ready", analysis.explainError());
	}

	@Test
	public void warmUpParserMarksServiceReady() {
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String dsl) {
				return FakeDslCompiler.tokens();
			}
		};
		service.warmUpParser();
		assertTrue("warmup must flip the service to ready", service.isReady());
		assertTrue(fake.parseCount.get() >= 1);
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
	public void analyzeMapsTokensToDocumentOffsets() {
		makeReady();
		final String dsl = "module Foo {\n  String someString;\n}\n";
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String text) {
				if (!dsl.equals(text)) return Either.fail("unexpected text: " + text);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, "module", 1, 0),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Foo", 1, 7),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Keyword, "String", 2, 2),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "someString", 2, 9));
			}
		};

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(dsl);
		assertTrue(analysis.isSuccess());
		List<AST> ast = analysis.get().ast;
		assertEquals(4, ast.size());
		assertToken(ast.get(0), "module", dsl.indexOf("module"));
		assertToken(ast.get(1), "Foo", dsl.indexOf("Foo"));
		assertToken(ast.get(2), "String", dsl.indexOf("String"));
		assertToken(ast.get(3), "someString", dsl.indexOf("someString"));
	}

	private static void assertToken(AST node, String value, int expectedOffset) {
		assertNotNull(node.concept);
		assertEquals(value, node.concept.value);
		assertEquals("token '" + value + "' must map to its document offset", expectedOffset, node.offset);
		assertEquals(value.length(), node.length);
	}

	@Test
	public void analyzeExtractsRuleRegions() {
		makeReady();
		final String dsl = "module Foo {\n  rule Bar {\n  }\n}\n";
		fake.handler = new FakeDslCompiler.ParseHandler() {
			@Override
			public Either<DslCompiler.ParseResult> parse(String text) {
				if (!dsl.equals(text)) return Either.fail("unexpected text: " + text);
				return FakeDslCompiler.tokens(
						FakeDslCompiler.concept(DslCompiler.SyntaxType.RuleExtension, "Foo", 1, 0),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.RuleStart, "Bar", 2, 2),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.RuleEnd, "Bar", 3, 2),
						FakeDslCompiler.concept(DslCompiler.SyntaxType.RuleEnd, "Foo", 4, 0));
			}
		};

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(dsl);
		assertTrue(analysis.isSuccess());
		List<DslCompilerService.RuleRegion> regions = analysis.get().regions;
		assertEquals(1, regions.size());
		DslCompilerService.RuleRegion region = regions.get(0);
		assertEquals("Foo", region.name);
		assertEquals(0, region.start);
		assertEquals(dsl.lastIndexOf('}'), region.end);
		assertTrue(region.nested);
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
						// value runs past the end of the document - the lexer must never emit such a token
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
						// line 9 does not exist in a 2-line document - it must be skipped, not crash
						FakeDslCompiler.concept(DslCompiler.SyntaxType.Identifier, "Ghost", 9, 0));
			}
		};

		Either<DslCompilerService.Analysis> analysis = service.analyzeFull(dsl);
		assertTrue("tokens on lines beyond the document must be skipped: "
				+ (analysis.isSuccess() ? "" : analysis.explainError()), analysis.isSuccess());
	}

	@Test
	public void rulesLoadOnceAndFindRuleDelegatesToParser() {
		int findsBefore = fake.findRuleCount.get();
		Either<DslCompiler.RuleInfo> early = service.findRule("module_rule");
		assertFalse(early.isSuccess());
		assertEquals("DSL rules not loaded yet", early.explainError());
		assertEquals("findRule must not touch the parser before rules are loaded", findsBefore, fake.findRuleCount.get());

		invokePrivate(service, "loadRulesInBackground");
		assertTrue(service.areRulesReady());

		fake.ruleHandler = new FakeDslCompiler.RuleHandler() {
			@Override
			public Either<DslCompiler.RuleInfo> findRule(String name) {
				return "module_rule".equals(name)
						? Either.success(FakeDslCompiler.rule("module_rule", "Module", "A module rule"))
						: Either.fail("Unknown rule: " + name);
			}
		};
		Either<DslCompiler.RuleInfo> rule = service.findRule("module_rule");
		assertTrue(rule.isSuccess());
		assertEquals("module_rule", rule.get().rule);
		assertEquals("A module rule", rule.get().description);
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
