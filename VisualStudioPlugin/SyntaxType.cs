using System.Runtime.Serialization;

namespace DSLPlatform
{
	[DataContract(Namespace = "")]
	internal enum SyntaxType
	{
		[EnumMember]
		Keyword = 0,
		[EnumMember]
		Identifier,
		[EnumMember]
		StringQuote,
		[EnumMember]
		Expression,
		[EnumMember]
		Type,
		[EnumMember]
		Navigation,
		[EnumMember]
		RuleStart,
		[EnumMember]
		RuleExtension,
		[EnumMember]
		RuleEnd
	}
}
