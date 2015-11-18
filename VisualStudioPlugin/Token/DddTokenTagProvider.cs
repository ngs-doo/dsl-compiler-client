using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DDDLanguage
{
	[Export(typeof(ITaggerProvider))]
	[ContentType("ddd")]
	[ContentType("dsl")]
	[TagType(typeof(DddTokenTag))]
	internal sealed class DddTokenTagProvider : ITaggerProvider
	{
		public ITagger<T> CreateTagger<T>(ITextBuffer buffer) where T : ITag
		{
			return new DddTokenTagger(buffer) as ITagger<T>;
		}
	}

}
