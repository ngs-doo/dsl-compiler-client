using System;
using System.Collections.Generic;
using System.Linq;
using System.Reactive.Linq;
using System.Threading.Tasks;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using NGS.Dsl;

namespace DDDLanguage
{
	internal class DddOutliningTagger : ITagger<IOutliningRegionTag>
	{
		private readonly ITextBuffer Buffer;
		private ITextSnapshot Snapshot;
		private Region[] Regions = new Region[0];
		private volatile bool Invalidated = true;
		private readonly IDisposable Subscription;

		internal DddOutliningTagger(ITextBuffer buffer)
		{
			this.Buffer = buffer;
			this.Snapshot = buffer.CurrentSnapshot;
			ParseAndCache();
			Subscription =
				Observable.FromEventPattern<TextContentChangedEventArgs>(Buffer, "Changed")
				.Where(ev => ev.EventArgs.After == Buffer.CurrentSnapshot)
				.Select(ev => { Invalidated = true; return ev; })
				.Throttle(TimeSpan.FromSeconds(0.4))
				.Subscribe(ev => Task.Factory.StartNew(ParseAndCache));
		}

		public event EventHandler<SnapshotSpanEventArgs> TagsChanged = (s, ea) => { };

		public IEnumerable<ITagSpan<IOutliningRegionTag>> GetTags(NormalizedSnapshotSpanCollection spans)
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
					var innerLines = currentSnapshot.GetText(startPosition + 1, len).Split('\n');
					var maxSpace = innerLines.Take(10).Where(it => !string.IsNullOrWhiteSpace(it)).Min(it => it.Length - it.TrimStart().Length);
					var innerText =
						string.Join("\n", innerLines.Take(10).Select(it => string.IsNullOrWhiteSpace(it) ? string.Empty : it.Substring(maxSpace)))
						+ (len > 999 || innerLines.Where(it => it.Length > 0).Count() > 10 ? Environment.NewLine + "..." : string.Empty);
					yield return new TagSpan<IOutliningRegionTag>(
						new SnapshotSpan(startLine.Start + region.StartOffset, endLine.End),
						new OutliningRegionTag(false, false, "{ ... }", innerText));
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
				return;
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
						if (lastInfo.IsNested)
						{
							newRegions.Add(new Region
							{
								Level = currentLevel,
								StartLine = currentRegion.StartLine,
								StartOffset = currentRegion.StartOffset,
								EndLine = t.Line - 1
							});
						}
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
			}
			catch { }
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
