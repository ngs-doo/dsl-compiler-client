package com.dslplatform.ideaplugin;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class ListAvailableConceptsAction extends AnAction {
   @Override
   public void actionPerformed(@NotNull AnActionEvent e) {
       Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
       CaretModel caretModel = editor.getCaretModel();
       String selectedText = caretModel.getCurrentCaret().getSelectedText();

       if (selectedText == null || selectedText.isEmpty()) {
       }
   }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        Language lang = file.getLanguage();
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        e.getPresentation().setEnabledAndVisible(
                /*lang instanceof PlainTextLanguage
                        && file.getFileType() instanceof DslFileType
                        && */!caretModel.getCurrentCaret().hasSelection());
    }
}