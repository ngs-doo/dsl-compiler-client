package com.dslplatform.ideaplugin;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DslCompletionContributorTest {

	@Test
	public void conceptKeywordsFromPlainWord() {
		assertEquals(Collections.singletonList("module"), DslCompletionContributor.conceptKeywords("module"));
	}

	@Test
	public void conceptKeywordsFromAlternation() {
		assertEquals(Arrays.asList("Entity", "Module"), DslCompletionContributor.conceptKeywords("(Entity|Module)"));
	}

	@Test
	public void conceptKeywordsIgnoreQuotedPartsWhenSplitting() {
		assertEquals(Arrays.asList("entity", "module"), DslCompletionContributor.conceptKeywords("(entity 'name' | module)"));
	}

	@Test
	public void conceptKeywordsOnlySplitOnTopLevelAlternation() {
		assertEquals(Arrays.asList("a", "d"), DslCompletionContributor.conceptKeywords("(a(b|c)|d)"));
	}

	@Test
	public void conceptKeywordsSkipNonWordAlternatives() {
		assertEquals(Arrays.asList("a", "b"), DslCompletionContributor.conceptKeywords("(a|'x'|b)"));
	}

	@Test
	public void conceptKeywordsEmptyOrNullGrammar() {
		assertEquals(Collections.emptyList(), DslCompletionContributor.conceptKeywords(""));
		assertEquals(Collections.emptyList(), DslCompletionContributor.conceptKeywords(null));
	}

	@Test
	public void conceptKeywordsWithoutClosingParenUseRestOfGrammar() {
		assertEquals(Arrays.asList("a", "b"), DslCompletionContributor.conceptKeywords("(a|b"));
	}

	@Test
	public void prettyRuleNameStripsRuleSuffixAndUnderscores() {
		assertEquals("module", DslCompletionContributor.prettyRuleName("module_rule"));
		assertEquals("aggregate root", DslCompletionContributor.prettyRuleName("aggregate_root_rule"));
		assertEquals("custom", DslCompletionContributor.prettyRuleName("custom"));
	}

	@Test
	public void prettyRuleNameTopLevelForEmptyOrNull() {
		assertEquals("Top level rules", DslCompletionContributor.prettyRuleName(""));
		assertEquals("Top level rules", DslCompletionContributor.prettyRuleName(null));
	}

	@Test
	public void lineOffsetsTracksNewlines() {
		assertArrayEquals(new int[]{0}, DslCompilerService.lineOffsets("abc"));
		assertArrayEquals(new int[]{0, 2}, DslCompilerService.lineOffsets("a\nbb"));
		assertArrayEquals(new int[]{0}, DslCompilerService.lineOffsets(""));
	}
}
