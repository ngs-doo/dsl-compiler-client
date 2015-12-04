using System.Runtime.Serialization;

namespace DDDLanguage
{
	[DataContract(Namespace = "")]
	internal class SyntaxConcept
	{
		[DataMember]
		public readonly SyntaxType Type;
		[DataMember]
		public readonly string Value;
		[DataMember]
		public readonly string Script;
		[DataMember]
		public readonly int Line;
		[DataMember]
		public readonly int Column;

		public SyntaxConcept(
			SyntaxType type,
			string value,
			string script,
			int line,
			int column)
		{
			this.Type = type;
			this.Value = value;
			this.Script = script;
			this.Line = line;
			this.Column = column;
		}
	}
}
