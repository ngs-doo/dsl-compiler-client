package com.dslplatform.ideaplugin;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DslFile extends PsiFileBase {
    public DslFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DomainSpecificationLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DslFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Domain Specification Language File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}