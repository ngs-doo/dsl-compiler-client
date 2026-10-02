package com.dslplatform.ideaplugin;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class DslFile extends PsiFileBase {

    private volatile String regionsText = null;
    private volatile List<DslCompilerService.RuleRegion> regions = Collections.emptyList();
    private volatile String refreshingText = null;

    public DslFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, DomainSpecificationLanguage.INSTANCE);
    }

    boolean requestRefresh(@NotNull String text) {
        if (text.equals(regionsText)) return false;
        synchronized (this) {
            if (text.equals(regionsText)) return false;
            if (text.equals(refreshingText)) return false;
            refreshingText = text;
            return true;
        }
    }

    void storeRegions(@NotNull String text, @NotNull List<DslCompilerService.RuleRegion> newRegions) {
        boolean changed;
        synchronized (this) {
            if (refreshingText != null && !text.equals(refreshingText)) {
                return;
            }
            changed = !newRegions.equals(regions);
            regions = newRegions;
            regionsText = text;
            if (text.equals(refreshingText)) refreshingText = null;
        }
        if (changed) scheduleFoldingRefresh();
    }

    private void scheduleFoldingRefresh() {
        final Project project = getProject();
        if (project == null || project.isDisposed()) return;
        final VirtualFile virtualFile = getViewProvider().getVirtualFile();
        final Document document = getViewProvider().getDocument();
        if (virtualFile == null || document == null) return;
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (project.isDisposed()) return;
                CodeFoldingManager foldingManager = CodeFoldingManager.getInstance(project);
                for (FileEditor fileEditor : FileEditorManager.getInstance(project).getAllEditors(virtualFile)) {
                    if (fileEditor instanceof TextEditor) {
                        Editor editor = ((TextEditor) fileEditor).getEditor();
                        if (!editor.isDisposed() && editor.getDocument() == document) {
                            foldingManager.scheduleAsyncFoldingUpdate(editor);
                        }
                    }
                }
            }
        });
    }

    void refreshSkipped(@NotNull String text) {
        synchronized (this) {
            if (text.equals(refreshingText)) refreshingText = null;
        }
    }

    void refreshFailed(@NotNull String text) {
        synchronized (this) {
            if (text.equals(refreshingText)) refreshingText = null;
        }
    }

    boolean hasFreshRegions(@NotNull String text) {
        return text.equals(regionsText);
    }

    List<DslCompilerService.RuleRegion> getRegions() {
        return regions;
    }

    String findEnclosingRule(@NotNull String text, int offset) {
        if (!text.equals(regionsText)) return null;
        return enclosingAt(offset);
    }

    String findEnclosingRuleBestEffort(@NotNull String text, int offset) {
        if (regionsText == null || regions.isEmpty()) {
            return null;
        }
        int l = 0;
        int max = Math.min(regionsText.length(), text.length());
        while (l < max && regionsText.charAt(l) == text.charAt(l)) l++;
        int delta = text.length() - regionsText.length();
        int oldOffset = offset > l ? offset - delta : offset;
        if (oldOffset < 0) oldOffset = 0;
        String name = enclosingAt(oldOffset);
        return name;
    }

    String guessContext(@NotNull String text, int offset) {
        int depth = 0;
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i < offset && i < text.length(); i++) {
            char c = text.charAt(i);
            if (inLineComment) {
                if (c == '\n') inLineComment = false;
            } else if (inBlockComment) {
                if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                    inBlockComment = false;
                    i++;
                }
            } else if (inString) {
                if (c == '\'') inString = false;
            } else if (c == '\'') {
                inString = true;
            } else if (c == '/' && i + 1 < text.length()) {
                char n = text.charAt(i + 1);
                if (n == '/') inLineComment = true;
                else if (n == '*') {
                    inBlockComment = true;
                    i++;
                }
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
            }
        }
        String name = depth > 0 ? "module_rule" : "";
        return name;
    }

    private String enclosingAt(int offset) {
        DslCompilerService.RuleRegion innermost = null;
        for (DslCompilerService.RuleRegion r : regions) {
            if (offset >= r.start && offset < r.end && (innermost == null || r.start > innermost.start)) {
                innermost = r;
            }
        }
        return innermost != null ? innermost.name : "";
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
