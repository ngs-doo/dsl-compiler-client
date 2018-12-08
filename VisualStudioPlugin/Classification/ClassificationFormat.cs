using System.ComponentModel.Composition;
using System.Windows.Media;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	/// <summary>
	/// Defines an editor format for the OrdinaryClassification type that has a purple background
	/// and is underlined.
	/// </summary>
	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-keyword")]
	[Name("ddd-keyword")]
	//this should be visible to the end user
	[UserVisible(false)]
	//set the priority to be after the default classifiers
	[Order(Before = Priority.Default)]
	internal sealed class DddKeyword : ClassificationFormatDefinition
	{
		/// <summary>
		/// Defines the visual format for the "ordinary" classification type
		/// </summary>
		public DddKeyword()
		{
			this.DisplayName = "Keyword"; //human readable version of the name
			this.ForegroundColor = Colors.SlateBlue;
		}
	}

	/// <summary>
	/// Defines an editor format for the OrdinaryClassification type that has a purple background
	/// and is underlined.
	/// </summary>
	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-identifier")]
	[Name("ddd-identifier")]
	//this should be visible to the end user
	[UserVisible(false)]
	//set the priority to be after the default classifiers
	[Order(Before = Priority.Default)]
	internal sealed class DddIdentifier : ClassificationFormatDefinition
	{
		/// <summary>
		/// Defines the visual format for the "ordinary" classification type
		/// </summary>
		public DddIdentifier()
		{
			this.DisplayName = "Identifier"; //human readable version of the name
			this.ForegroundColor = Colors.LightSeaGreen;
		}
	}

	/// <summary>
	/// Defines an editor format for the OrdinaryClassification type that has a purple background
	/// and is underlined.
	/// </summary>
	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-stringQuote")]
	[Name("ddd-stringQuote")]
	//this should be visible to the end user
	[UserVisible(false)]
	//set the priority to be after the default classifiers
	[Order(Before = Priority.Default)]
	internal sealed class DddStringQuote : ClassificationFormatDefinition
	{
		/// <summary>
		/// Defines the visual format for the "ordinary" classification type
		/// </summary>
		public DddStringQuote()
		{
			this.DisplayName = "Expression or quote"; //human readable version of the name
			this.ForegroundColor = Colors.Red;
		}
	}
}
