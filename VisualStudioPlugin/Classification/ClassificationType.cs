using System.ComponentModel.Composition;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	internal static class OrdinaryClassificationDefinition
	{
		/// <summary>
		/// Defines the "ordinary" classification type.
		/// </summary>
		[Export(typeof(ClassificationTypeDefinition))]
		[Name("ddd-keyword")]
		internal static ClassificationTypeDefinition DddKeyword = null;

		/// <summary>
		/// Defines the "ordinary" classification type.
		/// </summary>
		[Export(typeof(ClassificationTypeDefinition))]
		[Name("ddd-identifier")]
		internal static ClassificationTypeDefinition DddIdentifier = null;

		/// <summary>
		/// Defines the "ordinary" classification type.
		/// </summary>
		[Export(typeof(ClassificationTypeDefinition))]
		[Name("ddd-stringQuote")]
		internal static ClassificationTypeDefinition DddStringQuote = null;
	}
}
