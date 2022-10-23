using System;
using System.Collections.Generic;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.Text;
using Microsoft.VisualStudio.Text;

namespace DSLPlatform
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
		[DataContract(Namespace = "")]
		internal class RuleInfo
		{
			[DataMember]
			public string Rule { get; set; }
			[DataMember]
			public string Grammar { get; set; }
			[DataMember]
			public string[] Children { get; set; }
			[DataMember]
			public string Description { get; set; }
			public string DescriptionAndGrammar
			{
				get
				{
					if (string.IsNullOrEmpty(Description)) return "Grammar: " + Grammar;
					return Description + Environment.NewLine + "Grammar: " + Grammar;
				}
			}
		}

		private static readonly DataContractJsonSerializer ParseSerializer = new DataContractJsonSerializer(typeof(ParseResult));
		private static readonly DataContractJsonSerializer RuleSerializer = new DataContractJsonSerializer(typeof(RuleInfo[]));
		private static readonly List<RuleInfo> Rules = new List<RuleInfo>();
		private static readonly Dictionary<string, RuleInfo> RuleMap = new Dictionary<string, RuleInfo>();
		public static RuleInfo[] AllRules
		{
			get
			{
				InitRules();
				return Rules.ToArray();
			}
		}
		public static RuleInfo Find(string rule)
		{
			if (string.IsNullOrEmpty(rule))
				return null;
			InitRules();
			RuleInfo result;
			if (RuleMap.TryGetValue(rule, out result))
				return result;
			return null;
		}
		private static void InitRules()
		{
			if (Rules.Count != 0)
				return;
			try
			{
				lock (Rules)
				{
					var either = Compiler.Load("format=json rules", cms => (RuleInfo[])RuleSerializer.ReadObject(cms));
					if (either.Success && either.Value != null)
					{
						Rules.AddRange(either.Value);
						foreach (var r in either.Value)
							RuleMap[r.Rule] = r;
					}
				}
			}
			catch { }
		}

		private static SyntaxConcept[] Parse(ITextSnapshot snapshot, out bool success)
		{
			var sb = new StringBuilder();
			sb.Append("format=json tokens=");
			var dsl = snapshot.GetText();
			sb.Append(Encoding.UTF8.GetByteCount(dsl));
			var either = Compiler.CompileDsl(sb, null, dsl, cms => (ParseResult)ParseSerializer.ReadObject(cms));
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

		public static DddTokenTag[] GetTokens(ITextSnapshot snapshot, out bool success)
		{
			var tokens = Parse(snapshot, out success);
			var list = new List<DddTokenTag>(tokens.Length / 3);
			var stack = new Stack<DddTokenTag>();
			DddTokenTag current = null;
			for (int i = 0; i < tokens.Length; i++)
			{
				var t = tokens[i];
				if (t.Type == SyntaxType.RuleStart)
				{
					stack.Push(current = new DddTokenTag(current, t));
				}
				else if (t.Type == SyntaxType.RuleEnd)
				{
					stack.Pop();
					current = stack.Count > 0 ? stack.Peek() : null;
				}
				else if (t.Type == SyntaxType.Keyword
					|| t.Type == SyntaxType.Identifier
					|| t.Type == SyntaxType.StringQuote)
				{
					list.Add(new DddTokenTag(current, t));
				}
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
