package com.dslplatform.ideaplugin;

import com.dslplatform.grammar.NGSLexer;
import com.dslplatform.grammar.NGSParser;
import com.dslplatform.grammar.SyntaxConcept;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DslLexerParser extends Lexer implements PsiParser {

	private String lastDsl;
	private List<AST> ast = new ArrayList<>();
	private int position = 0;

	static class AST {
		public final SyntaxConcept concept;
		public final TokenType type;
		public final int offset;
		public final int length;

		public AST(SyntaxConcept concept, int offset, int length) {
			this.concept = concept;
			this.type = concept == null ? TokenType.IGNORED : TokenType.from(concept.Type);
			this.offset = offset;
			this.length = length;
		}
	}

	@NotNull
	@Override
	public ASTNode parse(IElementType iElementType, PsiBuilder psiBuilder) {
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

			ANTLRStringStream stream = new ANTLRStringStream(dsl);
			NGSLexer lexer = new NGSLexer(stream);
			NGSParser parser = new NGSParser(new CommonTokenStream(lexer));

			try {
				parser.ProcessDsl("dsl");
			} catch (RecognitionException ignore) {
			}
			List<SyntaxConcept> parsed = parser.GetSyntax();
			List<AST> newAst = new ArrayList<>(parsed.size() * 2);
			String[] lines = dsl.split("\\n");
			int[] linesTotal = new int[lines.length];
			int runningTotal = 0;
			for (int i = 0; i < lines.length; i++) {
				linesTotal[i] = runningTotal;
				runningTotal += lines[i].length() + 1;
			}
			for (SyntaxConcept c : parsed) {
				switch (c.Type) {
					case Identifier:
					case Keyword:
					case StringQuote:
						newAst.add(new AST(c, linesTotal[c.Line - 1] + c.Column, c.Value.length()));
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
