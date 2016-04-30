package com.dslplatform.ideaplugin;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class DslColorSettingsPage implements ColorSettingsPage {
	private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
			new AttributesDescriptor("Keyword", TokenType.KEYWORD_KEY),
			new AttributesDescriptor("Identifier", TokenType.IDENTIFIER_KEY),
			new AttributesDescriptor("StringQuote", TokenType.STRING_KEY),
	};

	@Nullable
	@Override
	public Icon getIcon() {
		return Icons.FILE;
	}

	@NotNull
	@Override
	public SyntaxHighlighter getHighlighter() {
		return new DslSyntaxHighlighter(null, null);
	}

	@NotNull
	@Override
	public String getDemoText() {
		return "// This is an example of the \".dsl\" file.\n" +
				"module schemaName {\n" +
				"   aggregate objectName {\n" +
				"	   int propertyName;\n" +
				"	   specification FilterName 'it => it.propertyName % 2 == 0';\n" +
				"   }\n" +
				"}";
	}

	@Nullable
	@Override
	public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return null;
	}

	@NotNull
	@Override
	public AttributesDescriptor[] getAttributeDescriptors() {
		return DESCRIPTORS;
	}

	@NotNull
	@Override
	public ColorDescriptor[] getColorDescriptors() {
		return ColorDescriptor.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public String getDisplayName() {
		return "DSL Platform";
	}
}