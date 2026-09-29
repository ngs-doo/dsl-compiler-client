package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.Either;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.lexer.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.DocumentRunnable;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
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
	private final VirtualFile file;
	private PsiFile psiFile;
	private Document document;
	private final Application application;
	private final Runnable refreshAll;
	private final Runnable scheduleRefresh;
	private final DslCompilerService dslService;

	private boolean forceRefresh;
	private boolean waitingForSync;
	private long delayUntil;
	private boolean waitingForCompiler;
	private String analyzingText = null;
	private boolean failedNotReady = false;
	private String lastDsl = "";
	private String startedText = null;
	private final List<AST> ast = new ArrayList<>();
	private int position = 0;
	private boolean isActive = true;
	private final Logger logger = Logger.getInstance("DSL Platform");

	private static final int RETRY_DELAY_MS = 500;

	public DslLexerParser(Project project, VirtualFile file) {
		this.project = project;
		this.file = file;
		this.application = ApplicationManager.getApplication();
		this.dslService = application.getService(DslCompilerService.class);
		if (project != null && file != null) {
			resolvePsi();
			refreshAll = new DocumentRunnable(document, project) {
				@Override
				public void run() {
					if (!isActive) return;
					if (hasActiveLookup()) {
						scheduleRefreshWhileLookupOpen();
						return;
					}
					CommandProcessor.getInstance().runUndoTransparentAction(
							new Runnable() {
								@Override
								public void run() {
									forceRefresh = true;
									resolvePsi();
									if (isActive && document != null && document.isWritable()) {
										String newText = document.getText();
										if (newText.isEmpty() || position >= ast.size()) {
											position = 0;
										}
										try {
											document.setText(newText);
										} catch (Exception ex) {
											logger.warn(ex.getMessage());
										}
									}
								}
							});
				}
			};
			scheduleRefresh = new DocumentRunnable(document, project) {
				@Override
				public void run() {
					if (!isActive) return;
					if (ModalityState.current() != ModalityState.NON_MODAL) {
						scheduleRefreshLater();
					} else {
						application.runWriteAction(refreshAll);
					}
				}
			};
		} else {
			psiFile = null;
			document = null;
			refreshAll = () -> {};
			scheduleRefresh = () -> {};
		}
	}

	private void scheduleRefreshLater() {
		if (!isActive) return;
		application.invokeLater(scheduleRefresh, ModalityState.NON_MODAL);
	}

	private boolean hasActiveLookup() {
		if (project == null || project.isDisposed()) return false;
		try {
			return LookupManager.getInstance(project).getActiveLookup() != null;
		} catch (Exception ex) {
			return false;
		}
	}

	private void scheduleRefreshWhileLookupOpen() {
		if (!isActive || waitingForSync) return;
		waitingForSync = true;
		application.executeOnPooledThread(new DumbAwareRunnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(RETRY_DELAY_MS);
				} catch (InterruptedException ignore) {
				}
				waitingForSync = false;
				if (isActive) {
					scheduleRefreshLater();
				}
			}
		});
	}

	private void analyzeInBackground(final String text) {
		if (!isActive || project == null || project.isDisposed() || text == null || dslService == null) return;
		if (!dslService.isReady()) {
			failedNotReady = true;
			if (!waitingForCompiler && project.isOpen()) {
				waitingForCompiler = true;
				application.executeOnPooledThread(waitForCompiler);
			}
			return;
		}
		synchronized (this) {
			if (text.equals(analyzingText)) return;
			analyzingText = text;
		}
		application.executeOnPooledThread(new DumbAwareRunnable() {
			@Override
			public void run() {
				final Either<List<AST>> tryNewAst = dslService.analyze(text);
				synchronized (DslLexerParser.this) {
					if (!text.equals(analyzingText)) return;   // superseded by a newer analyze
					analyzingText = null;
				}
				if (!tryNewAst.isSuccess()) {
					logger.debug("background analyze failed: " + tryNewAst.explainError());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignore) {
					}
					String current = document != null ? document.getText() : lastDsl;
					if (isActive && text.equals(current)) {
						analyzeInBackground(text);
					}
					return;
				}
				final List<AST> newAst = tryNewAst.get();
				logger.debug("analyzed successfully = " + newAst.size());
				application.invokeLater(new DumbAwareRunnable() {
					@Override
					public void run() {
						if (!isActive) return;
						String current = document != null ? document.getText() : text;
						if (current == null || !current.equals(text)) return;
						List<AST> asts = new ArrayList<AST>(newAst);
						if (asts.isEmpty()) {
							asts.add(new AST(null, 0, text.length(), null));
						}
						lastParsedAnalysis = padToFullCoverage(text, asts);
						lastParsedDsl = text;
						failedNotReady = false;
						fixupAndReposition(text, lastParsedAnalysis, 0);
						scheduleRefreshLater();
					}
				}, ModalityState.NON_MODAL);
			}
		});
	}

	private void resolvePsi() {
		if (psiFile != null || project == null || file == null || project.isDisposed()) return;
		try {
			PsiFile f = PsiManager.getInstance(project).findFile(file);
			if (f != null) {
				psiFile = f;
				document = PsiDocumentManager.getInstance(project).getDocument(f);
			}
		} catch (Exception ex) {
			logger.debug("Failed to resolve PSI for " + file.getPath() + ": " + ex.getMessage());
		}
	}

	void stop() {
		isActive = false;
	}

	boolean isDisposed() {
		return project != null && project.isDisposed();
	}

	private AST getCurrent() {
		return position >= 0 && position < ast.size() ? ast.get(position) : null;
	}

	private void fixupAndReposition(String dsl, List<AST> newAst, int start) {
		lastDsl = dsl;
		changeAst(start, padToFullCoverage(dsl, newAst));
	}

	private static List<AST> padToFullCoverage(String dsl, List<AST> in) {
		final int len = dsl.length();
		List<AST> clean = new ArrayList<>(in.size());
		int prevEnd = 0;
		for (AST a : in) {
			if (a == null || a.length <= 0 || a.offset < prevEnd || a.offset >= len || a.offset + a.length > len) continue;
			clean.add(a);
			prevEnd = a.offset + a.length;
		}
		int cur = 0;
		int index = 0;
		while (index < clean.size()) {
			AST it = clean.get(index);
			if (it.offset > cur) {
				clean.add(index, new AST(null, cur, it.offset - cur, null));
				index++;
			}
			cur = it.offset + it.length;
			index++;
		}
		if (len > 0 && cur < len) {
			clean.add(new AST(null, cur, len - cur, null));
		}
		return clean;
	}

	private void rebaseToNewText(String dsl, int start) {
		final String old = lastDsl;
		int pos = 0;
		while (pos < dsl.length() && pos < old.length() && dsl.charAt(pos) == old.charAt(pos)) {
			pos++;
		}
		List<AST> newAst = new ArrayList<>(ast.size() + 1);
		synchronized (ast) {
			for (AST a : ast) {
				if (a.offset >= 0 && a.offset + a.length > 0 && a.offset + a.length <= pos) {
					newAst.add(a);
				} else break;
			}
		}
		if (pos < dsl.length()) {
			newAst.add(new AST(null, pos, dsl.length() - pos, null));
		}
		fixupAndReposition(dsl, newAst, start);
	}

	private void setupFullCoverage(String dsl) {
		if (lastDsl.equals(dsl)) {
			position = 0;
			return;
		}
		List<AST> newAst = new ArrayList<AST>(dsl.length() > 0 ? 1 : 0);
		if (dsl.length() > 0) {
			newAst.add(new AST(null, 0, dsl.length(), null));
		}
		fixupAndReposition(dsl, newAst, 0);
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

	private void ensureCoverage(int start) {
		synchronized (ast) {
			if (ast.isEmpty()) return;
			AST last = ast.get(ast.size() - 1);
			if (last.offset + last.length == lastDsl.length()) return;
			logger.warn("lexer coverage out of sync (tokens end at " + (last.offset + last.length)
					+ ", buffer is " + lastDsl.length() + " chars); rebuilding");
			List<AST> rebuilt = new ArrayList<AST>(ast.size() + 1);
			int keepEnd = 0;
			for (AST a : ast) {
				if (a.offset >= keepEnd && a.offset + a.length > keepEnd && a.offset + a.length <= lastDsl.length()) {
					rebuilt.add(a);
					keepEnd = a.offset + a.length;
				} else break;
			}
			fixupAndReposition(lastDsl, rebuilt, start);
		}
		analyzeInBackground(lastDsl);
	}

	private final Runnable waitForDslSync = new DumbAwareRunnable() {
		@Override
		public void run() {
			try {
				do {
					Thread.sleep(100);
				} while (System.currentTimeMillis() < delayUntil && isActive);
			} catch (Exception ignore) {
			}
			waitingForSync = false;
			if (!isActive) return;
			String text = document != null ? document.getText() : lastDsl;
			if (text != null && !text.equals(lastParsedDsl)) {
				analyzeInBackground(text);
			}
		}
	};

	private final Runnable waitForCompiler = new DumbAwareRunnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (Exception ignore) {
			}
			waitingForCompiler = false;
			if (!isActive) return;
			String text = document != null ? document.getText() : lastDsl;
			if (text != null && !text.equals(lastParsedDsl)) {
				analyzeInBackground(text);
			}
		}
	};

	private List<AST> lastParsedAnalysis;
	private String lastParsedDsl;

	@Override
	public void start(@NotNull CharSequence charSequence, int start, int end, int state) {
		final String dsl = charSequence.toString();
		startedText = dsl;
		if (project != null && project.isDisposed() || !isActive) {
			setupFullCoverage(dsl);
			return;
		}
		resolvePsi();
		final boolean nonEditorPage = project == null || psiFile == null || document == null || !document.isWritable();
		if (forceRefresh || nonEditorPage || ast.isEmpty() || (failedNotReady && dslService.isReady())) {
			if (lastParsedAnalysis != null && dsl.equals(lastParsedDsl)) {
				changeAst(start, lastParsedAnalysis);
				lastDsl = lastParsedDsl;
				forceRefresh = false;
			} else {
				if (ast.isEmpty()) {
					List<AST> newAst = new ArrayList<AST>(1);
					newAst.add(new AST(null, 0, dsl.length(), null));
					fixupAndReposition(dsl, newAst, start);
				} else if (!dsl.equals(lastDsl)) {
					rebaseToNewText(dsl, start);
				} else {
					synchronized (ast) {
						position = 0;
						for (int i = 0; i < ast.size(); i++) {
							if (ast.get(i).offset > start) {
								position = i - 1;
								break;
							}
						}
					}
				}
				forceRefresh = false;
				analyzeInBackground(dsl);
			}
		} else if (!dsl.equals(lastDsl)) {
			logger.debug("changed dsl");
			final String actualDsl;
			if (start == end && dsl.isEmpty()) {
				if (psiFile.getLanguage() == DomainSpecificationLanguage.INSTANCE) {
					actualDsl = document != null ? document.getText() : psiFile.getText();
					if (actualDsl.equals(lastDsl)) {
						position = 0;
						return;
					}
				} else {
					//IntelliJ is using hakish way to force refresh
					position = 0;
					return;
				}
			} else actualDsl = dsl;
			rebaseToNewText(actualDsl, start);
			delayUntil = System.currentTimeMillis() + RETRY_DELAY_MS;
			if (!waitingForSync && project.isOpen()) {
				waitingForSync = true;
				application.executeOnPooledThread(waitForDslSync);
			}
		} else if (start == 0 && end == dsl.length()) {
			position = 0;
		}
		ensureCoverage(start);
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
		return current == null ? getBufferEnd() : current.offset;
	}

	@Override
	public int getTokenEnd() {
		AST current = getCurrent();
		return current != null ? current.offset + current.length : getBufferEnd();
	}

	@Override
	public void advance() {
		position++;
	}

	@NotNull
	@Override
	public CharSequence getBufferSequence() {
		return startedText != null ? startedText : lastDsl;
	}

	@Override
	public int getBufferEnd() {
		return getBufferSequence().length();
	}
}
