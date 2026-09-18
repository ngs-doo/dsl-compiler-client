package com.dslplatform.ideaplugin;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class DslParserDefinition implements ParserDefinition {
	private static final IElementType DSL_TEXT = new IElementType("DSL_TEXT", DomainSpecificationLanguage.INSTANCE);
	private static final IFileElementType DSL_FILE_ELEMENT_TYPE = new IFileElementType(DomainSpecificationLanguage.INSTANCE) {
		@Override
		public ASTNode parseContents(@NotNull ASTNode chameleon) {
			return ASTFactory.leaf(DSL_TEXT, chameleon.getChars());
		}
	};

	@NotNull
	@Override
	public Lexer createLexer(Project project) {
		return new EmptyLexer();
	}

	@NotNull
	@Override
	public PsiParser createParser(Project project) {
		throw new UnsupportedOperationException("Not supported");
	}

	@NotNull
	@Override
	public IFileElementType getFileNodeType() {
		return DSL_FILE_ELEMENT_TYPE;
	}

	@NotNull
	@Override
	public TokenSet getWhitespaceTokens() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public PsiElement createElement(ASTNode node) {
		throw new UnsupportedOperationException("Not supported");
	}

	@NotNull
	@Override
	public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
		return new DslFile(viewProvider);
	}
}
