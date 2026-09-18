package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DslHoverListener implements EditorFactoryListener {
	private static final int THROTTLE_MS = 250;
	private static final int MAX_HINT_WIDTH = 480;

	private final Map<Editor, HoverHandler> handlers = new HashMap<Editor, HoverHandler>();
	private final DslCompilerService dslService = ApplicationManager.getApplication().getService(DslCompilerService.class);

	@Override
	public void editorCreated(@NotNull EditorFactoryEvent event) {
		final Editor editor = event.getEditor();
		if (editor.getProject() == null) return;
		PsiFile file = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
		if (!(file instanceof DslFile)) return;

		final HoverHandler handler = new HoverHandler(editor);
		handlers.put(editor, handler);
		editor.getContentComponent().addMouseMotionListener(handler);
		editor.getDocument().addDocumentListener(handler);
	}

	@Override
	public void editorReleased(@NotNull EditorFactoryEvent event) {
		final HoverHandler handler = handlers.remove(event.getEditor());
		if (handler != null) {
			handler.dispose();
		}
	}

	private class HoverHandler extends MouseAdapter implements DocumentListener {
		private final Editor editor;
		private long lastCheck;
		private String shownRule;
		private int shownOffset = -1;
		private int shownLength = -1;
		private volatile int generation;
		private boolean showing;
		private volatile long analyzedStamp = -1;
		private volatile List<AST> analyzedAst;

		HoverHandler(Editor editor) {
			this.editor = editor;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			check(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			invalidate();
		}

		@Override
		public void documentChanged(DocumentEvent e) {
			invalidate();
			analyzedStamp = -1;
			analyzedAst = null;
		}

		void dispose() {
			invalidate();
			editor.getContentComponent().removeMouseMotionListener(this);
			editor.getDocument().removeDocumentListener(this);
		}

		private void invalidate() {
			generation++;
			showing = false;
			shownRule = null;
			shownOffset = -1;
			shownLength = -1;
		}

		private void check(MouseEvent e) {
			if (!DslSettings.getInstance().isShowTooltips()) {
				hide();
				return;
			}
			long now = System.currentTimeMillis();
			if (now - lastCheck < THROTTLE_MS) return;
			lastCheck = now;
			final Point point = e.getPoint();
			final int offset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(point));
			final String text = editor.getDocument().getText();
			final long stamp = editor.getDocument().getModificationStamp();
			ApplicationManager.getApplication().executeOnPooledThread(new DumbAwareRunnable() {
				@Override
				public void run() {
					List<AST> asts = stamp == analyzedStamp ? analyzedAst : null;
					if (asts == null) {
						Either<List<AST>> tryAst = dslService.analyze(text);
						if (!tryAst.isSuccess()) return;
						asts = tryAst.get();
						analyzedAst = asts;
						analyzedStamp = stamp;
					}
					String rule = null;
					int tokenOffset = -1;
					int tokenLength = -1;
					for (AST a : asts) {
						if (offset >= a.offset && offset < a.offset + a.length) {
							if (a.parent != null && a.parent.concept != null) {
								rule = a.parent.concept.value;
								tokenOffset = a.parent.offset;
								tokenLength = a.parent.length;
							}
							break;
						}
					}
					final String finalRule = rule;
					final int finalTokenOffset = tokenOffset;
					final int finalTokenLength = tokenLength;
					DslCompiler.RuleInfo info = null;
					if (finalRule != null) {
						Either<DslCompiler.RuleInfo> tryInfo = dslService.findRule(finalRule);
						if (tryInfo.isSuccess()) info = tryInfo.get();
					}
					final DslCompiler.RuleInfo finalInfo = info;
					final int gen = generation;
					ApplicationManager.getApplication().invokeLater(new Runnable() {
						@Override
						public void run() {
							if (gen != generation) return;
							if (finalInfo == null) {
								hide();
							} else {
								show(point, finalRule, finalInfo, finalTokenOffset, finalTokenLength);
							}
						}
					}, ModalityState.defaultModalityState());
				}
			});
		}

		private void show(Point point, String rule, DslCompiler.RuleInfo info, int tokenOffset, int tokenLength) {
			if (rule.equals(shownRule) && tokenOffset == shownOffset && tokenLength == shownLength) return;
			if (showing) {
				HintManager.getInstance().hideAllHints();
			}
			shownRule = rule;
			shownOffset = tokenOffset;
			shownLength = tokenLength;
			showing = true;
			String description = info.description == null ? "" : info.description;
			String grammar = info.grammar == null ? "" : info.grammar;
			StringBuilder html = new StringBuilder("<html><body style=\"font-family: sans-serif;\">");
			if (!description.isEmpty()) {
				html.append(escape(description)).append("<br>");
			}
			html.append("<p style=\"font-family: sans-serif;\"><font size=\"2\">Grammar: ").append(escape(grammar)).append("</font></p></body></html>");
			JEditorPane pane = new JEditorPane("text/html", html.toString());
			pane.setEditable(false);
			pane.setFocusable(false);
			pane.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			Dimension pref = pane.getPreferredSize();
			if (pref.width > MAX_HINT_WIDTH) {
				pane.setSize(MAX_HINT_WIDTH, pref.height);
				pref = new Dimension(MAX_HINT_WIDTH, pane.getPreferredSize().height);
			}
			JScrollPane scroll = new JScrollPane(pane);
			scroll.getVerticalScrollBar().setUnitIncrement(16);
			scroll.setPreferredSize(pref);
			scroll.getViewport().setViewPosition(new Point(0, 0));
			HintManager.getInstance().showHint(scroll, new RelativePoint(editor.getContentComponent(), new Point(point.x, point.y + 16)), 0, 0);
		}

		private void hide() {
			if (showing) {
				showing = false;
				shownRule = null;
				HintManager.getInstance().hideAllHints();
			}
		}
	}
	private static String escape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
