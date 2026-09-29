package com.dslplatform.ideaplugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
		PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
		if (file == null) {
			Project project = e.getProject();
			Editor editor = e.getData(CommonDataKeys.EDITOR);
			if (project != null && editor != null) {
				file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
			}
		}
		return file;
	}

	static boolean isDsl(@Nullable AnActionEvent e) {
		return resolve(e) instanceof DslFile;
	}
}
