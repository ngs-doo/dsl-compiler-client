package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.diagnostic.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class DslCompilerIntegrationTest {

	private static final Logger logger = Logger.getInstance("DslCompilerIntegrationTest");
	private static DslCompiler.TokenParser parser;

	@BeforeClass
	public static void startCompiler() throws Exception {
		File defaultCompiler = new File(System.getProperty("user.home"), ".DSL-Platform/dsl-compiler.exe");
		boolean hasRuntime = commandOnPath("mono") || commandOnPath("dotnet");
		assumeTrue("DSL compiler not available: no dsl-compiler.exe in " + defaultCompiler.getAbsolutePath()
				+ " and neither mono nor dotnet on PATH - skipping integration tests",
				defaultCompiler.isFile() || hasRuntime);
		DslContext context = new DslContext(logger);
		context.put(Download.INSTANCE, null);
		boolean processed = Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE));
		String path = context.get(DslCompiler.INSTANCE);
		assumeTrue("DSL compiler setup failed (processContext=" + processed + ", path=" + path + ")", processed && path != null);
		Either<DslCompiler.TokenParser> tryParser = DslCompiler.setupServer(context, new File(path));
		assumeTrue("Failed to start DSL compiler server: " + (tryParser.isSuccess() ? "" : tryParser.explainError()), tryParser.isSuccess());
		parser = tryParser.get();
		long deadline = System.currentTimeMillis() + 180 * 1000L;
		Either<DslCompiler.ParseResult> warmup = null;
		while (System.currentTimeMillis() < deadline) {
			warmup = rawParse("module warmup {\n}\n");
			if (warmup != null && warmup.isSuccess()) return;
			Thread.sleep(2000);
		}
		assumeTrue("DSL compiler did not become ready in time: " + (warmup == null ? "no response" : warmup.explainError()), false);
	}

	@AfterClass
	public static void stopCompiler() {
		if (parser != null) {
			try {
				parser.close();
			} catch (Exception ignore) {
			}
		}
	}

	private static boolean commandOnPath(String name) {
		String path = System.getenv("PATH");
		if (path == null) return false;
		for (String dir : path.split(File.pathSeparator)) {
			if (new File(dir, name).canExecute()) return true;
		}
		return false;
	}

	private static Either<DslCompiler.ParseResult> rawParse(String dsl) {
		synchronized (parser) {
			try {
				return parser.parse(dsl);
			} catch (Exception e) {
				return Either.fail(e.getMessage());
			}
		}
	}

	private static DslCompiler.ParseResult parseOrThrow(String dsl) {
		Either<DslCompiler.ParseResult> result = rawParse(dsl);
		assertTrue("parse failed: " + (result.isSuccess() ? "" : result.explainError()), result.isSuccess());
		return result.get();
	}

	private static String readFixture(String relativePath) throws Exception {
		File file = new File(relativePath);
		assertTrue("fixture not found: " + file.getAbsolutePath(), file.isFile());
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	@Test
	public void parsesInlineModuleAndExtractsItsRegion() {
		String dsl = "module Inline {\n}\n";
		DslCompiler.ParseResult result = parseOrThrow(dsl);
		assertNull(result.error);
		assertNotNull(result.tokens);
		assertFalse(result.tokens.isEmpty());
		List<DslRuleRegions.Region> regions = DslRuleRegions.extract(result.tokens, DslCompilerService.lineOffsets(dsl), dsl.length());
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("module_rule", region.name);
		assertEquals(dsl.indexOf('{'), region.start);
		assertEquals(dsl.indexOf('}'), region.end);
		assertFalse(region.nested);
	}

	@Test
	public void parsesRealDslFileFromRepository() throws Exception {
		String dsl = readFixture("../MavenPlugin/src/test/resources/dsl/test.dsl");
		DslCompiler.ParseResult result = parseOrThrow(dsl);
		assertNull(result.error);
		assertNotNull(result.tokens);
		assertFalse(result.tokens.isEmpty());

		List<DslRuleRegions.Region> regions = DslRuleRegions.extract(result.tokens, DslCompilerService.lineOffsets(dsl), dsl.length());
		assertEquals("unexpected regions: " + regionNames(regions), 2, regions.size());

		for (DslRuleRegions.Region region : regions) {
			assertTrue(region.start >= 0 && region.end > region.start && region.end <= dsl.length());
			assertEquals("region start should point at the opening brace", '{', dsl.charAt(region.start));
			assertEquals("region end should point at the closing brace", '}', dsl.charAt(region.end));
		}

		DslRuleRegions.Region module = null;
		DslRuleRegions.Region aggregate = null;
		for (DslRuleRegions.Region region : regions) {
			if ("module_rule".equals(region.name)) module = region;
			if ("aggregate_root_rule".equals(region.name)) aggregate = region;
		}
		assertNotNull("no region for module_rule, got: " + regionNames(regions), module);
		assertEquals(dsl.indexOf('{'), module.start);
		assertEquals(dsl.lastIndexOf('}'), module.end);
		assertNotNull("no region for aggregate_root_rule, got: " + regionNames(regions), aggregate);
		assertEquals(dsl.indexOf('{', module.start + 1), aggregate.start);
		assertEquals(dsl.indexOf('}'), aggregate.end);
	}

	@Test
	public void findRuleReturnsGrammarRuleInfo() {
		Either<DslCompiler.RuleInfo> tryRule;
		synchronized (parser) {
			tryRule = parser.findRule("module_rule");
		}
		assertTrue("findRule failed: " + (tryRule.isSuccess() ? "" : tryRule.explainError()), tryRule.isSuccess());
		assertEquals("module_rule", tryRule.get().rule);

		Either<DslCompiler.RuleInfo> tryInstanceName;
		synchronized (parser) {
			tryInstanceName = parser.findRule("MojoTestModule");
		}
		assertFalse("instance names are not grammar rules", tryInstanceName.isSuccess());
	}

	private static String regionNames(List<DslRuleRegions.Region> regions) {
		StringBuilder sb = new StringBuilder();
		for (DslRuleRegions.Region region : regions) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(region.name).append(" [").append(region.start).append('-').append(region.end).append(']');
		}
		return sb.toString();
	}
}
