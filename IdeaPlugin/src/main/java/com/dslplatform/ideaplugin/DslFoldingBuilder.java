package com.dslplatform.ideaplugin;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DslFoldingBuilder extends FoldingBuilderEx implements DumbAware {

	@Override
	public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		if (!(root instanceof DslFile)) return FoldingDescriptor.EMPTY;
		final DslFile dslFile = (DslFile) root;
		final String text = document.getText();
		if (!dslFile.hasFreshRegions(text)) return FoldingDescriptor.EMPTY;

		final List<DslCompilerService.RuleRegion> regions = dslFile.getRegions();
		if (regions.isEmpty()) return FoldingDescriptor.EMPTY;

		final ASTNode rootNode = root.getNode();
		if (rootNode == null) return FoldingDescriptor.EMPTY;

		final int docLength = text.length();
		List<FoldingDescriptor> descriptors = new ArrayList<>(regions.size());
		for (DslCompilerService.RuleRegion region : regions) {
			int start = region.start;
			int end = Math.min(region.end + 1, docLength);
			if (start < 0 || start >= docLength || end <= start) continue;
			FoldingDescriptor descriptor = new FoldingDescriptor(rootNode, new TextRange(start, end), null,
					region.nested ? "{ ... }" : "{}");
			descriptor.setGutterMarkEnabledForSingleLine(true);
			descriptors.add(descriptor);
		}
		return descriptors.toArray(new FoldingDescriptor[0]);
	}

	@Override
	public String getPlaceholderText(@NotNull ASTNode node) {
		return "{ ... }";
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode node) {
		return false;
	}
}
