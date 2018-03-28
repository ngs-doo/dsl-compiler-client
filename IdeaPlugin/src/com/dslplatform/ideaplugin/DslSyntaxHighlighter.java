package com.dslplatform.ideaplugin;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class DslSyntaxHighlighter extends SyntaxHighlighterBase {
	private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

	private final DslLexerParser lexerParser;
	public final VirtualFile virtualFile;

	public DslSyntaxHighlighter(Project project, VirtualFile virtualFile) {
		this.lexerParser = new DslLexerParser(project, virtualFile);
		this.virtualFile = virtualFile;
	}

	void stop() {
		lexerParser.stop();
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		return lexerParser;
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		if (tokenType instanceof TokenType) {
			TokenType tt = (TokenType) tokenType;
			return tt.attributes;
		}
		return EMPTY_KEYS;
	}
}