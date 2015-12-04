using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.Serialization;

namespace DDDLanguage
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
