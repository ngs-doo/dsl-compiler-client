package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.parameters.DslCompiler;

import java.util.ArrayList;
import java.util.List;

public final class DslRuleRegions {

	public static final class Region {
		public final int start;
		public final int end;
		public final String name;
		public final boolean nested;

		public Region(int start, int end, String name, boolean nested) {
			this.start = start;
			this.end = end;
			this.name = name;
			this.nested = nested;
		}
	}

	private DslRuleRegions() {
	}

	public static List<Region> extract(List<DslCompiler.SyntaxConcept> tokens, int[] linesTotal, int textLength) {
		List<Region> regions = new ArrayList<Region>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> starts = new ArrayList<Integer>();
		ArrayList<Integer> levels = new ArrayList<Integer>();
		ArrayList<Boolean> nested = new ArrayList<Boolean>();
		for (DslCompiler.SyntaxConcept t : tokens) {
			int off = linesTotal[t.line - 1] + t.column;
			if (t.type == DslCompiler.SyntaxType.RuleExtension) {
				names.add(t.value);
				starts.add(off);
				levels.add(0);
				nested.add(false);
			} else if (t.type == DslCompiler.SyntaxType.RuleEnd && !levels.isEmpty()) {
				int idx = levels.size() - 1;
				int level = levels.get(idx) - 1;
				levels.set(idx, level);
				if (level >= 0) continue;
				regions.add(new Region(starts.get(idx), off, t.value, nested.get(idx)));
				names.remove(idx);
				starts.remove(idx);
				levels.remove(idx);
				nested.remove(idx);
				if (!levels.isEmpty()) {
					int parentIdx = levels.size() - 1;
					levels.set(parentIdx, levels.get(parentIdx) - 1);
				}
			} else if (t.type == DslCompiler.SyntaxType.RuleStart && !levels.isEmpty()) {
				int idx = levels.size() - 1;
				levels.set(idx, levels.get(idx) + 1);
				nested.set(idx, true);
			}
		}
		for (int i = names.size() - 1; i >= 0; i--) {
			regions.add(new Region(starts.get(i), textLength, names.get(i), nested.get(i)));
		}
		return regions;
	}
}
