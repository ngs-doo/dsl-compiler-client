using System.ComponentModel.Composition;
using System.Windows.Media;
using Microsoft.VisualStudio.Text.Classification;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-keyword")]
	[Name("ddd-keyword")]
	[UserVisible(false)]
	[Order(Before = Priority.Default)]
	internal sealed class DddKeyword : ClassificationFormatDefinition
	{
		public DddKeyword()
		{
			this.DisplayName = "Keyword"; //human readable version of the name
			this.ForegroundColor = Colors.SlateBlue;
		}
	}

	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-identifier")]
	[Name("ddd-identifier")]
	[UserVisible(false)]
	[Order(Before = Priority.Default)]
	internal sealed class DddIdentifier : ClassificationFormatDefinition
	{
		public DddIdentifier()
		{
			this.DisplayName = "Identifier"; //human readable version of the name
			this.ForegroundColor = Colors.LightSeaGreen;
		}
	}

	[Export(typeof(EditorFormatDefinition))]
	[ClassificationType(ClassificationTypeNames = "ddd-stringQuote")]
	[Name("ddd-stringQuote")]
	[UserVisible(false)]
	[Order(Before = Priority.Default)]
	internal sealed class DddStringQuote : ClassificationFormatDefinition
	{
		public DddStringQuote()
		{
			this.DisplayName = "Expression or quote"; //human readable version of the name
			this.ForegroundColor = Colors.Red;
		}
	}
}
