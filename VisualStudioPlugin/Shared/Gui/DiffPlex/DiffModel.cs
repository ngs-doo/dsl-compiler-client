namespace DSLPlatform
{
	internal class DiffModel
	{
		public string FileName { get; set; }
		public string OriginalSource { get; set; }
		public string NewSource { get; set; }

		public enum ModificationType
		{
			NotModified,
			Modified,
			Created,
			Deleted
		}

		public ModificationType Modified;

		public string Title
		{
			get
			{
				var suffix = "";
				switch (Modified)
				{
					case ModificationType.Modified:
						suffix = " - Modified";
						break;

					case ModificationType.Created:
						suffix = " - Created";
						break;

					case ModificationType.Deleted:
						suffix = " - Deleted";
						break;
				}

				return FileName + suffix;
			}
		}

		public bool Expanded
		{
			get
			{
				return Modified == ModificationType.Modified;
			}
		}
	}
}
