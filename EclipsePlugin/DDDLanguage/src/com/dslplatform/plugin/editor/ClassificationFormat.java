package com.dslplatform.plugin.editor;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.dslplatform.grammar.SyntaxConcept;
import com.dslplatform.grammar.SyntaxType;

public class ClassificationFormat {

	public static Color getColor(SyntaxConcept concept) {
		// TODO remove hc styles, use preference store
		switch (concept.Type) {
		case Keyword:
			return new Color(getDisplay(), 155, 0, 128);
		case Identifier:
			return new Color(getDisplay(), 0, 0, 155);
		case StringQuote:
			return new Color(getDisplay(), 139, 0, 0);
		case Delimiter:
			return new Color(getDisplay(), 0, 0, 0);
		default:
			return null;
		}
	}

	public static boolean hasTextAttribute(SyntaxType type) {
		return type == SyntaxType.Keyword
				|| type == SyntaxType.Identifier
				|| type == SyntaxType.StringQuote
				|| type == SyntaxType.Delimiter;
	}
	
	public static TextAttribute getTextAttribute(SyntaxConcept concept) {
		Color color = getColor(concept);
		if (color == null)
			return null;
		return new TextAttribute(getColor(concept));
	}
	
	private static Display getDisplay() {
		Display display = Display.getCurrent();
		// may be null if outside the UI thread
		if (display == null)
			display = Display.getDefault();
		return display;
	}
}
