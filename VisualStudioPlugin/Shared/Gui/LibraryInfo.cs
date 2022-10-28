using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Windows;
using System.Windows.Media;

namespace DSLPlatform
{
	internal class LibraryInfo : IEquatable<LibraryInfo>, ICloneable
	{
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
		public readonly bool RequireDependenciesLegacy;
		public string Extension { get; private set; }
		public string[] ReferencesLegacy { get; private set; }
		public List<Nuget> Nugets { get; set; }

		public class Nuget
		{
			public string Project { get; set; }
			public string Version { get; set; }
			public Nuget Clone()
			{
				return new Nuget { Project = Project, Version = Version };
			}

			public static bool Equal(List<Nuget> left, List<Nuget> right)
			{
				if (left.Count != right.Count) return false;
				return left.Zip(right, (l, r) => new { l, r }).All(kv => kv.l.Project == kv.r.Project && kv.l.Version == kv.r.Version);
			}
		}

		public static string BasePath { get; set; }
		public readonly BuildTypes[] SupportedBuilds;
		private readonly Func<LibraryInfo, Version> DependenciesVersion;
		public Version Version() { return DependenciesVersion(this); }

		public LibraryInfo(
			string type,
			string compilerName,
			Func<LibraryInfo, Version> dependenciesVersion,
			bool requireDependenciesLegacy,
			string[] referencesLegacy,
			List<Nuget> nugets,
			BuildTypes buildType,
			string extension,
			params BuildTypes[] supportedBuilds)
		{
			Type = type;
			this.SupportedBuilds = supportedBuilds;
			this.DependenciesVersion = dependenciesVersion;
			this.RequireDependenciesLegacy = requireDependenciesLegacy;
			if (buildType == BuildTypes.Source)
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
			ReferencesLegacy = referencesLegacy;
			Nugets = nugets;
			Extension = extension;
			this.BuildType = buildType;
			Dependencies = Path.Combine("dependencies", type);
			WithActiveRecord = WithHelperMethods = true;
		}

		public string TargetPath { get { return Path.Combine(BasePath, Target); } }
		public BuildTypes BuildType { get; set; }
		public Visibility BuildVisibility { get { return SupportedBuilds != null && SupportedBuilds.Length > 1 ? Visibility.Visible : Visibility.Collapsed; } }
		public Visibility DllVisibility { get { return BuildType != BuildTypes.Source ? Visibility.Visible : Visibility.Collapsed; } }
		public Visibility LegacyVisibility { get { return BuildType == BuildTypes.LegacyDotNet ? Visibility.Visible : Visibility.Collapsed; } }
		public Visibility NetStandardVisibility { get { return BuildType == BuildTypes.DotNetStandard ? Visibility.Visible : Visibility.Collapsed; } }
		public string DependenciesPath { get { return Path.Combine(BasePath, Dependencies); } }
		public bool TargetExists { get { return PathExists(Target); } }
		public bool DependenciesExists { get { return PathExists(Dependencies); } }

		public bool WithActiveRecord { get; set; }
		public bool WithHelperMethods { get; set; }
		public bool WithManualJson { get; set; }
		public bool UseUtc { get; set; }
		public bool MinimalSerialization { get; set; }
		public bool NoPrepareExecute { get; set; }
		public bool MutableSnowflake { get; set; }
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
				if (BuildType == BuildTypes.Source)
				{
					if (TargetExists)
						return "Source can be created. After compilation source will be placed to " + Target;
					return @"Target path not found. 
Please create or specify target path where generated sources will be placed.";
				}
				var hasFiles = DependenciesExists
					&& Directory.EnumerateFiles(DependenciesPath, "*.dll", SearchOption.TopDirectoryOnly).Any();
				if (TargetExists && BuildType == BuildTypes.DotNetStandard)
					return
						"Library can be used. After compilation DLL will be copied to "
						+ Target + " as " + Name + ".dll";
				if (TargetExists && BuildType == BuildTypes.LegacyDotNet && !RequireDependenciesLegacy && string.IsNullOrEmpty(Dependencies))
					return
						"Library can be used. After compilation DLL will be copied to "
						+ Target + " as " + Name + ".dll" + Environment.NewLine
						+ "Dependency folder not specified";
				if (TargetExists && BuildType == BuildTypes.LegacyDotNet && !RequireDependenciesLegacy && DependenciesExists)
					return
						"Library can be used. After compilation DLL will be copied to "
						+ Target + " as " + Name + ".dll" + Environment.NewLine
						+ "Dependency folder found in " + Dependencies;
				if (TargetExists && BuildType == BuildTypes.LegacyDotNet && DependenciesExists && hasFiles)
					return
						"Library can be used. After compilation DLL will be copied to "
						+ Target + " as " + Name + ".dll" + Environment.NewLine
						+ "Dependencies can be found in " + Dependencies + " and should be referenced from project.";
				return
					(string.IsNullOrWhiteSpace(Name) ? @"DLL name not specified.
Please specify DLL for compiled library from DSL model." + Environment.NewLine : string.Empty)
					+ (TargetExists ? string.Empty : @"Target path not found. 
Please create or specify target path from where compiled DLL will be referenced." + Environment.NewLine)
					+ (DependenciesExists || BuildType == BuildTypes.DotNetStandard ? string.Empty : @"Dependency path not found. 
Please create or specify dependency path for library and reference it from project.")
					+ ((DependenciesExists || BuildType == BuildTypes.DotNetStandard) && !hasFiles ? "Dependencies not found if dependency folder: " + Dependencies + @"
Please download dependencies before running compilation" : string.Empty);
			}
		}

		public bool CanCompile
		{
			get
			{
				if (BuildType == BuildTypes.Source || BuildType == BuildTypes.DotNetStandard)
					return TargetExists;
				return !string.IsNullOrWhiteSpace(Name)
					&& TargetExists
					&& (!RequireDependenciesLegacy
						|| DependenciesExists && Directory.EnumerateFiles(DependenciesPath, "*.dll", SearchOption.TopDirectoryOnly).Any());
			}
		}

		public LibraryInfo Clone()
		{
			return new LibraryInfo(Type, CompilerName, DependenciesVersion, RequireDependenciesLegacy, ReferencesLegacy, Nugets.Select(it => it.Clone()).ToList(), BuildType, Extension, SupportedBuilds)
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
				MutableSnowflake = MutableSnowflake,
				Legacy = Legacy,
				Namespace = Namespace,
				BuildType = BuildType,
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
				&& other.MutableSnowflake == this.MutableSnowflake
				&& other.Legacy == this.Legacy
				&& other.Namespace == this.Namespace
				&& other.BuildType == this.BuildType
				&& Nuget.Equal(other.Nugets, this.Nugets);
		}
	}
}
