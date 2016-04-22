package com.dslplatform.plugin.editor;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

public class DSLSourceViewerConfiguration extends SourceViewerConfiguration {
	
	private IEditorPart editor;
	
	public DSLSourceViewerConfiguration(IEditorPart editor) {
		this.editor = editor;
	}
	
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		
		DSLPresentationReconciler reconciler = new DSLPresentationReconciler(sourceViewer);
		ITokenScanner scanner = new TokenScanner();
		String contentType = IDocument.DEFAULT_CONTENT_TYPE;
		// TODO preference store
		long delay = 300;
		DefaultDamagerRepairer dr = new DelayedDamagerRepairer(scanner, reconciler, delay);
		reconciler.setDamager(dr, contentType);
		reconciler.setRepairer(dr, contentType);
		
		// IResource file = this.extractResource(this.editor);
		
		return reconciler;
	}
	
   IResource extractResource() {
      IEditorInput input = editor.getEditorInput();
      if (!(input instanceof IFileEditorInput))
         return null;
      return ((IFileEditorInput)input).getFile();
   }
}
