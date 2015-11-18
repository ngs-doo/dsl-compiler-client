package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

public class DSLPresentationReconciler extends PresentationReconciler implements ITextPresentationUpdater {
	
	private final ISourceViewer viewer;
	
	public DSLPresentationReconciler(ISourceViewer sourceViewer) {
		viewer = sourceViewer;
	}
	
	@Override
	public void updateTextPresentation(TextPresentation presentation) {
		viewer.changeTextPresentation(presentation, false);
	}

	@Override
	public void updateMarkers(TextPresentation presentation) {
		// TODO Auto-generated method stub
		
	}

}
