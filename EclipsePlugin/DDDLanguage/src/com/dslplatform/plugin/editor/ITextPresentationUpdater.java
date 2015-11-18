package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.TextPresentation;

public interface ITextPresentationUpdater {

	public void updateTextPresentation(TextPresentation presentation);
	
	public void updateMarkers(TextPresentation presentation);
}
