package com.dslplatform.ideaplugin;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.actions.BaseCodeCompletionAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ListAvailableConceptsAction extends BaseCodeCompletionAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		invokeCompletion(e, CompletionType.BASIC, 1);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabledAndVisible(DslSettings.getInstance().isShowConcepts()
			&& DslActionContext.isDsl(e));
	}
}
