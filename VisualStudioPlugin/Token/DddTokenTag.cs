using Microsoft.VisualStudio.Text.Tagging;

namespace DDDLanguage
{
	internal class DddTokenTag : ITag
	{
		public DddTokenTypes type { get; private set; }

		public DddTokenTag(DddTokenTypes type)
		{
			this.type = type;
		}
	}
}
