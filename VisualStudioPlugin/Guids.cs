// Guids.cs
// MUST match guids.h
using System;

namespace DDDLanguage
{
	static class GuidList
	{
		public const string guidDDDLanguagePkgString = "c1c220fd-790d-4c27-a455-53b2e64586fe";
		public const string guidDDDLanguageCmdSetString = "8b294211-e3cf-4b6a-b816-f5687414e4dd";
		public const string guidToolWindowPersistanceString = "80223397-d1f8-4d21-87d3-51f13e9ddabc";

		public static readonly Guid guidDDDLanguageCmdSet = new Guid(guidDDDLanguageCmdSetString);
	};
}