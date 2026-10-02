package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.parameters.DslCompiler;
import org.junit.Test;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DslRuleRegionsTest {

	private static final Constructor<DslCompiler.SyntaxConcept> TOKEN_CTOR;

	static {
		try {
			TOKEN_CTOR = DslCompiler.SyntaxConcept.class.getDeclaredConstructor(Map.class);
			TOKEN_CTOR.setAccessible(true);
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private static DslCompiler.SyntaxConcept token(DslCompiler.SyntaxType type, String value, int line, int column) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Type", type.name());
		map.put("Value", value);
		map.put("Script", "");
		map.put("Line", line);
		map.put("Column", column);
		try {
			return TOKEN_CTOR.newInstance(map);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static List<DslRuleRegions.Region> extract(String text, DslCompiler.SyntaxConcept... tokens) {
		List<DslCompiler.SyntaxConcept> list = new ArrayList<DslCompiler.SyntaxConcept>(Arrays.asList(tokens));
		return DslRuleRegions.extract(list, DslCompilerService.lineOffsets(text), text.length());
	}

	@Test
	public void singleRuleClosesAtItsBrace() {
		String text = "module Foo {\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "Foo", 1, 0),
				token(DslCompiler.SyntaxType.RuleEnd, "Foo", 2, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("Foo", region.name);
		assertEquals(text.indexOf("module"), region.start);
		assertEquals(text.lastIndexOf('}'), region.end);
		assertFalse(region.nested);
	}

	@Test
	public void unterminatedRuleExtendsToDocumentEnd() {
		String text = "module Foo {\n  String someString;\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "Foo", 1, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("Foo", region.name);
		assertEquals(0, region.start);
		assertEquals(text.length(), region.end);
		assertFalse(region.nested);
	}

	@Test
	public void siblingRulesProduceSeparateRegions() {
		String text = "module A {\n}\nmodule B {\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "A", 1, 0),
				token(DslCompiler.SyntaxType.RuleEnd, "A", 2, 0),
				token(DslCompiler.SyntaxType.RuleExtension, "B", 3, 0),
				token(DslCompiler.SyntaxType.RuleEnd, "B", 4, 0));
		assertEquals(2, regions.size());
		assertEquals("A", regions.get(0).name);
		assertEquals(text.indexOf('}'), regions.get(0).end);
		assertEquals("B", regions.get(1).name);
		assertEquals(text.indexOf("module B"), regions.get(1).start);
		assertEquals(text.lastIndexOf('}'), regions.get(1).end);
	}

	@Test
	public void nestedRuleMarksParentAsNested() {
		String text = "module Outer {\n  rule Inner {\n  }\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "Outer", 1, 0),
				token(DslCompiler.SyntaxType.RuleStart, "Inner", 2, 2),
				token(DslCompiler.SyntaxType.RuleEnd, "Inner", 3, 2),
				token(DslCompiler.SyntaxType.RuleEnd, "Outer", 4, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("Outer", region.name);
		assertEquals(0, region.start);
		assertEquals(text.lastIndexOf('}'), region.end);
		assertTrue(region.nested);
	}

	@Test
	public void multipleNestedLevelsCloseOnlyAtOutermostBrace() {
		String text = "module A {\n  rule B {\n    rule C {\n    }\n  }\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "A", 1, 0),
				token(DslCompiler.SyntaxType.RuleStart, "B", 2, 2),
				token(DslCompiler.SyntaxType.RuleStart, "C", 3, 4),
				token(DslCompiler.SyntaxType.RuleEnd, "C", 4, 4),
				token(DslCompiler.SyntaxType.RuleEnd, "B", 5, 2),
				token(DslCompiler.SyntaxType.RuleEnd, "A", 6, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("A", region.name);
		assertEquals(text.lastIndexOf('}'), region.end);
		assertTrue(region.nested);
	}

	@Test
	public void ruleEndWithoutStartIsIgnored() {
		String text = "}\nmodule A {\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleEnd, "Ghost", 1, 0),
				token(DslCompiler.SyntaxType.RuleExtension, "A", 2, 0),
				token(DslCompiler.SyntaxType.RuleEnd, "A", 3, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("A", region.name);
		assertEquals(text.indexOf("module"), region.start);
		assertEquals(text.lastIndexOf('}'), region.end);
	}

	@Test
	public void extensionInsideExtensionClosesInnerFirst() {
		String text = "module Outer {\n  module Inner {\n  }\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.RuleExtension, "Outer", 1, 0),
				token(DslCompiler.SyntaxType.RuleExtension, "Inner", 2, 2),
				token(DslCompiler.SyntaxType.RuleEnd, "Inner", 3, 2),
				token(DslCompiler.SyntaxType.RuleEnd, "Outer", 4, 0));
		assertEquals(2, regions.size());
		assertEquals("Inner", regions.get(0).name);
		assertEquals(text.indexOf("module Inner"), regions.get(0).start);
		assertEquals(text.indexOf('}'), regions.get(0).end);
		assertFalse(regions.get(0).nested);
		assertEquals("Outer", regions.get(1).name);
		assertEquals(text.lastIndexOf('}'), regions.get(1).end);
		assertFalse(regions.get(1).nested);
	}

	@Test
	public void nonStructuralTokensAreIgnored() {
		String text = "module Foo {\n  String someString;\n}\n";
		List<DslRuleRegions.Region> regions = extract(text,
				token(DslCompiler.SyntaxType.Keyword, "module", 1, 0),
				token(DslCompiler.SyntaxType.Identifier, "Foo", 1, 7),
				token(DslCompiler.SyntaxType.RuleExtension, "Foo", 1, 0),
				token(DslCompiler.SyntaxType.Keyword, "String", 2, 2),
				token(DslCompiler.SyntaxType.Identifier, "someString", 2, 9),
				token(DslCompiler.SyntaxType.RuleEnd, "Foo", 3, 0));
		assertEquals(1, regions.size());
		DslRuleRegions.Region region = regions.get(0);
		assertEquals("Foo", region.name);
		assertEquals(text.lastIndexOf('}'), region.end);
		assertFalse(region.nested);
	}

	@Test
	public void emptyTokensProduceNoRegions() {
		assertTrue(extract("module Foo {\n}\n").isEmpty());
	}
}
