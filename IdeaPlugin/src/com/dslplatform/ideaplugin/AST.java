package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.parameters.DslCompiler;

import java.util.ArrayList;
import java.util.List;

class AST {
	final DslCompiler.SyntaxConcept concept;
	final TokenType type;
	final int offset;
	final int length;
	final AST parent;
	final List<AST> children = new ArrayList<AST>();

	AST(DslCompiler.SyntaxConcept concept, int offset, int length, AST parent) {
		this.concept = concept;
		this.type = concept == null ? TokenType.IGNORED : TokenType.from(concept.type);
		this.offset = offset;
		this.length = length;
		this.parent = parent;
		if (parent != null) {
			parent.children.add(this);
		}
	}
}
