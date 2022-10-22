using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using Microsoft.VisualStudio.Language.Intellisense;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(ICompletionSourceProvider))]
	[ContentType("dsl")]
	[ContentType("ddd")]
	[Name("dddCompletion")]
	class DddCompletionSourceProvider : ICompletionSourceProvider
	{
		[Import]
		private IBufferTagAggregatorFactoryService aggService = null;

		public ICompletionSource TryCreateCompletionSource(ITextBuffer textBuffer)
		{
			return new DddCompletionSource(textBuffer, aggService.CreateTagAggregator<DddOutlineTag>(textBuffer));
		}
	}

	class DddCompletionSource : ICompletionSource
	{
		private static readonly Dictionary<string, List<Completion>> Completitions = new Dictionary<string, List<Completion>>();

		private readonly ITagAggregator<DddOutlineTag> Aggregator;
		private readonly ITextBuffer Buffer;
		private bool IsDisposed;

		public DddCompletionSource(ITextBuffer buffer, ITagAggregator<DddOutlineTag> aggregator)
		{
			this.Buffer = buffer;
			this.Aggregator = aggregator;
		}

		private static string PrepareStaticGrammar(string grammar)
		{
			if (grammar.Length == 0) return string.Empty;
			if (!char.IsLetter(grammar[0])) return string.Empty;
			var i = 0;
			for (; i < grammar.Length; i++)
			{
				if (!char.IsWhiteSpace(grammar[i]) && !char.IsLetterOrDigit(grammar[i])) break;
			}
			if (i == grammar.Length) return grammar;
			return grammar.Substring(0, i).Trim();
		}

		private static string PrettyRuleName(string rule)
		{
			if (string.IsNullOrEmpty(rule)) return "Top level rules";
			return (rule.EndsWith("_rule") ? rule.Substring(0, rule.Length - 5) : rule).Replace('_', ' ');
		}

		private static List<Completion> GetCompletitions(string rule)
		{
			if (Completitions.Count == 0)
			{
				lock (Completitions)
				{
					foreach (var r in SyntaxParser.AllRules)
					{
						if (r.Children.Length > 0)
						{
							var list = new List<Completion>();
							foreach (var c in r.Children)
							{
								var nr = SyntaxParser.Find(c);
								if (nr != null)
								{
									list.Add(new Completion(
										PrettyRuleName(nr.Rule),
										PrepareStaticGrammar(nr.Grammar),
										nr.DescriptionAndGrammar,
										null,
										null));
								}
							}
							if (list.Count > 0)
								Completitions[r.Rule] = list;
						}
					}
				}
			}
			List<Completion> result;
			if (Completitions.TryGetValue(rule, out result))
				return result;
			return null;
		}

		public void AugmentCompletionSession(ICompletionSession session, IList<CompletionSet> completionSets)
		{
			if (IsDisposed) return;

			var snapshot = Buffer.CurrentSnapshot;
			var triggerPoint = session.GetTriggerPoint(snapshot);
			if (triggerPoint == null) return;

			var start = triggerPoint.Value;
			var span = new SnapshotSpan(start, start);
			while (start > 0 && char.IsWhiteSpace((start - 1).GetChar()))
				start -= 1;

			if (start.Position != 0
				&& (start - 1).GetChar() != '{'
				&& (start - 1).GetChar() != '}'
				&& (start - 1).GetChar() != ';') return;

			var parentRule =
				(from t in Aggregator.GetTags(span)
				 where t.Tag.End >= triggerPoint.Value.Position
				 orderby t.Tag.End ascending
				 select t.Tag.Rule).FirstOrDefault() ?? string.Empty;

			var completions = GetCompletitions(parentRule);
			if (completions == null) return;

			var applicableTo = snapshot.CreateTrackingSpan(span, SpanTrackingMode.EdgeInclusive);

			completionSets.Add(new CompletionSet(parentRule, PrettyRuleName(parentRule), applicableTo, completions, Enumerable.Empty<Completion>()));
		}

		public void Dispose()
		{
			IsDisposed = true;
		}
	}
}