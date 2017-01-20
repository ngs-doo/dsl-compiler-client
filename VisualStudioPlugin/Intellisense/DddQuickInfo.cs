using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Linq;
using Microsoft.VisualStudio.Language.Intellisense;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DDDLanguage.Intellisense
{
	[Export(typeof(IQuickInfoSourceProvider))]
	[ContentType("dsl")]
	[ContentType("ddd")]
	[Name("dddQuickInfo")]
	class DddQuickInfo : IQuickInfoSourceProvider
	{
		[Import]
		IBufferTagAggregatorFactoryService aggService = null;

		public IQuickInfoSource TryCreateQuickInfoSource(ITextBuffer textBuffer)
		{
			return new DddQuickInfoSource(textBuffer, aggService.CreateTagAggregator<DddTokenTag>(textBuffer));
		}
	}

	class DddQuickInfoSource : IQuickInfoSource
	{
		private readonly ITagAggregator<DddTokenTag> Aggregator;
		private readonly ITextBuffer Buffer;
		private bool IsDisposed = false;

		public DddQuickInfoSource(ITextBuffer buffer, ITagAggregator<DddTokenTag> aggregator)
		{
			this.Aggregator = aggregator;
			this.Buffer = buffer;
		}

		public void AugmentQuickInfoSession(
			IQuickInfoSession session,
			IList<object> quickInfoContent,
			out ITrackingSpan applicableToSpan)
		{
			applicableToSpan = null;

			if (IsDisposed) return;

			var triggerPoint = (SnapshotPoint)session.GetTriggerPoint(Buffer.CurrentSnapshot);

			if (triggerPoint == null) return;

			foreach (IMappingTagSpan<DddTokenTag> curTag in Aggregator.GetTags(new SnapshotSpan(triggerPoint, triggerPoint)))
			{
				var t = curTag.Tag;
				if (t.Type != DddTokenTypes.Keyword && t.Type != DddTokenTypes.Identifier && t.Type != DddTokenTypes.StringQuote) continue;
				var tagSpan = curTag.Span.GetSpans(Buffer).First();
				applicableToSpan = Buffer.CurrentSnapshot.CreateTrackingSpan(tagSpan, SpanTrackingMode.EdgeExclusive);
				if (t.Parent != null)
				{
					var rule = t.Parent.Concept.Value;
					var found = SyntaxParser.Find(rule);
					if (found != null)
					{
						if (found.Description != null)
							quickInfoContent.Add(found.Description + Environment.NewLine + "Grammar: " + found.Grammar);
						else
							quickInfoContent.Add("Grammar: " + found.Grammar);
					}
				}
			}
		}

		public void Dispose()
		{
			IsDisposed = true;
		}
	}

}
