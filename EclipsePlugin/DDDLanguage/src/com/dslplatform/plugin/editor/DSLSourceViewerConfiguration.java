package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class DSLSourceViewerConfiguration extends SourceViewerConfiguration {

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		
		// TODO decouple?
		DSLPresentationReconciler reconciler = new DSLPresentationReconciler(sourceViewer);
		ITokenScanner scanner = new TokenScanner();
		String contentType = IDocument.DEFAULT_CONTENT_TYPE;
		
		// TODO preference store
		long delay = 300;
		
		DefaultDamagerRepairer dr = new DelayedDamagerRepairer(scanner, reconciler, delay);
		reconciler.setDamager(dr, contentType);
		reconciler.setRepairer(dr, contentType);

		return reconciler;
	}
}
