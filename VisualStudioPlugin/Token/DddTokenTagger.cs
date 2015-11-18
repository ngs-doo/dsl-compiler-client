using System;
using System.Collections.Generic;
using System.Reactive.Linq;
using System.Threading.Tasks;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using NGS.Dsl;

namespace DDDLanguage
{
	internal sealed class DddTokenTagger : ITagger<DddTokenTag>
	{
		private readonly ITextBuffer Buffer;
		//TODO: replace with array
		private readonly Dictionary<SyntaxType, DddTokenTag> DddTags;
		private ITagSpan<DddTokenTag>[] Tags = new ITagSpan<DddTokenTag>[0];
		private volatile bool Invalidated = true;
		private readonly IDisposable Subscription;

		internal DddTokenTagger(ITextBuffer buffer)
		{
			Buffer = buffer;
			DddTags = new Dictionary<SyntaxType, DddTokenTag>();
			DddTags[SyntaxType.Keyword] = new DddTokenTag(DddTokenTypes.Keyword);
			DddTags[SyntaxType.Identifier] = new DddTokenTag(DddTokenTypes.Identifier);
			DddTags[SyntaxType.StringQuote] = new DddTokenTag(DddTokenTypes.StringQuote);
			ParseAndCache();
			Subscription =
				Observable.FromEventPattern<TextContentChangedEventArgs>(Buffer, "Changed")
				.Where(ev => ev.EventArgs.After == Buffer.CurrentSnapshot)
				.Select(ev => { Invalidated = true; return ev; })
				.Throttle(TimeSpan.FromSeconds(0.4))
				.Subscribe(ev => Task.Factory.StartNew(ParseAndCache));
		}

		private void ParseAndCache()
		{
			if (!Invalidated)
				return;
			try
			{
				Invalidated = false;
				var snapshot = Buffer.CurrentSnapshot;
				bool validDsl;
				var tokens = SyntaxParser.GetTokens(snapshot, out validDsl);
				var arr = new ITagSpan<DddTokenTag>[tokens.Length];
				int previousLine = -1;
				ITextSnapshotLine line = null;
				for (int i = 0; i < tokens.Length; i++)
				{
					var t = tokens[i];
					if (t.Line != previousLine)
					{
						line = snapshot.GetLineFromLineNumber(t.Line - 1);
						previousLine = t.Line;
					}
					var span = new SnapshotSpan(snapshot, new Span(line.Start.Position + t.Column, t.Value.Length));
					arr[i] = new TagSpan<DddTokenTag>(span, DddTags[t.Type]);
				}
				if (!validDsl)
				{
					if (TagsEqual(Tags, arr))
						return;
				}
				if (Invalidated)
					return;
				Tags = arr;
				TagsChanged(this, new SnapshotSpanEventArgs(new SnapshotSpan(snapshot, Span.FromBounds(0, snapshot.Length))));
			}//TODO error handler
			catch { }
		}

		private static bool TagsEqual(ITagSpan<DddTokenTag>[] left, ITagSpan<DddTokenTag>[] right)
		{
			if (left.Length != right.Length)
				return false;
			for (int i = 0; i < left.Length; i++)
				if (!left[i].Equals(right[i]))
					return false;
			return true;
		}

		public event EventHandler<SnapshotSpanEventArgs> TagsChanged = (s, ea) => { };

		public IEnumerable<ITagSpan<DddTokenTag>> GetTags(NormalizedSnapshotSpanCollection collection)
		{
			return Tags;
		}
	}
}
