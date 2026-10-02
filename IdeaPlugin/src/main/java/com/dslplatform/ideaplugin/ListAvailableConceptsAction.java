package com.dslplatform.ideaplugin;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
			codeCompletion.actionPerformed(e);
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabledAndVisible(DslSettings.getInstance().isShowConcepts()
			&& DslActionContext.isDsl(e));
	}
}
