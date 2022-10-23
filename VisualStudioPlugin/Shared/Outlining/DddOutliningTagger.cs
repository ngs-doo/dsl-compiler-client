using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(ITaggerProvider))]
	[TagType(typeof(DddOutlineTag))]
	[ContentType("ddd")]
	[ContentType("dsl")]
	internal sealed class DddOutliningTaggerProvider : ITaggerProvider
	{
		public ITagger<T> CreateTagger<T>(ITextBuffer buffer) where T : ITag
		{
			Func<ITagger<T>> sc = () => new DddOutliningTagger(buffer) as ITagger<T>;
			return buffer.Properties.GetOrCreateSingletonProperty<ITagger<T>>(sc);
		}
	}

	internal class DddOutliningTagger : ITagger<DddOutlineTag>
	{
		private readonly ITextBuffer Buffer;
		private ITextSnapshot Snapshot;
		private Region[] Regions = new Region[0];
		private volatile bool Invalidated = true;
		private readonly Timer Timer;

		internal DddOutliningTagger(ITextBuffer buffer)
		{
			this.Buffer = buffer;
			this.Snapshot = buffer.CurrentSnapshot;
			this.Timer = new System.Threading.Timer(_ => Task.Factory.StartNew(ParseAndCache), null, -1, -1);
			ParseAndCache();
			Buffer.Changed += (s, ea) =>
			{
				if (ea.After != Buffer.CurrentSnapshot)
					return;
				Invalidated = true;
				lock (Timer)
					Timer.Change(300, -1);
			};
		}

		public event EventHandler<SnapshotSpanEventArgs> TagsChanged = (s, ea) => { };

		public IEnumerable<ITagSpan<DddOutlineTag>> GetTags(NormalizedSnapshotSpanCollection spans)
		{
			if (spans.Count == 0)
				yield break;
			var currentSnapshot = Snapshot;
			var entire = new SnapshotSpan(spans[0].Start, spans[spans.Count - 1].End).TranslateTo(currentSnapshot, SpanTrackingMode.EdgeExclusive);
			var startLineNumber = entire.Start.GetContainingLine().LineNumber;
			var endLineNumber = entire.End.GetContainingLine().LineNumber;
			for (int i = 0; i < Regions.Length; i++)
			{
				var region = Regions[i];
				if (region.StartLine <= endLineNumber && region.EndLine >= startLineNumber)
				{
					var startLine = currentSnapshot.GetLineFromLineNumber(region.StartLine);
					var endLine = currentSnapshot.GetLineFromLineNumber(region.EndLine);

					var startPosition = startLine.Start.Position + region.StartOffset;
					var len = Math.Min(endLine.End - 2 - startPosition, 1000);
					var innerText = currentSnapshot.GetText(startPosition + 1, len);
					yield return new TagSpan<DddOutlineTag>(
						new SnapshotSpan(startLine.Start + region.StartOffset, endLine.End),
						new DddOutlineTag(region.IsNested, innerText, len, region.Rule, endLine.Start.Position + region.EndOffset + 1));
				}
			}
		}

		class LevelInfo
		{
			public int Level;
			public bool IsNested;
		}

		private void ParseAndCache()
		{
			if (!Invalidated)
			{
				lock (Timer)
					Timer.Change(-1, -1);
				return;
			}
			try
			{
				Invalidated = false;
				var newSnapshot = Buffer.CurrentSnapshot;
				var newRegions = new List<Region>();

				PartialRegion currentRegion = null;
				bool validDsl;
				var tokens = SyntaxParser.GetExtensions(newSnapshot, out validDsl);
				if (Invalidated)
					return;
				int currentLevel = 0;
				var levelInfo = new List<LevelInfo>(tokens.Length / 16 + 2);
				LevelInfo lastInfo = null;
				for (int i = 0; i < tokens.Length; i++)
				{
					var t = tokens[i];
					if (t.Type == SyntaxType.RuleExtension)
					{
						lastInfo = new LevelInfo();
						if (currentLevel < levelInfo.Count)
							levelInfo[currentLevel] = lastInfo;
						else
							levelInfo.Add(lastInfo);
						currentLevel++;
						currentRegion = new PartialRegion
						{
							Rule = t.Value,
							Level = currentLevel,
							StartLine = t.Line - 1,
							StartOffset = t.Column,
							PartialParent = currentRegion
						};
					}
					else if (t.Type == SyntaxType.RuleEnd)
					{
						if (currentRegion == null)
							continue;
						lastInfo.Level--;
						if (lastInfo.Level >= 0)
							continue;
						currentLevel--;
						newRegions.Add(new Region
						{
							Rule = t.Value,
							IsNested = lastInfo.IsNested,
							Level = currentLevel,
							StartLine = currentRegion.StartLine,
							StartOffset = currentRegion.StartOffset,
							EndLine = t.Line - 1,
							EndOffset = t.Column - 1
						});
						lastInfo = currentLevel > 0 ? levelInfo[currentLevel - 1] : null;
						if (lastInfo != null)
							lastInfo.Level--;
						currentRegion = currentRegion.PartialParent;
					}
					else if (lastInfo != null)
					{
						lastInfo.Level++;
						lastInfo.IsNested = true;
					}
				}
				if (Invalidated)
					return;

				int changeStart = 0;
				int changeEnd = newSnapshot.Length;

				if (!validDsl)
				{
					var oldSpans = new Span[Regions.Length];
					for (int i = 0; i < Regions.Length; i++)
						oldSpans[i] = AsSnapshotSpan(Regions[i], Snapshot).TranslateTo(newSnapshot, SpanTrackingMode.EdgeExclusive).Span;
					var newSpans = new Span[newRegions.Count];
					for (int i = 0; i < newRegions.Count; i++)
						newSpans[i] = AsSnapshotSpan(newRegions[i], newSnapshot).Span;

					var oldSpanCollection = new NormalizedSpanCollection(oldSpans);
					var newSpanCollection = new NormalizedSpanCollection(newSpans);

					//the changed regions are regions that appear in one set or the other, but not both.
					var removed = NormalizedSpanCollection.Difference(oldSpanCollection, newSpanCollection);

					if (removed.Count > 0)
					{
						changeStart = removed[0].Start;
						changeEnd = removed[removed.Count - 1].End;
					}

					if (newSpans.Length > 0)
					{
						changeStart = Math.Min(changeStart, newSpans[0].Start);
						changeEnd = Math.Max(changeEnd, newSpans[newSpans.Length - 1].End);
					}
				}
				if (Invalidated)
					return;
				Snapshot = newSnapshot;
				Regions = newRegions.ToArray();

				if (changeStart <= changeEnd)
					TagsChanged(this, new SnapshotSpanEventArgs(new SnapshotSpan(newSnapshot, Span.FromBounds(changeStart, changeEnd))));
				lock (Timer)
					Timer.Change(1000, -1);
			}
			catch
			{
				lock (Timer)
					Timer.Change(5000, -1);
			}
		}

		private static SnapshotSpan AsSnapshotSpan(Region region, ITextSnapshot snapshot)
		{
			var startLine = snapshot.GetLineFromLineNumber(region.StartLine);
			var endLine = region.StartLine == region.EndLine
				? startLine
				: snapshot.GetLineFromLineNumber(region.EndLine);
			return new SnapshotSpan(startLine.Start + region.StartOffset, endLine.End);
		}
	}
}
