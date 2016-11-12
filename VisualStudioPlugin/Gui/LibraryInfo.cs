using System;
using System.IO;
using System.Linq;
using System.Windows.Media;

namespace DDDLanguage
{
	internal class LibraryInfo : IEquatable<LibraryInfo>, ICloneable
	{
		internal bool SourceOnly { get; private set; }
		internal bool CompileOption { get; set; }
		public bool Compile
		{
			get { return CompileOption; }
			set { CompileOption = value && CanCompile; }
		}
		private string relativeTaget;
		public string Target
		{
			get { return relativeTaget; }
			set
			{
				if (value == null)
					return;
				if (BasePath != null && value.StartsWith(BasePath, StringComparison.InvariantCultureIgnoreCase))
					relativeTaget = value.Substring(BasePath.Length);
				else
					relativeTaget = value;
			}
		}
		private string relativeDependencies;
		public string Dependencies
		{
			get { return relativeDependencies; }
			set
			{
				if (value == null)
					return;
				if (BasePath != null && value.StartsWith(BasePath, StringComparison.InvariantCultureIgnoreCase))
					relativeDependencies = value.Substring(BasePath.Length);
				else
					relativeDependencies = value;
			}
		}

		public string Type { get; private set; }
		public string Name { get; set; }
		public string CompilerName { get; private set; }
		public string Extension { get; private set; }
		public string[] References { get; private set; }

		public static string BasePath { get; set; }

		public LibraryInfo(
			string type,
			string compilerName,
			string[] references,
			bool sourceOnly = false,
			string extension = null)
		{
			Type = type;
			if (sourceOnly)
			{
				Name = type;
				Target = type;
			}
			else
			{
				Name = type + "Model";
				Target = "lib";
			}
			CompilerName = compilerName;
			References = references;
			SourceOnly = sourceOnly;
			Extension = extension;
			Dependencies = Path.Combine("dependencies", type);
			WithActiveRecord = WithHelperMethods = true;
		}

		public string TargetPath { get { return Path.Combine(BasePath, Target); } }
		public string DependenciesPath { get { return Path.Combine(BasePath, Dependencies); } }
		public bool TargetExists { get { return PathExists(Target); } }
		public bool DependenciesExists { get { return PathExists(Dependencies); } }

		public bool WithActiveRecord { get; set; }
		public bool WithHelperMethods { get; set; }
		public bool WithManualJson { get; set; }
		public bool UseUtc { get; set; }
		public bool MinimalSerialization { get; set; }
		public bool NoPrepareExecute { get; set; }
		public bool Legacy { get; set; }
		public string Namespace { get; set; }

		public static bool PathExists(string relative)
		{
			return !string.IsNullOrWhiteSpace(BasePath)
				&& Directory.Exists(Path.Combine(BasePath, relative));
		}

		public Brush TargetColor { get { return TargetExists ? Brushes.Black : Brushes.Red; } }
		public Brush DependenciesColor { get { return DependenciesExists ? Brushes.Black : Brushes.Red; } }

		public string StatusDescription
		{
			get
			{
				if (SourceOnly)
				{
					if (TargetExists)
						return "Source can be created. After compilation source will be placed to " + Target;
					return @"Target path not found. 
Please create or specify target path where generated sources will be placed.";
				}
				var hasFiles = DependenciesExists
					&& Directory.EnumerateFiles(DependenciesPath, "*.dll", SearchOption.TopDirectoryOnly).Any();
				if (TargetExists && DependenciesExists && hasFiles)
					return
						"Library can be used. After compilation DLL will be copied to "
						+ Target + " as " + Name + ".dll" + Environment.NewLine
						+ "Dependencies can be found in " + Dependencies + " and should be referenced from project.";
				return
					(string.IsNullOrWhiteSpace(Name) ? @"DLL name not specified.
Please specify DLL for compiled library from DSL model." + Environment.NewLine : string.Empty)
					+ (TargetExists ? string.Empty : @"Target path not found. 
Please create or specify target path from where compiled DLL will be referenced." + Environment.NewLine)
					+ (DependenciesExists ? string.Empty : @"Dependency path not found. 
Please create or specify dependency path for library and reference it from project.")
					+ (DependenciesExists && !hasFiles ? "Dependencies not found if dependency folder: " + Dependencies + @"
Please download dependencies before running compilation" : string.Empty);
			}
		}

		public bool CanCompile
		{
			get
			{
				if (SourceOnly)
					return TargetExists;
				return !string.IsNullOrWhiteSpace(Name)
					&& TargetExists
					&& DependenciesExists
					&& Directory.EnumerateFiles(DependenciesPath, "*.dll", SearchOption.TopDirectoryOnly).Any();
			}
		}

		public LibraryInfo Clone()
		{
			return new LibraryInfo(Type, CompilerName, References, SourceOnly, Extension)
			{
				CompileOption = CompileOption,
				Name = Name,
				Target = Target,
				Dependencies = Dependencies,
				WithActiveRecord = WithActiveRecord,
				WithHelperMethods = WithHelperMethods,
				WithManualJson = WithManualJson,
				UseUtc = UseUtc,
				MinimalSerialization = MinimalSerialization,
				NoPrepareExecute = NoPrepareExecute,
				Legacy = Legacy,
				Namespace = Namespace
			};
		}
		object ICloneable.Clone() { return Clone(); }

		public bool Equals(LibraryInfo other)
		{
			return other != null
				&& other.CompileOption == this.CompileOption
				&& other.Name == this.Name
				&& other.Target == this.Target
				&& other.Dependencies == this.Dependencies
				&& other.WithActiveRecord == this.WithActiveRecord
				&& other.WithHelperMethods == this.WithHelperMethods
				&& other.WithManualJson == this.WithManualJson
				&& other.UseUtc == this.UseUtc
				&& other.MinimalSerialization == this.MinimalSerialization
				&& other.NoPrepareExecute == this.NoPrepareExecute
				&& other.Legacy == this.Legacy
				&& other.Namespace == this.Namespace;
		}
	}
}
