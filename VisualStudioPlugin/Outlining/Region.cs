namespace DDDLanguage
{
	internal class PartialRegion
	{
		public int StartLine;
		public int StartOffset;
		public int Level;
		public PartialRegion PartialParent;
	}

	internal class Region : PartialRegion
	{
		public int EndLine;
	}
}
