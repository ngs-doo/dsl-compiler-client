package com.dslplatform.ideaplugin;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DslRuleContextTest {

	@Test
	public void guessContextTopLevelIsEmpty() {
		assertEquals("", DslRuleContext.guessContext("module A {\n  x\n", 5));
	}

	@Test
	public void guessContextInsideBracesIsModuleRule() {
		String text = "module A {\n  x\n}\n";
		int afterOpen = text.indexOf('{') + 1;
		assertEquals("module_rule", DslRuleContext.guessContext(text, afterOpen));
		assertEquals("module_rule", DslRuleContext.guessContext(text, text.length() - 2));
		assertEquals("", DslRuleContext.guessContext(text, text.length()));
	}

	@Test
	public void guessContextNestedBracesStillModuleRule() {
		String text = "module A {\nentity B {\n";
		assertEquals("module_rule", DslRuleContext.guessContext(text, text.length()));
	}

	@Test
	public void guessContextIgnoresBracesInStringsAndComments() {
		String text = "s = '{' // }\n/* } */\nt = 'a}b'\n";
		assertEquals("", DslRuleContext.guessContext(text, text.length()));
	}

	@Test
	public void enclosingAtPicksInnermostRegion() {
		DslCompilerService.RuleRegion outer = new DslCompilerService.RuleRegion(5, 30, "module_rule");
		DslCompilerService.RuleRegion inner = new DslCompilerService.RuleRegion(12, 24, "entity_rule");
		assertEquals("entity_rule", DslRuleContext.enclosingAt(Arrays.asList(outer, inner), 15));
		assertEquals("module_rule", DslRuleContext.enclosingAt(Arrays.asList(outer, inner), 8));
		assertEquals("", DslRuleContext.enclosingAt(Arrays.asList(outer, inner), 40));
	}

	@Test
	public void bestEffortReturnsNullWithoutRegions() {
		assertNull(DslRuleContext.findEnclosingRuleBestEffort("module A {\n", 5, null, Collections.emptyList()));
		assertNull(DslRuleContext.findEnclosingRuleBestEffort("module A {\n", 5, "module A {\n", Collections.emptyList()));
	}

	@Test
	public void bestEffortMatchesFreshLookupForIdenticalText() {
		String text = "module A {\nx\n}";
		DslCompilerService.RuleRegion region = new DslCompilerService.RuleRegion(text.indexOf('{'), text.lastIndexOf('}'), "module_rule");
		assertEquals("module_rule", DslRuleContext.findEnclosingRuleBestEffort(text, 12, text, Collections.singletonList(region)));
	}

	@Test
	public void bestEffortCorrectsOffsetsForAppendAtEnd() {
		String regionsText = "module A {\nx\n";
		DslCompilerService.RuleRegion region = new DslCompilerService.RuleRegion(regionsText.indexOf('{'), regionsText.length(), "module_rule");
		String text = regionsText + "  y";
		assertEquals("module_rule", DslRuleContext.findEnclosingRuleBestEffort(text, text.length() - 1, regionsText, Collections.singletonList(region)));
	}

	@Test
	public void bestEffortKeepsOffsetsInsideCommonPrefix() {
		String regionsText = "module A {\nx\n";
		DslCompilerService.RuleRegion region = new DslCompilerService.RuleRegion(regionsText.indexOf('{'), regionsText.length(), "module_rule");
		String text = regionsText + "  y";
		assertEquals("module_rule", DslRuleContext.findEnclosingRuleBestEffort(text, regionsText.indexOf('{') + 1, regionsText, Collections.singletonList(region)));
	}
}
