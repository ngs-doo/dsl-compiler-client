using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.Text;
using Microsoft.VisualStudio.Text;

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

		[DataContract(Namespace = "")]
		internal class ParseError
		{
			[DataMember]
			public int Line { get; set; }
			[DataMember]
			public int Column { get; set; }
			[DataMember]
			public string Error { get; set; }
		}
		[DataContract(Namespace = "")]
		internal class ParseResult
		{
			[DataMember]
			public ParseError Error { get; set; }
			[DataMember]
			public List<SyntaxConcept> Tokens { get; set; }
		}

		private static readonly DataContractJsonSerializer Serializer = new DataContractJsonSerializer(typeof(ParseResult));

		private static SyntaxConcept[] Parse(ITextSnapshot snapshot, out bool success)
		{
			var sb = new StringBuilder();
			sb.Append("format=json tokens=");
			var dsl = snapshot.GetText();
			sb.Append(Encoding.UTF8.GetByteCount(dsl));
			var either = Compiler.CompileDsl(sb, null, dsl, cms => (ParseResult)Serializer.ReadObject(cms));
			if (!either.Success)
			{
				success = false;
				Parsed(snapshot, new ParsedArgs(either.Error));
				return new SyntaxConcept[0];
			}
			var result = either.Value;
			if (result.Error != null)
			{
				var msg = (result.Error.Line >= 0 ? "Line: " + result.Error.Line + ". " : string.Empty) + result.Error.Error;
				Parsed(snapshot, new ParsedArgs(msg));
			}
			else Parsed(snapshot, new ParsedArgs());
			success = result.Error == null;
			if (result.Tokens == null)
				return EmptyResult;
			return result.Tokens.ToArray();
		}

		private static readonly SyntaxConcept[] EmptyResult = new SyntaxConcept[0];

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
