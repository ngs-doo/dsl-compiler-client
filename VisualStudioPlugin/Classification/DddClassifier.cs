using System;
using System.Collections.Generic;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Text.Tagging;

namespace DSLPlatform
{
	internal sealed class DddClassifier : ITagger<ClassificationTag>
	{
		private readonly ITextBuffer Buffer;
		private readonly ITagAggregator<DddTokenTag> Aggregator;
		//TODO: replace with array
		private readonly Dictionary<DddTokenTypes, ClassificationTag> DddTags;

		internal DddClassifier(
			ITextBuffer buffer,
			ITagAggregator<DddTokenTag> DddTagAggregator,
			IClassificationTypeRegistryService typeService)
		{
			Buffer = buffer;
			Aggregator = DddTagAggregator;
			DddTags = new Dictionary<DddTokenTypes, ClassificationTag>();
			DddTags[DddTokenTypes.Keyword] = new ClassificationTag(typeService.GetClassificationType("Script Keyword") ?? typeService.GetClassificationType("ddd-keyword"));
			DddTags[DddTokenTypes.Identifier] = new ClassificationTag(typeService.GetClassificationType("Script Comment") ?? typeService.GetClassificationType("ddd-identifier"));
			DddTags[DddTokenTypes.StringQuote] = new ClassificationTag(typeService.GetClassificationType("Script String") ?? typeService.GetClassificationType("ddd-stringQuote"));
		}

		public event EventHandler<SnapshotSpanEventArgs> TagsChanged { add { } remove { } }

		public IEnumerable<ITagSpan<ClassificationTag>> GetTags(NormalizedSnapshotSpanCollection spans)
		{
			foreach (var tagSpan in Aggregator.GetTags(spans))
			{
				var tagSpans = tagSpan.Span.GetSpans(spans[0].Snapshot);
				ClassificationTag ct;
				if (DddTags.TryGetValue(tagSpan.Tag.Type, out ct))
					yield return new TagSpan<ClassificationTag>(tagSpans[0], ct);
				else continue;
			}
		}
	}
}
