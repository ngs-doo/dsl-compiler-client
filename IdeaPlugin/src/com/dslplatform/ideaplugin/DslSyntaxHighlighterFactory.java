package com.dslplatform.ideaplugin;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DslSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
	private static class MapKey {
		private final Project project;
		private final String file;
		private final String id;
		MapKey(Project project, String file) {
			this.project = project;
			this.file = file;
			id = project == null ? file : project.getName() + "/" + file;
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MapKey)) return false;
			MapKey mk = (MapKey)obj;
			return mk.id.equals(this.id);
		}
	}
	private final ConcurrentMap<MapKey, DslSyntaxHighlighter> highlighters = new ConcurrentHashMap<MapKey, DslSyntaxHighlighter>();
	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
		MapKey key = new MapKey(project, virtualFile != null ? virtualFile.getPath() + "/" + virtualFile.getName() : "");
		DslSyntaxHighlighter highlighter = highlighters.get(key);
		if (highlighter == null || highlighter.virtualFile == null || !highlighter.virtualFile.isValid()
				|| virtualFile != null && !virtualFile.equals(highlighter.virtualFile)
				|| highlighter.isDisposed()) {
			if (highlighter != null) highlighter.stop();
			highlighter = new DslSyntaxHighlighter(project, virtualFile);
			highlighters.put(key, highlighter);
		}
		return highlighter;
	}
}