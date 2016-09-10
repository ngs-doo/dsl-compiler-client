package com.dslplatform.ideaplugin;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DslSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
	private final ConcurrentMap<VirtualFile, SyntaxHighlighter> highlighters = new ConcurrentHashMap<VirtualFile, SyntaxHighlighter>();
	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
		SyntaxHighlighter highlighter = highlighters.get(virtualFile);
		if (highlighter == null) {
			highlighter = new DslSyntaxHighlighter(project, virtualFile);
			highlighters.put(virtualFile, highlighter);
		}
		return highlighter;
	}
}