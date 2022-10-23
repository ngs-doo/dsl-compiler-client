using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Media;

namespace DSLPlatform
{
	internal class ScrollViewerSynchronizer
	{
		private readonly List<ScrollViewer> scrollViewers;
		private readonly Dictionary<ScrollBar, ScrollViewer> verticalScrollerViewers = new Dictionary<ScrollBar, ScrollViewer>();
		private readonly Dictionary<ScrollBar, ScrollViewer> horizontalScrollerViewers = new Dictionary<ScrollBar, ScrollViewer>();
		private double verticalScrollOffset;
		private double horizontalScrollOffset;

		public ScrollViewerSynchronizer(ScrollViewer left, ScrollViewer right)
		{
			this.scrollViewers = new List<ScrollViewer> { left, right };
			scrollViewers.ForEach(x => x.Loaded += Scroller_Loaded);
		}

		private void Scroller_Loaded(object sender, RoutedEventArgs e)
		{
			var scrollViewer = (ScrollViewer)sender;
			var ctx = scrollViewers[0].DataContext as DiffModel;
			var diff = new DiffPlex.Differ();
			var ld = diff.CreateLineDiffs(ctx.OriginalSource ?? string.Empty, ctx.NewSource ?? string.Empty, true);
			var total = Math.Max(ld.PiecesNew.Length, ld.PiecesOld.Length);
			if (ld.DiffBlocks.Count > 0 && total > 0)
			{
				var line = Math.Min(ld.DiffBlocks[0].InsertStartB, ld.DiffBlocks[0].DeleteStartA);
				var lh = scrollViewer.ExtentHeight / total;
				if (line > total / 2 && lh > 0)
					line += (int)(scrollViewer.ViewportHeight / lh) / 2;
				if (line > total)
					line = total;
				if (scrollViewer.ViewportHeight / 2 < lh * line)
					verticalScrollOffset = (scrollViewer.ExtentHeight - scrollViewer.ViewportHeight) * line / total;
			}
			scrollViewer.ScrollToVerticalOffset(verticalScrollOffset);
			scrollViewer.ScrollToHorizontalOffset(horizontalScrollOffset);
			scrollViewer.Opacity = 1;
			if (verticalScrollerViewers.Count > 0)
				scrollViewer.ScrollToVerticalOffset(verticalScrollOffset);
			scrollViewer.ApplyTemplate();


			var scrollViewerRoot = (FrameworkElement)VisualTreeHelper.GetChild(scrollViewer, 0);
			var horizontalScrollBar = (ScrollBar)scrollViewerRoot.FindName("PART_HorizontalScrollBar");
			var verticalScrollBar = (ScrollBar)scrollViewerRoot.FindName("PART_VerticalScrollBar");

			if (!horizontalScrollerViewers.Keys.Contains(horizontalScrollBar))
			{
				horizontalScrollerViewers.Add(horizontalScrollBar, scrollViewer);
			}

			if (!verticalScrollerViewers.Keys.Contains(verticalScrollBar))
			{
				verticalScrollerViewers.Add(verticalScrollBar, scrollViewer);
			}

			if (horizontalScrollBar != null)
			{
				horizontalScrollBar.Scroll += HorizontalScrollBar_Scroll;
				horizontalScrollBar.ValueChanged += HorizontalScrollBar_ValueChanged;
			}

			if (verticalScrollBar != null)
			{
				verticalScrollBar.Scroll += VerticalScrollBar_Scroll;
				verticalScrollBar.ValueChanged += VerticalScrollBar_ValueChanged;
			}
		}

		private void VerticalScrollBar_ValueChanged(object sender, RoutedPropertyChangedEventArgs<double> e)
		{
			var changedScrollBar = sender as ScrollBar;
			var changedScrollViewer = verticalScrollerViewers[changedScrollBar];
			Scroll(changedScrollViewer);
		}

		private void VerticalScrollBar_Scroll(object sender, ScrollEventArgs e)
		{
			var changedScrollBar = sender as ScrollBar;
			var changedScrollViewer = verticalScrollerViewers[changedScrollBar];
			Scroll(changedScrollViewer);
		}

		private void HorizontalScrollBar_ValueChanged(object sender, RoutedPropertyChangedEventArgs<double> e)
		{
			var changedScrollBar = sender as ScrollBar;
			var changedScrollViewer = horizontalScrollerViewers[changedScrollBar];
			Scroll(changedScrollViewer);
		}

		private void HorizontalScrollBar_Scroll(object sender, ScrollEventArgs e)
		{
			var changedScrollBar = sender as ScrollBar;
			var changedScrollViewer = horizontalScrollerViewers[changedScrollBar];
			Scroll(changedScrollViewer);
		}

		private void Scroll(ScrollViewer changedScrollViewer)
		{
			verticalScrollOffset = changedScrollViewer.VerticalOffset;
			horizontalScrollOffset = changedScrollViewer.HorizontalOffset;

			foreach (var scrollViewer in scrollViewers.Where(s => s != changedScrollViewer))
			{
				if (scrollViewer.VerticalOffset != changedScrollViewer.VerticalOffset)
					scrollViewer.ScrollToVerticalOffset(changedScrollViewer.VerticalOffset);

				if (scrollViewer.HorizontalOffset != changedScrollViewer.HorizontalOffset)
					scrollViewer.ScrollToHorizontalOffset(changedScrollViewer.HorizontalOffset);
			}
		}
	}
}