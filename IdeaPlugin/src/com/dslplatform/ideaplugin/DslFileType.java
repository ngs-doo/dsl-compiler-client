package com.dslplatform.ideaplugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DslFileType extends LanguageFileType {
	public static final DslFileType INSTANCE = new DslFileType();

	private DslFileType() {
		super(DomainSpecificationLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName() {
		return "DSL file";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "Domain Specification Language file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "dsl";
	}

	@Override
	public Icon getIcon() {
		return DslIcons.FILE;
	}
}