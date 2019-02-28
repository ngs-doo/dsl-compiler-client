using Microsoft.VisualStudio.Utilities;
using System.ComponentModel.Composition;

namespace DSLPlatform
{
	internal static class FileAndContentTypeDefinitions
	{
		[Export]
		[Name("dsl")]
		[BaseDefinition("text")]
		internal static ContentTypeDefinition hidingContentTypeDefinitionDsl;

		[Export]
		[Name("ddd")]
		[BaseDefinition("text")]
		internal static ContentTypeDefinition hidingContentTypeDefinitionDdd;

		[Export]
		[FileExtension(".dsl")]
		[ContentType("dsl")]
		internal static FileExtensionToContentTypeDefinition hiddenFileExtensionDefinitionDsl;

		[Export]
		[FileExtension(".ddd")]
		[ContentType("ddd")]
		internal static FileExtensionToContentTypeDefinition hiddenFileExtensionDefinitionDdd;
	}
}
