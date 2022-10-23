using System;
using Microsoft.VisualStudio.Shell;

namespace DSLPlatform
{
	[AttributeUsage(AttributeTargets.Class, AllowMultiple = true, Inherited = true)]
	internal sealed class ProvideSolutionProps : RegistrationAttribute
	{
		public string PropName { get; private set; }

		public ProvideSolutionProps(string propName)
		{
			PropName = propName;
		}

		public override void Register(RegistrationContext context)
		{
			Key childKey = null;
			try
			{
				childKey = context.CreateKey("SolutionPersistence\\" + PropName);
				childKey.SetValue(string.Empty, context.ComponentType.GUID.ToString("B"));
			}
			finally
			{
				if (childKey != null) childKey.Close();
			}
		}

		public override void Unregister(RegistrationContext context)
		{
			context.RemoveKey("SolutionPersistence\\" + PropName);
		}
	}
}
