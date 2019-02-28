using System;
using Microsoft.VisualStudio.Shell;

namespace DSLPlatform
{
	static class GuidList
	{
		public const string guidDSLPlatformPkgString = "c1c220fd-790d-4c27-a455-53b2e64586fe";
		public const string guidDSLPlatformCmdSetString = "8b294211-e3cf-4b6a-b816-f5687414e4dd";
		public const string guidToolWindowPersistanceString = "80223397-d1f8-4d21-87d3-51f13e9ddabc";

		public static readonly Guid guidDSLPlatformCmdSet = new Guid(guidDSLPlatformCmdSetString);
	}
	static class PkgCmdIDList
	{
		public const int cmdDslPlatformCmd = 0x100;
		public const int cmdDslPlatformTool = 0x101;
		public const int cmdCompileDslCmd = 0x102;
	}
	static class Constants
	{
		public const string vsProjectKindUnmodeled = "{67294A52-A4F0-11D2-AA88-00C04F688DDE}";
	}
	public class VsColors
	{
		public static object Foreground { get { return VsBrushes.CommandBarTextActiveKey; } }
		public static object Background { get { return VsBrushes.CommandBarOptionsBackgroundKey; } }
	}
}
