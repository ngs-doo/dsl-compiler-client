using System;
using System.Collections.Generic;
using Antlr.Runtime;
using Microsoft.VisualStudio.Text;
using NGS.Dsl;

namespace DDDLanguage
{
	internal static class SyntaxParser
	{
		public class ParsedArgs : EventArgs
		{
			public ParsedArgs() { }
			public ParsedArgs(string error) { this.Error = error; }

			public string Error { get; private set; }
		}

		public static event EventHandler<ParsedArgs> Parsed = (_, __) => { };

		private static SyntaxConcept[] Parse(ITextSnapshot snapshot, out bool success)
		{
			var result = new List<SyntaxConcept>(1024);
			var error = NGS.Dsl.Parser.Parse(snapshot.GetText(), result);
			var ex = error as RecognitionException;
			if (ex != null)
			{
				var msg = (ex.Line >= 0 ? "Line: " + ex.Line + ". " : string.Empty) + ex.Message;
				Parsed(snapshot, new ParsedArgs(msg));
			}
			else if (error != null)
				Parsed(snapshot, new ParsedArgs(error.Message));
			else
				Parsed(snapshot, new ParsedArgs());
			success = error == null;
			return result.ToArray();
		}

		public static SyntaxConcept[] GetTokens(ITextSnapshot snapshot, out bool success)
		{
			var tokens = Parse(snapshot, out success);
			var list = new List<SyntaxConcept>(tokens.Length / 3);
			for (int i = 0; i < tokens.Length; i++)
			{
				var t = tokens[i];
				if (t.Type == SyntaxType.Keyword
					|| t.Type == SyntaxType.Identifier
					|| t.Type == SyntaxType.StringQuote)
					list.Add(t);
			}
			return list.ToArray();
		}

		public static SyntaxConcept[] GetExtensions(ITextSnapshot snapshot, out bool success)
		{
			var tokens = Parse(snapshot, out success);
			var list = new List<SyntaxConcept>(tokens.Length / 3);
			for (int i = 0; i < tokens.Length; i++)
			{
				var t = tokens[i];
				if (t.Type == SyntaxType.RuleStart
					|| t.Type == SyntaxType.RuleExtension
					|| t.Type == SyntaxType.RuleEnd)
					list.Add(t);
			}
			return list.ToArray();
		}
	}
}
