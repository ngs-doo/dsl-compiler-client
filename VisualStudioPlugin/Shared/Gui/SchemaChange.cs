using System.Runtime.Serialization;
using System.Windows;

namespace DSLPlatform
{
	[DataContract(Namespace = "")]
	public struct SchemaChange
	{
		[DataContract(Namespace = "")]
		public enum ChangeType
		{
			[EnumMember]
			Unknown,
			[EnumMember]
			Remove,
			[EnumMember]
			Create,
			[EnumMember]
			Rename,
			[EnumMember]
			Move,
			[EnumMember]
			Copy
		}
		[DataMember]
		public ChangeType Type { get; set; }
		[DataMember]
		public string Definition { get; set; }
		[DataMember]
		public string Description { get; set; }

		public bool IsUnsafe()
		{
			return Type == ChangeType.Unknown || Type == ChangeType.Remove;
		}

		public Visibility UnsafeVisibility
		{
			get { return IsUnsafe() ? Visibility.Visible : Visibility.Collapsed; }
		}
		public Visibility SafeVisibility
		{
			get { return IsUnsafe() ? Visibility.Collapsed : Visibility.Visible; }
		}

		public FontWeight FontWeight
		{
			get { return IsUnsafe() ? FontWeights.Bold : FontWeights.Normal; }
		}
	}
}
