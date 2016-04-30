package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.lexer.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
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

import java.io.*;
import java.util.*;

public class DslLexerParser extends Lexer {

	private final Project project;
	private final PsiFile psiFile;
	private final Document document;
	private final Application application;

	private boolean forceRefresh;
	private boolean waitingForSync;
	private String lastDsl;
	private final List<AST> ast = new ArrayList<AST>();
	private int position = 0;

	public DslLexerParser(Project project, VirtualFile file) {
		this.project = project;
		this.application = ApplicationManager.getApplication();
		if (project != null && file != null) {
			psiFile = PsiManager.getInstance(project).findFile(file);
			document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
		} else {
			psiFile = null;
			document = null;
		}
	}

	static class AST {
		public final DslCompiler.SyntaxConcept concept;
		public final TokenType type;
		public final int offset;
		public final int length;

		public AST(DslCompiler.SyntaxConcept concept, int offset, int length) {
			this.concept = concept;
			this.type = concept == null ? TokenType.IGNORED : TokenType.from(concept.type);
			this.offset = offset;
			this.length = length;
		}
	}

	static class DslContext extends Context {

		private final Logger logger;

		public DslContext(Logger logger) {
			this.logger = logger;
		}

		public void show(String... values) {
			for (String v : values) {
				logger.info(v);
			}
		}

		public void log(String value) {
			logger.debug(value);
		}

		public void log(char[] value, int len) {
			logger.debug(new String(value, 0, len));
		}

		public void error(String value) {
			logger.warn(value);
		}

		public void error(Exception ex) {
			logger.warn(ex.getMessage());
			logger.debug(ex.toString());
		}
	}

	private static DslCompiler.TokenParser tokenParser;

	static {
		Logger logger = com.intellij.openapi.diagnostic.Logger.getInstance("DSL Platform");
		DslContext context = new DslContext(logger);
		context.put(Download.INSTANCE, null);
		if (!Main.processContext(context, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE))) {
			logger.warn("Unable to setup DSL command line client");
		}
		final String path = context.get(DslCompiler.INSTANCE);
		if (path == null) {
			logger.error("Unable to setup dsl-compiler.exe");
		} else {
			final File compiler = new File(path);
			logger.info("DSL Platform compiler found at: " + compiler.getAbsolutePath());
			Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
			if (trySetup.isSuccess()) {
				tokenParser = trySetup.get();
				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							tokenParser.close();
							tokenParser = null;
						} catch (Exception ignore) {
						}
					}
				}));
			}
		}
	}

	private AST getCurrent() {
		return position >= 0 && position < ast.size() ? ast.get(position) : null;
	}

	private void analyze(String dsl, int start) {
		List<DslCompiler.SyntaxConcept> parsed = dsl.length() > 0 && tokenParser != null
				? parseTokens(dsl)
				: new ArrayList<DslCompiler.SyntaxConcept>(0);
		List<AST> newAst = new ArrayList<AST>(parsed.size() * 2);
		String[] lines = dsl.split("\\n");
		int[] linesTotal = new int[lines.length];
		int runningTotal = 0;
		for (int i = 0; i < lines.length; i++) {
			linesTotal[i] = runningTotal;
			runningTotal += lines[i].length() + 1;
		}
		for (DslCompiler.SyntaxConcept c : parsed) {
			switch (c.type) {
				case Identifier:
				case Keyword:
				case StringQuote:
					newAst.add(new AST(c, linesTotal[c.line - 1] + c.column, c.value.length()));
			}
		}
		if (newAst.size() == 0 && dsl.length() > 0) {
			newAst.add(new AST(null, 0, dsl.length()));
		}
		fixupAndReposition(dsl, newAst, start);
		forceRefresh = false;
	}

	private void fixupAndReposition(String dsl, List<AST> newAst, int start) {
		int cur = 0;
		int index = 0;
		while (index < newAst.size()) {
			AST it = newAst.get(index);
			if (it.offset > cur) {
				newAst.add(index, new AST(null, cur, it.offset - cur));
				index++;
			}
			cur = it.offset + it.length;
			index++;
		}
		if (dsl.length() > 0) {
			AST last = newAst.get(newAst.size() - 1);
			int width = last.offset + last.length;
			if (width < dsl.length()) {
				newAst.add(new AST(null, width, dsl.length() - width));
			}
		}
		synchronized (ast) {
			ast.clear();
			ast.addAll(newAst);
			for (int i = 0; i < ast.size(); i++) {
				if (ast.get(i).offset > start) {
					position = i - 1;
					return;
				}
			}
			position = 0;
		}
	}

	private final Runnable refreshAll = new DumbAwareRunnable() {
		@Override
		public void run() {
			document.setText(document.getText());
		}
	};

	private final Runnable scheduleRefresh = new DumbAwareRunnable() {
		@Override
		public void run() {
			forceRefresh = true;
			application.runWriteAction(refreshAll);
		}
	};

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
				String currentDsl;
				do {
					Thread.sleep(300);
					currentDsl = application.runReadAction(obtainLatestDsl);
				} while (!currentDsl.equals(lastDsl));
				waitingForSync = false;
				application.invokeLater(scheduleRefresh);
			} catch (Exception ignore) {
			}
		}
	};

	@Override
	public void start(@NotNull CharSequence charSequence, int start, int end, int state) {
		final String dsl = charSequence.toString();
		lastDsl = dsl;
		if (project == null || forceRefresh || ast.size() == 0 && dsl.length() > 0) {
			analyze(dsl, start);
		} else {
			List<AST> newAst = new ArrayList<AST>(1);
			newAst.add(new AST(null, 0, dsl.length()));
			fixupAndReposition(dsl, newAst, start);
			if (!waitingForSync && project.isOpen() && psiFile != null) {
				waitingForSync = true;
				application.executeOnPooledThread(waitForDslSync);
			}
		}
	}

	private static synchronized List<DslCompiler.SyntaxConcept> parseTokens(String dsl) {
		Either<DslCompiler.ParseResult> result = tokenParser.parse(dsl);
		if (!result.isSuccess() || result.get().tokens == null) {
			return new ArrayList<DslCompiler.SyntaxConcept>(0);
		}
		return result.get().tokens;
	}

	static class OffsetPosition implements LexerPosition {

		private final int offset;
		private final int state;

		public OffsetPosition(int offset, int state) {
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
