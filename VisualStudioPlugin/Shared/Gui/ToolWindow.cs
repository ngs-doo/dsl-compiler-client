using System;
using System.Runtime.InteropServices;
using Microsoft.VisualStudio.Shell;

namespace DSLPlatform
{
	[Guid("80223397-d1f8-4d21-87d3-51f13e9ddabc")]
	public class ToolWindow : ToolWindowPane
	{
		public ToolWindow() :
			base(null)
		{
			this.Caption = "DSL Platform";
			this.BitmapResourceID = 301;
			this.BitmapIndex = 0;

			// This is the user control hosted by the tool window; Note that, even if this class implements IDisposable,
			// we are not calling Dispose on this object. This is because ToolWindowPane calls Dispose on 
			// the object returned by the Content property.
			base.Content = new ToolContent { Content = new AboutControl() };
		}
	}
}
