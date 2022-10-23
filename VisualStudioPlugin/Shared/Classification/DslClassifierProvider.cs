using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Text.Tagging;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(ITaggerProvider))]
	[ContentType("dsl")]
	[TagType(typeof(ClassificationTag))]
	internal sealed class DslClassifierProvider : ITaggerProvider
	{
		[Export]
		[Name("dsl")]
		[BaseDefinition("text")]
		internal static ContentTypeDefinition DslContentType = null;

		[Export]
		[FileExtension(".dsl")]
		[ContentType("dsl")]
		internal static FileExtensionToContentTypeDefinition DslFileType = null;

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
