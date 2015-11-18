package com.dslplatform.plugin.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;

import com.dslplatform.grammar.NGSLexer;
import com.dslplatform.grammar.NGSParser;
import com.dslplatform.grammar.SyntaxConcept;
import com.dslplatform.grammar.SyntaxType;
import com.dslplatform.plugin.Logger;

public class TokenScanner implements ITokenScanner {

	private final ArrayList<SyntaxConcept> concepts;
	private final List<Integer> lineOffsets;
	private int lastScannedIndex;

	public TokenScanner() {
		concepts = new ArrayList<SyntaxConcept>();
		lineOffsets = new ArrayList<Integer>();
	}

	@Override
	public void setRange(final IDocument document, int offset, int length) {
		
		try {
			String dsl = document.get(0, document.getLength());
			parse("dsl", dsl);
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
				Logger.debug("token: '"+concept.Type+"' @"+concept.Line+","+concept.Column+"; value: "+concept.Value);
				return new Token(attr);
			}
			else {
				Logger.debug("ignored: '"+concept.Type+"' @"+concept.Line+","+concept.Column+"; value: "+concept.Value);
			}
		}
	}
	
	@Override
	public int getTokenOffset() {
		SyntaxConcept concept = getLastConcept();
		return getOffset(concept.Line, concept.Column);
	}

	@Override
	public int getTokenLength() {
		return getLastConcept().Value.length();
	}
	
	private void parse(final String script, String content) {
		reset();
		
		final ANTLRStringStream stream = new ANTLRStringStream(content);
		final NGSLexer lexer = new NGSLexer(stream);
		final NGSParser parser = new NGSParser(new CommonTokenStream(lexer));
		
		try {
			parser.ProcessDsl(script);
		} catch (RecognitionException ex) {
			Logger.debug("PARSE EXCEPTION: @line " + ex.line + ex.getMessage());
		}
		
		List<SyntaxConcept> parsed = parser.GetSyntax();
		parsed = orderConcepts(parsed);
		parsed = delimitConcepts(parsed);
		concepts.addAll(parsed);
	}
	
	/**
	 * Append delimiter token after each concept that can be highlighted,
	 * which prevents merging of adjacent tokens with same text attributes 
	 */
	private List<SyntaxConcept> delimitConcepts(List<SyntaxConcept> concepts) {
		List<SyntaxConcept> delimited = new ArrayList<SyntaxConcept>();
		for (int i = 0; i < concepts.size(); i++) {
			SyntaxConcept current = concepts.get(i);
			delimited.add(current);
			if (i < concepts.size()-1) {
				if(ClassificationFormat.hasTextAttribute(current.Type))
					delimited.add(new SyntaxConcept(SyntaxType.Delimiter, " ",
							"dsl", current.Line, current.Column + current.Value.length()));
			}
		}
		return delimited;
	}
	
	/**
	 * Reorder concepts in same order as they appear in the document
	 */
	private List<SyntaxConcept> orderConcepts(List<SyntaxConcept> concepts) {
		List<SyntaxConcept> ordered = new ArrayList<SyntaxConcept>();
		SyntaxConcept current;
		SyntaxConcept previous;
		int pr;
		for (int i=0; i < concepts.size(); i++) {
			current = concepts.get(i);
			pr = i-1;
			while (pr > 0) {
				previous = concepts.get(pr);
				if (previous.Line < current.Line || (previous.Line == current.Line && previous.Column < current.Column))
					break;
				pr--;
			}
			ordered.add(pr+1, current);
		}
		return ordered;
	}

	private int getOffset(int line, int column) {
		return lineOffsets.get(line-1) + column;
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
			c = (char)singleChar;
			offset++;
			
			if (c == 0)
				break;
			
			if (c == '\n')
				lineOffsets.add(offset);
			if (c == '\r') {
				c = (char)reader.read();
				if (c == '\n') {
					offset++;
					lineOffsets.add(offset);
				}
				else {
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
