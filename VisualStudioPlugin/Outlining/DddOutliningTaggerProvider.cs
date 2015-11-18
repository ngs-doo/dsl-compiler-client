using System;
using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DDDLanguage
{
	[Export(typeof(ITaggerProvider))]
	[TagType(typeof(IOutliningRegionTag))]
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
}
