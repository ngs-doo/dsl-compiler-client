package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DynamicActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DslConceptsMenuGroup extends ActionGroup implements DynamicActionGroup {

	public DslConceptsMenuGroup() {
		super("DSL", true);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setVisible(DslActionContext.isDsl(e));
	}

	@NotNull
	@Override
	public AnAction[] getChildren(AnActionEvent e) {
		if (e == null) return AnAction.EMPTY_ARRAY;
		PsiFile psi = DslActionContext.resolve(e);
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (!(psi instanceof DslFile) || editor == null) {
			return AnAction.EMPTY_ARRAY;
		}

		final DslFile dslFile = (DslFile) psi;
		final int offset = editor.getCaretModel().getOffset();
		final String text = editor.getDocument().getText();

		final DslCompilerService service = ApplicationManager.getApplication().getService(DslCompilerService.class);
		if (!dslFile.hasFreshRegions(text) && dslFile.requestRefresh(text)) {
			ApplicationManager.getApplication().executeOnPooledThread(new DumbAwareRunnable() {
				@Override
				public void run() {
					service.refreshRegions(dslFile, text);
				}
			});
		}

		String parentRule = dslFile.findEnclosingRule(text, offset);
		if (parentRule == null) parentRule = dslFile.findEnclosingRuleBestEffort(text, offset);
		if (parentRule == null) parentRule = dslFile.guessContext(text, offset);

		DslCompiler.RuleInfo parent = findRule(service, parentRule);
		if (parent == null && !parentRule.isEmpty()) {
			parent = findRule(service, "");
		}
		if (parent == null) {
			final String message = service.areRulesReady() ? "DSL Platform compiler not ready" : "DSL Platform rules are still loading...";
			return new AnAction[]{new AnAction(message) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent event) {
				}
			}};
		}

		List<DslCompiler.RuleInfo> ordered = new ArrayList<DslCompiler.RuleInfo>();
		for (String childName : parent.children) {
			DslCompiler.RuleInfo child = findRule(service, childName);
			if (child == null) continue;
			ordered.add(child);
		}
		Collections.sort(ordered, new Comparator<DslCompiler.RuleInfo>() {
			@Override
			public int compare(DslCompiler.RuleInfo a, DslCompiler.RuleInfo b) {
				return DslCompletionContributor.prettyRuleName(a.rule).compareToIgnoreCase(DslCompletionContributor.prettyRuleName(b.rule));
			}
		});
		AnAction[] children = new AnAction[ordered.size()];
		for (int i = 0; i < ordered.size(); i++) {
			DslCompiler.RuleInfo child = ordered.get(i);
			final String grammar = child.grammar == null ? "" : child.grammar;
			List<String> keywords = DslCompletionContributor.conceptKeywords(grammar);
			final String insert = keywords.isEmpty() ? "" : keywords.get(0);
			children[i] = new AnAction(DslCompletionContributor.prettyRuleName(child.rule)) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent event) {
					insertAtCaret(event, insert);
				}
			};
			if (!grammar.isEmpty()) {
				children[i].getTemplatePresentation().setDescription(grammar);
			}
		}
		return children;
	}

	private static DslCompiler.RuleInfo findRule(DslCompilerService service, String name) {
		Either<DslCompiler.RuleInfo> tryRule = service.findRule(name);
		return tryRule.isSuccess() ? tryRule.get() : null;
	}

	private static void insertAtCaret(@NotNull AnActionEvent event, @NotNull String textToInsert) {
		final Project project = event.getProject();
		final Editor editor = event.getData(CommonDataKeys.EDITOR);
		if (project == null || editor == null) return;
		final int offset = editor.getCaretModel().getOffset();
		CommandProcessor.getInstance().executeCommand(project, new Runnable() {
			@Override
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					@Override
					public void run() {
						PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
						editor.getDocument().insertString(offset, textToInsert);
						editor.getCaretModel().moveToOffset(offset + textToInsert.length());
					}
				});
			}
		}, "Insert DSL concept", null);
	}
}
