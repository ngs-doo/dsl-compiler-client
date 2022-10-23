using System.Collections.Generic;
using Microsoft.VisualStudio.Text.Tagging;

namespace DSLPlatform
{
	internal class DddTokenTag : ITag
	{
		public readonly DddTokenTypes Type;
		public readonly SyntaxConcept Concept;

		public readonly DddTokenTag Parent;
		public readonly List<DddTokenTag> Children = new List<DddTokenTag>();

		public DddTokenTag(DddTokenTag parent, SyntaxConcept concept)
		{
			this.Concept = concept;
			this.Parent = parent;
			this.Type = concept.Type == SyntaxType.RuleStart
				? DddTokenTypes.RuleStart
				: ConceptToToken(concept.Type);
			if (parent != null)
				parent.Children.Add(this);
		}

		public static DddTokenTypes ConceptToToken(SyntaxType syntax)
		{
			switch (syntax)
			{
				case SyntaxType.Keyword:
					return DddTokenTypes.Keyword;
				case SyntaxType.Identifier:
					return DddTokenTypes.Identifier;
				default:
					return DddTokenTypes.StringQuote;
			}
		}

		public override string ToString()
		{
			return Concept.Value;
		}
	}
}
