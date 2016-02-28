package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.*;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
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
		public final StringBuilder showLog = new StringBuilder();
		public final StringBuilder errorLog = new StringBuilder();
		public final StringBuilder traceLog = new StringBuilder();

		public void reset() {
			showLog.setLength(0);
			errorLog.setLength(0);
			traceLog.setLength(0);
		}

		public void show(String... values) {
			for (String v : values) {
				showLog.append(v);
			}
		}

		public void log(String value) {
			traceLog.append(value);
		}

		public void log(char[] value, int len) {
			traceLog.append(value, 0, len);
		}

		public void error(String value) {
			errorLog.append(value);
		}

		public void error(Exception ex) {
			errorLog.append(ex.getMessage());
			traceLog.append(ex.toString());
		}
	}

	private final static DslContext context = new DslContext();
	private static Socket socket;

	private final static File compiler;
	private static Process process;
	private static long startedOn;
	private static int port;

	static {
		DslContext ctx = new DslContext();
		ctx.put(Download.INSTANCE, null);
		if (!Main.processContext(ctx, Arrays.<CompileParameter>asList(Download.INSTANCE, DslCompiler.INSTANCE))) {
			System.err.println("Unable to setup DSL command line client");
		}
		final String path = ctx.get(DslCompiler.INSTANCE);
		if (path == null) {
			System.err.println("Unable to setup dsl-compiler.exe");
			compiler = new File("dsl-compiler.exe");
		} else {
			compiler = new File(path);
			System.out.println("DSL Platform compiler found at: " + compiler.getAbsolutePath());
			startServer(ctx);
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					stopServer();
				}
			}));
		}
	}

	private static synchronized void socketCleanup(DslContext context, boolean restartServer) {
		if (context.errorLog.length() > 0) {
			System.err.println(context.errorLog.toString());
		}
		long now = (new Date()).getTime();
		if (restartServer && now > startedOn + 60000) {
			stopServer();
		}
		final Socket sock = socket;
		if (sock != null) {
			try {
				sock.close();
			} catch (Exception ignore) {
			}
			socket = null;
		}
	}

	private static synchronized void stopServer() {
		final Process proc = process;
		if (proc != null) {
			System.out.println("Stopped DSL Platform compiler");
			try {
				proc.destroy();
			} catch (Exception ignore) {
			}
			process = null;
		}
	}

	private static synchronized void startServer(DslContext context) {
		System.out.println("Starting DSL Platform compiler...");
		stopServer();
		Random rnd = new Random();
		port = rnd.nextInt(40000) + 20000;
		Either<Process> tryProcess = DslCompiler.startServer(context, compiler, port);
		startedOn = (new Date()).getTime();
		final Process proc = tryProcess.isSuccess() ? tryProcess.get() : null;
		process = proc;
		if (proc != null) {
			System.out.println("Started DSL Platform compiler at port: " + port);
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						proc.waitFor();
					} catch (Exception ignore) {
					}
					System.out.println("DSL Platform compiler process stopped");
					process = null;
				}
			});
			thread.setDaemon(true);
			thread.start();
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
			List<DslCompiler.SyntaxConcept> parsed = dsl.length() > 0
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
		try {
			context.reset();
			if (process == null) {
				startServer(context);
			} else {
				socket = setupSocket(socket);
				Either<DslCompiler.ParseResult> result = DslCompiler.parseTokens(context, socket, dsl);
				if (!result.isSuccess()) {
					socketCleanup(context, false);
					socket = setupSocket(null);
					result = DslCompiler.parseTokens(context, socket, dsl);
				}
				if (result.isSuccess()) {
					return result.get().tokens;
				} else {
					socketCleanup(context, true);
				}
			}
		} catch (Exception ex) {
			socketCleanup(context, true);
		}
		return new ArrayList<DslCompiler.SyntaxConcept>(0);
	}

	private static synchronized Socket setupSocket(Socket socket) throws ExitException, IOException {
		if (socket != null) return socket;
		context.put(DslCompiler.INSTANCE, Integer.toString(port));
		if (!DslCompiler.INSTANCE.check(context)) {
			throw new IOException("Unable to setup socket");
		}
		return context.load(DslCompiler.DSL_COMPILER_SOCKET);
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
