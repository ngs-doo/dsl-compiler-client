package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DslCompletionContributor extends CompletionContributor implements DumbAware {

	private static final Logger LOG = Logger.getInstance("DSL Platform");

	public DslCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new DslRuleCompletionProvider());
	}

	private static class DslRuleCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
			if (!DslSettings.getInstance().isShowConcepts()) return;

			final PsiFile originalFile = parameters.getOriginalFile();
			if (!(originalFile instanceof DslFile)) return;
			final DslFile dslFile = (DslFile) originalFile;
			final String text = originalFile.getText();
			int offset = parameters.getOffset();
			if (offset < 0) offset = 0;
			if (offset > text.length()) offset = text.length();

			int p = offset;
			while (p > 0 && Character.isJavaIdentifierPart(text.charAt(p - 1))) p--;
			final String prefix = text.substring(p, offset);

			int i = offset;
			while (i > 0 && Character.isWhitespace(text.charAt(i - 1))) i--;
			if (i != 0) {
				char c = text.charAt(i - 1);
				boolean statementStart = c == '{' || c == '}' || c == ';';
				boolean typingKeyword = Character.isJavaIdentifierPart(c) && !prefix.isEmpty();
				if (!statementStart && !typingKeyword) return;
			}

			final DslCompilerService service = ApplicationManager.getApplication().getService(DslCompilerService.class);
			if (!dslFile.hasFreshRegions(text) && dslFile.requestRefresh(text)) {
				ApplicationManager.getApplication().executeOnPooledThread(new DumbAwareRunnable() {
					@Override
					public void run() {
						service.refreshRegions(dslFile, text);
					}
				});
			}

			String parentRule = dslFile.findEnclosingRule(text, offset);
			if (parentRule == null) parentRule = dslFile.findEnclosingRuleBestEffort(text, offset);
			if (parentRule == null) parentRule = dslFile.guessContext(text, offset);

			DslCompiler.RuleInfo parent = findRule(service, parentRule);
			if (parent == null && !parentRule.isEmpty()) {
				parent = findRule(service, "");
			}
			if (parent == null) {
				LOG.debug("completion: no rules available at offset " + offset + " (compiler not ready?)");
				return;
			}

			int added = 0;
			for (String childName : parent.children) {
				DslCompiler.RuleInfo child = findRule(service, childName);
				if (child == null) continue;
				if (!matches(child, prefix)) continue;
				final String grammar = child.grammar == null ? "" : child.grammar;
				final String name = prettyRuleName(child.rule);
				final String insert = prepareStaticGrammar(grammar);
				LookupElementBuilder element = LookupElementBuilder.create(insert.isEmpty() ? name : insert)
						.withPresentableText(name)
						.withIcon(DslIcons.FILE)
						.withTailText(grammar.isEmpty() ? null : " " + grammar, true);
				if (insert.isEmpty()) {
					element = element.withInsertHandler(new InsertNothing(prefix));
				}
				result.addElement(element);
				added++;
			}
			if (added > 0) {
				result.stopHere();
			}
		}

		private static DslCompiler.RuleInfo findRule(DslCompilerService service, String name) {
			Either<DslCompiler.RuleInfo> tryRule = service.findRule(name);
			return tryRule.isSuccess() ? tryRule.get() : null;
		}

		private static boolean matches(DslCompiler.RuleInfo child, String prefix) {
			if (prefix.isEmpty()) return true;
			final String grammar = child.grammar == null ? "" : child.grammar;
			final String insert = prepareStaticGrammar(grammar);
			if (!insert.isEmpty() && insert.startsWith(prefix)) return true;
			for (String keyword : conceptKeywords(grammar)) {
				if (keyword.startsWith(prefix)) return true;
			}
			return prettyRuleName(child.rule).toLowerCase().contains(prefix.toLowerCase());
		}
	}

	private static class InsertNothing implements InsertHandler<LookupElement> {
		private final String prefix;

		InsertNothing(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void handleInsert(InsertionContext context, LookupElement item) {
			final Document document = context.getEditor().getDocument();
			final int start = context.getStartOffset();
			final int end = Math.max(start, context.getEditor().getCaretModel().getOffset());
			document.replaceString(start, end, prefix);
		}
	}

	static String prepareStaticGrammar(String grammar) {
		if (grammar == null || grammar.isEmpty()) return "";
		if (!Character.isLetter(grammar.charAt(0))) return "";
		int i = 0;
		while (i < grammar.length()
				&& (Character.isWhitespace(grammar.charAt(i)) || Character.isLetterOrDigit(grammar.charAt(i)))) {
			i++;
		}
		return (i == grammar.length() ? grammar : grammar.substring(0, i)).trim();
	}

	static List<String> conceptKeywords(String grammar) {
		final List<String> keywords = new ArrayList<String>();
		if (grammar == null) return keywords;
		final String g = grammar.trim();
		if (g.isEmpty()) return keywords;
		char c = g.charAt(0);
		if (Character.isLetterOrDigit(c)) {
			keywords.add(firstWord(g));
			return keywords;
		}
		if (c != '(') return keywords;
		int depth = 0;
		boolean inQuote = false;
		int end = -1;
		for (int i = 0; i < g.length(); i++) {
			char ch = g.charAt(i);
			if (ch == '\'') inQuote = !inQuote;
			else if (!inQuote && ch == '(') depth++;
			else if (!inQuote && ch == ')') {
				depth--;
				if (depth == 0) { end = i; break; }
			}
		}
		final String inner = end > 0 ? g.substring(1, end) : g.substring(1);
		depth = 0;
		inQuote = false;
		int altStart = 0;
		for (int i = 0; i <= inner.length(); i++) {
			char ch = i < inner.length() ? inner.charAt(i) : '|';
			if (ch == '\'') inQuote = !inQuote;
			else if (!inQuote && ch == '(') depth++;
			else if (!inQuote && ch == ')') depth--;
			else if (!inQuote && ch == '|' && depth == 0) {
				addKeyword(keywords, inner.substring(altStart, i));
				altStart = i + 1;
			}
		}
		return keywords;
	}

	private static void addKeyword(List<String> keywords, String alternative) {
		String alt = alternative.trim();
		if (alt.isEmpty() || !Character.isLetterOrDigit(alt.charAt(0))) return;
		String word = firstWord(alt);
		if (!keywords.contains(word)) keywords.add(word);
	}

	private static String firstWord(String s) {
		int i = 0;
		while (i < s.length() && Character.isLetterOrDigit(s.charAt(i))) i++;
		return s.substring(0, i).trim();
	}

	static String prettyRuleName(String rule) {
		if (rule == null || rule.isEmpty()) return "Top level rules";
		String name = rule.endsWith("_rule") ? rule.substring(0, rule.length() - 5) : rule;
		return name.replace('_', ' ');
	}
}
