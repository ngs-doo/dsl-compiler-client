namespace DDDLanguage
{
	internal class PartialRegion
	{
		public string Rule;
		public int StartLine;
		public int StartOffset;
		public int Level;
		public PartialRegion PartialParent;
	}

	internal class Region : PartialRegion
	{
		public int EndLine;
		public int EndOffset;
	}
}
