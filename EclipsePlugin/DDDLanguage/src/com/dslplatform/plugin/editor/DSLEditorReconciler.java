package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

public class DSLEditorReconciler extends MonoReconciler {

    public DSLEditorReconciler(IReconcilingStrategy strategy,
            boolean isIncremental) {
        super(strategy, isIncremental);
    }
}
