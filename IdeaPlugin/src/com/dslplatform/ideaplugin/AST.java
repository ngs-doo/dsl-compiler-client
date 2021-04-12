package com.dslplatform.ideaplugin;

import com.dslplatform.compiler.client.parameters.DslCompiler;

import java.util.ArrayList;
import java.util.Comparator;
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

	public static Comparator<AST> SORT = new Comparator<AST>() {
		@Override
		public int compare(AST left, AST right) {
			return left.offset - right.offset;
		}
	};

	@Override
	public String toString() {
		return "AST(" + (concept != null ? concept.type.name() : "empty") + ", " + offset + " - " + length + ")";
	}
}
