package com.dslplatform.plugin.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Either;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.Download;
import com.dslplatform.compiler.client.parameters.DslCompiler;
import com.dslplatform.compiler.client.parameters.DslCompiler.SyntaxConcept;
import com.dslplatform.compiler.client.parameters.LogOutput;
import com.dslplatform.plugin.Logger;

public class TokenScanner implements ITokenScanner {

	private final ArrayList<DslCompiler.SyntaxConcept> concepts;
	private final List<Integer> lineOffsets;
	private int lastScannedIndex;
	private static DslCompiler.TokenParser tokenParser;

	static {
		Context context = new Context();
		context.put(LogOutput.INSTANCE, null);
		context.put(Download.INSTANCE, null);
		if (!Main.processContext(context, Arrays.<CompileParameter> asList(Download.INSTANCE, DslCompiler.INSTANCE))) {
			Logger.info("Unable to setup DSL command line client");
		}
		final String path = context.get(DslCompiler.INSTANCE);
		if (path == null) {
			Logger.info("Unable to setup dsl-compiler.exe");
		} else {
			final File compiler = new File(path);
			Logger.info("DSL Platform compiler found at: " + compiler.getAbsolutePath());
			Either<DslCompiler.TokenParser> trySetup = DslCompiler.setupServer(context, compiler);
			if (trySetup.isSuccess()) {
				com.dslplatform.plugin.Logger.info("Initializing tokenParser");
				tokenParser = trySetup.get();
				com.dslplatform.plugin.Logger.info("tokenParser initialized");
			}
		}
	}

	public TokenScanner() {
		concepts = new ArrayList<DslCompiler.SyntaxConcept>();
		lineOffsets = new ArrayList<Integer>();
	}

	@Override
	public void setRange(final IDocument document, int offset, int length) {
		try {
			String dsl = document.get(0, document.getLength());
			parse("dsl: " + document.toString(), dsl);
			try {
				calculateLineOffsets(dsl);
			} catch (IOException e) {
				// TODO handle
				e.printStackTrace();
			}
		} catch (BadLocationException e) {
			// TODO handle
			e.printStackTrace();
		}
	}

	@Override
	public IToken nextToken() {
		while (true) {
			lastScannedIndex++;
			if (lastScannedIndex >= concepts.size())
				return Token.EOF;

			SyntaxConcept concept = getLastConcept();
			TextAttribute attr = ClassificationFormat.getTextAttribute(concept);
			if (attr != null) {
				Logger.debug("token: '" + concept.type + "' @" + concept.line + "," + concept.column + "; value: "
						+ concept.value);
				return new Token(attr);
			} else {
				Logger.debug("ignored: '" + concept.type + "' @" + concept.line + "," + concept.column + "; value: "
						+ concept.value);
			}
		}
	}

	@Override
	public int getTokenOffset() {
		SyntaxConcept concept = getLastConcept();
		return getOffset(concept.line, concept.column);
	}

	@Override
	public int getTokenLength() {
		return getLastConcept().value.length();
	}

	private synchronized List<DslCompiler.SyntaxConcept> parseTokens(String dsl) {
		if (tokenParser == null)
			return new ArrayList<DslCompiler.SyntaxConcept>(0);

		Either<DslCompiler.ParseResult> result = tokenParser.parse(dsl);
		if (!result.isSuccess() || result.get().tokens == null) {
			// TODO markers
			Logger.info("Parsing FAIL");
			Logger.info(result.explainError());
			Logger.info(result.whyNot().getMessage());
			return new ArrayList<DslCompiler.SyntaxConcept>(0);
		}

		if (result.get().error != null) {
			Logger.info("Error: " + result.get().error.error);
			
			// TODO markers
			//ParseError error = result.get().error;
			/*Map<String, Object> marker = new HashMap();
			marker.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.put(IMarker.LINE_NUMBER, error.line);
			marker.put(IMarker.MESSAGE, error.error);
			markers.add(marker);*/
			
		}
		return result.get().tokens;
	}

	private void parse(final String script, String content) {
		reset();
		concepts.addAll(content.length() > 0 && tokenParser != null
				? parseTokens(content)
				: new ArrayList<DslCompiler.SyntaxConcept>(0));
	}

	private int getOffset(int line, int column) {
		return lineOffsets.get(line - 1) + column;
	}

	private void calculateLineOffsets(String content) throws IOException {
		char c;
		int offset = 0;
		BufferedReader reader = new BufferedReader(new StringReader(content));

		lineOffsets.clear();
		lineOffsets.add(0);

		while (true) {
			int singleChar = reader.read();
			if (singleChar == -1)
				break;
			c = (char) singleChar;
			offset++;

			if (c == 0)
				break;

			if (c == '\n')
				lineOffsets.add(offset);
			if (c == '\r') {
				c = (char) reader.read();
				if (c == '\n') {
					offset++;
					lineOffsets.add(offset);
				} else {
					lineOffsets.add(offset);
					offset++;
				}
			}
		}
	}

	private void reset() {
		lastScannedIndex = -1;
		concepts.clear();
	}

	private SyntaxConcept getLastConcept() {
		return concepts.get(lastScannedIndex);
	}
}
