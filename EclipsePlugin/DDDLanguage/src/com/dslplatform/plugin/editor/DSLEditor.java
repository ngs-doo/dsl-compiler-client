package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextEditor;

public class DSLEditor extends TextEditor {

	public DSLEditor() {
		super();
		
		// final IResource resource = (IResource) this.getEditorInput().getAdapter(IResource.class);

		
		setSourceViewerConfiguration(new DSLSourceViewerConfiguration());
	}

	public IDocument getDocument() {
		return getSourceViewer().getDocument();
	}
}
