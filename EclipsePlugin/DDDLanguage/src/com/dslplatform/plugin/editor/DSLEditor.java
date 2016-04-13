package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.TextEditor;

public class DSLEditor extends TextEditor {

	public DSLEditor() {
		super();
		setSourceViewerConfiguration(new DSLSourceViewerConfiguration(this));
	}

	public IDocument getDocument() {
		return getSourceViewer().getDocument();
	}
}
