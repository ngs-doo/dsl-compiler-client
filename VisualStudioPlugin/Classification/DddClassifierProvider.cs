using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(ITaggerProvider))]
	[ContentType("ddd")]
	[TagType(typeof(ClassificationTag))]
	internal sealed class DddClassifierProvider : ITaggerProvider
	{
		[Export]
		[Name("ddd")]
		[BaseDefinition("text")]
		internal static ContentTypeDefinition DddContentType = null;

		[Export]
		[FileExtension(".ddd")]
		[ContentType("ddd")]
		internal static FileExtensionToContentTypeDefinition DddFileType = null;

		[Import]
		internal IClassificationTypeRegistryService ClassificationTypeRegistry = null;

		[Import]
		internal IBufferTagAggregatorFactoryService AggregatorFactory = null;

		public ITagger<T> CreateTagger<T>(ITextBuffer buffer) where T : ITag
		{
			var tagAggregator = AggregatorFactory.CreateTagAggregator<DddTokenTag>(buffer);
			return new DddClassifier(buffer, tagAggregator, ClassificationTypeRegistry) as ITagger<T>;
		}
	}
}
