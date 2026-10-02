package com.dslplatform.ideaplugin;

import com.intellij.lang.Language;

public class DomainSpecificationLanguage extends Language {
	public static final DomainSpecificationLanguage INSTANCE = new DomainSpecificationLanguage();

	private DomainSpecificationLanguage() {
		super("DomainSpecificationLanguage");
	}
}