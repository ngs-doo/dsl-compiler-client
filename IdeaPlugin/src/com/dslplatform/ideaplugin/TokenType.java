package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class TokenType extends IElementType {

	public static final TextAttributesKey IDENTIFIER_KEY = createTextAttributesKey("IDENTIFIER", DefaultLanguageHighlighterColors.CONSTANT);
	public static final TextAttributesKey KEYWORD_KEY = createTextAttributesKey("KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
	public static final TextAttributesKey STRING_KEY = createTextAttributesKey("STRING", DefaultLanguageHighlighterColors.STRING);

	public static final TokenType KEYWORD = new TokenType(DslCompiler.SyntaxType.Keyword.name(), new TextAttributesKey[]{KEYWORD_KEY});
	public static final TokenType IDENTIFIER = new TokenType(DslCompiler.SyntaxType.Identifier.name(), new TextAttributesKey[]{IDENTIFIER_KEY});
	public static final TokenType STRING_QUOTE = new TokenType(DslCompiler.SyntaxType.StringQuote.name(), new TextAttributesKey[]{STRING_KEY});
	public static final TokenType OTHER = new TokenType("other", new TextAttributesKey[0]);
	public static final TokenType IGNORED = new TokenType("ignored", new TextAttributesKey[0]);

	public static TokenType from(DslCompiler.SyntaxType type) {
		switch (type) {
			case Keyword:
				return KEYWORD;
			case Identifier:
				return IDENTIFIER;
			case StringQuote:
				return STRING_QUOTE;
			default:
				return OTHER;
		}
	}

	public final String name;
	public final TextAttributesKey[] attributes;

	private TokenType(String name, TextAttributesKey[] attributes) {
		super(name, DomainSpecificationLanguage.INSTANCE);
		this.name = name;
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return name;
	}
}