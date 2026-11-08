package com.dslplatform.ideaplugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class ListAvailableConceptsAction extends DumbAwareAction {

	private static final String CODE_COMPLETION_ACTION_ID = "CodeCompletion";

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		AnAction codeCompletion = ActionManager.getInstance().getAction(CODE_COMPLETION_ACTION_ID);
		if (codeCompletion != null) {
			Editor editor = e.getData(CommonDataKeys.EDITOR);
			ActionManager.getInstance().tryToExecute(
					codeCompletion,
					e.getInputEvent(),
					editor != null ? editor.getContentComponent() : null,
					e.getPlace(),
					true
			);
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(DslSettings.getInstance().isShowConcepts()
			&& DslActionContext.isDsl(e));
	}
}
