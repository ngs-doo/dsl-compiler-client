package com.dslplatform.ideaplugin;

import java.util.List;

final class DslRuleContext {

	private DslRuleContext() {
	}

	static String guessContext(String text, int offset) {
		int depth = 0;
		boolean inString = false;
		boolean inLineComment = false;
		boolean inBlockComment = false;
		for (int i = 0; i < offset && i < text.length(); i++) {
			char c = text.charAt(i);
			if (inLineComment) {
				if (c == '\n') inLineComment = false;
			} else if (inBlockComment) {
				if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
					inBlockComment = false;
					i++;
				}
			} else if (inString) {
				if (c == '\'') inString = false;
			} else if (c == '\'') {
				inString = true;
			} else if (c == '/' && i + 1 < text.length()) {
				char n = text.charAt(i + 1);
				if (n == '/') inLineComment = true;
				else if (n == '*') {
					inBlockComment = true;
					i++;
				}
			} else if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
			}
		}
		return depth > 0 ? "module_rule" : "";
	}

	static String enclosingAt(List<DslCompilerService.RuleRegion> regions, int offset) {
		DslCompilerService.RuleRegion innermost = null;
		for (DslCompilerService.RuleRegion r : regions) {
			if (offset >= r.start && offset < r.end && (innermost == null || r.start > innermost.start)) {
				innermost = r;
			}
		}
		return innermost != null ? innermost.name : "";
	}

	static String findEnclosingRuleBestEffort(String text, int offset, String regionsText, List<DslCompilerService.RuleRegion> regions) {
		if (regionsText == null || regions.isEmpty()) {
			return null;
		}
		int l = 0;
		int max = Math.min(regionsText.length(), text.length());
		while (l < max && regionsText.charAt(l) == text.charAt(l)) l++;
		int delta = text.length() - regionsText.length();
		int oldOffset = offset > l ? offset - delta : offset;
		if (oldOffset < 0) oldOffset = 0;
		return enclosingAt(regions, oldOffset);
	}
}
