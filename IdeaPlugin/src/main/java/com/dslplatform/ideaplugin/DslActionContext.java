package com.dslplatform.ideaplugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

final class DslActionContext {

	private DslActionContext() {
	}

	@Nullable
	static PsiFile resolve(@Nullable AnActionEvent e) {
		if (e == null) return null;
		final Project project = e.getProject();
		final Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (project == null || editor == null) return null;
		final Document document = editor.getDocument();
		if (document == null) return null;
		return ReadAction.compute(() -> PsiDocumentManager.getInstance(project).getPsiFile(document));
	}

	static boolean isDsl(@Nullable AnActionEvent e) {
		return resolve(e) instanceof DslFile;
	}
}
