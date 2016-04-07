package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class DslLexerParser extends Lexer implements PsiParser {

	private String lastDsl;
	private List<AST> ast = new ArrayList<AST>();
	private int position = 0;

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

	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType iElementType, @NotNull PsiBuilder psiBuilder) {
		psiBuilder.mark().done(iElementType);
		return psiBuilder.getTreeBuilt();
	}

	private AST getCurrent() {
		return position >= 0 && position < ast.size() ? ast.get(position) : null;
	}

	@Override
	public void start(@NotNull CharSequence charSequence, int start, int end, int state) {
		String dsl = charSequence.toString();
		if (!dsl.equals(lastDsl)) {
			lastDsl = dsl;
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
			int cur = 0;
			int index = 0;
			while (index < newAst.size()) {
				AST ast = newAst.get(index);
				if (ast.offset > cur) {
					newAst.add(index, new AST(null, cur, ast.offset - cur));
					index++;
				}
				cur = ast.offset + ast.length;
				index++;
			}
			if (dsl.length() > 0) {
				AST last = newAst.get(newAst.size() - 1);
				int width = last.offset + last.length;
				if (width < dsl.length()) {
					newAst.add(new AST(null, width, dsl.length() - width));
				}
			}
			ast = newAst;
		}
		for (int i = 0; i < ast.size(); i++) {
			if (ast.get(i).offset > start) {
				position = i - 1;
				return;
			}
		}
		position = 0;
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
