package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.intellij.lexer.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentRunnable;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DslLexerParser extends Lexer {

	private final Project project;
	private final PsiFile psiFile;
	private final Document document;
	private final Application application;
	private final Runnable refreshAll;
	private final Runnable scheduleRefresh;
	private final DslCompilerService dslService;

	private boolean forceRefresh;
	private boolean waitingForSync;
	private long delayUntil;
	private String lastDsl = "";
	private final List<AST> ast = new ArrayList<AST>();
	private int position = 0;

	public DslLexerParser(Project project, VirtualFile file) {
		this.project = project;
		this.application = ApplicationManager.getApplication();
		this.dslService = ServiceManager.getService(DslCompilerService.class);
		if (project != null && file != null) {
			psiFile = PsiManager.getInstance(project).findFile(file);
			document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
			dslService.callWhenReady(new Runnable() {
				@Override
				public void run() {
					if (application != null) {
						application.invokeLater(scheduleRefresh);
					}
				}
			});
			refreshAll = new DocumentRunnable(document, null) {
				@Override
				public void run() {
					com.intellij.openapi.command.CommandProcessor.getInstance().runUndoTransparentAction(
							new Runnable() {
								@Override
								public void run() {
									forceRefresh = true;
									document.setText(document.getText());
								}
							});
				}
			};
			scheduleRefresh = new DocumentRunnable(document, null) {
				@Override
				public void run() {
					application.runWriteAction(refreshAll);
				}
			};
		} else {
			psiFile = null;
			document = null;
			refreshAll = new Runnable() {
				@Override
				public void run() {
				}
			};
			scheduleRefresh = new Runnable() {
				@Override
				public void run() {
				}
			};
		}
	}

	private AST getCurrent() {
		return position >= 0 && position < ast.size() ? ast.get(position) : null;
	}

	private void fixupAndReposition(String dsl, List<AST> newAst, int start) {
		lastDsl = dsl;
		int cur = 0;
		int index = 0;
		while (index < newAst.size()) {
			AST it = newAst.get(index);
			if (it.offset > cur) {
				newAst.add(index, new AST(null, cur, it.offset - cur, null));
				index++;
			}
			cur = it.offset + it.length;
			index++;
		}
		if (dsl.length() > 0) {
			AST last = newAst.get(newAst.size() - 1);
			int width = last.offset + last.length;
			if (width < dsl.length()) {
				newAst.add(new AST(null, width, dsl.length() - width, null));
			}
		}
		changeAst(start, newAst);
	}

	private void changeAst(int start, List<AST> newAst) {
		synchronized (ast) {
			position = 0;
			ast.clear();
			ast.addAll(newAst);
			for (int i = 0; i < ast.size(); i++) {
				if (ast.get(i).offset > start) {
					position = i - 1;
					return;
				}
			}
		}
	}

	private final Computable<String> obtainLatestDsl = new Computable<String>() {
		@Override
		public String compute() {
			return psiFile.getText();
		}
	};

	private final Runnable waitForDslSync = new DumbAwareRunnable() {
		@Override
		public void run() {
			try {
				do {
					Thread.sleep(100);
				} while (System.currentTimeMillis() < delayUntil);
				waitingForSync = false;
				application.invokeLater(scheduleRefresh);
			} catch (Exception ignore) {
			}
		}
	};

	private List<AST> lastParsedAnalysis;
	private String lastParsedDsl;

	@Override
	public void start(@NotNull CharSequence charSequence, int start, int end, int state) {
		if (project != null && project.isDisposed()) return;
		final boolean nonEditorPage = project == null || psiFile == null;
		final String dsl = charSequence.toString();
		if (forceRefresh || nonEditorPage || ast.size() == 0) {
			if (lastParsedAnalysis != null && dsl.equals(lastParsedDsl)) {
				changeAst(start, lastParsedAnalysis);
				lastDsl = lastParsedDsl;
				forceRefresh = false;
			} else {
				Either<List<AST>> tryNewAst = dslService.analyze(dsl);
				if (tryNewAst.isSuccess()) {
					List<AST> newAst = tryNewAst.get();
					if (newAst.size() == 0) {
						newAst.add(new AST(null, 0, dsl.length(), null));
					}
					fixupAndReposition(dsl, newAst, start);
					forceRefresh = false;
					lastParsedAnalysis = newAst;
					lastParsedDsl = dsl;
				} else {
					List<AST> newAst = new ArrayList<AST>(1);
					newAst.add(new AST(null, 0, dsl.length(), null));
					fixupAndReposition(dsl, newAst, start);
				}
			}
		} else if (!dsl.equals(lastDsl)) {
			final String actualDsl;
			if (start == end && dsl.length() == 0) {
				actualDsl = application.runReadAction(obtainLatestDsl);
				if (actualDsl.equals(lastDsl)) {
					position = 0;
					return;
				}
			} else actualDsl = dsl;
			List<AST> newAst = new ArrayList<AST>(ast.size());
			int cur = 0;
			int pos = start;
			while(pos < dsl.length() && pos < lastDsl.length() && dsl.charAt(pos) == lastDsl.charAt(pos)) {
				pos++;
			}
			while (cur < ast.size()) {
				AST a = ast.get(cur);
				if (a.offset + a.length < pos) {
					newAst.add(a);
				}
				else break;
				cur++;
			}
			if (pos < actualDsl.length()) {
				newAst.add(new AST(null, pos, actualDsl.length() - pos, null));
			}
			fixupAndReposition(actualDsl, newAst, start);
			delayUntil = System.currentTimeMillis() + 500;
			if (!waitingForSync && project.isOpen()) {
				waitingForSync = true;
				application.executeOnPooledThread(waitForDslSync);
			}
		} else if (start == 0 && end == dsl.length()) {
			position = 0;
		}
	}

	static class OffsetPosition implements LexerPosition {

		private final int offset;
		private final int state;

		OffsetPosition(int offset, int state) {
			this.offset = offset;
			this.state = state;
		}

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int getState() {
			return state;
		}
	}

	@NotNull
	public LexerPosition getCurrentPosition() {
		int offset = this.getTokenStart();
		int intState = this.getState();
		return new OffsetPosition(offset, intState);
	}

	public void restore(@NotNull LexerPosition position) {
		this.start(this.getBufferSequence(), position.getOffset(), this.getBufferEnd(), position.getState());
	}

	@Override
	public int getState() {
		return position;
	}

	@Nullable
	@Override
	public IElementType getTokenType() {
		AST current = getCurrent();
		return current != null ? current.type : null;
	}

	@Override
	public int getTokenStart() {
		AST current = getCurrent();
		if (current == null) {
			return lastDsl.length();
		}
		return current.offset;
	}

	@Override
	public int getTokenEnd() {
		AST current = getCurrent();
		return current != null ? current.offset + current.length : lastDsl.length();
	}

	@Override
	public void advance() {
		position++;
	}

	@NotNull
	@Override
	public CharSequence getBufferSequence() {
		return lastDsl;
	}

	@Override
	public int getBufferEnd() {
		return lastDsl.length();
	}
}
