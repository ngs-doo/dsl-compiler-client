using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml.Linq;

namespace DSLPlatform
{
	internal class CompileTargets
	{
		private static List<LibraryInfo.Nuget> NoDependencies()
		{
			return new List<LibraryInfo.Nuget>();
		}
		private static List<LibraryInfo.Nuget> RevenjStandard()
		{
			return new List<LibraryInfo.Nuget>(new[] { new LibraryInfo.Nuget { Project = "revenj", Version = "1.6.1" } });
		}
		private static List<LibraryInfo.Nuget> RevenjClient()
		{
			return new List<LibraryInfo.Nuget>(new[] { new LibraryInfo.Nuget { Project = "revenj.client", Version = "0.9.0" } });
		}

		private static readonly string[] PocoDependencies = new[] {
			typeof(string).Assembly.Location,
			typeof(System.Uri).Assembly.Location,
			typeof(System.Linq.Enumerable).Assembly.Location,
			typeof(System.Drawing.Color).Assembly.Location,
			typeof(System.Xml.Linq.XElement).Assembly.Location,
			typeof(System.Xml.XmlDocument).Assembly.Location,
			typeof(System.Runtime.Serialization.DataContractAttribute).Assembly.Location,
			typeof(System.Data.DataTable).Assembly.Location,
			typeof(System.ComponentModel.Composition.Primitives.Export).Assembly.Location,
			typeof(Microsoft.CSharp.RuntimeBinder.RuntimeBinderException).Assembly.Location
		};

		private static readonly string[] WpfDependencies = new[] {
			typeof(string).Assembly.Location,
			typeof(System.Uri).Assembly.Location,
			typeof(System.Linq.Enumerable).Assembly.Location,
			typeof(System.Drawing.Color).Assembly.Location,
			typeof(System.Xml.Linq.XElement).Assembly.Location,
			typeof(System.Xml.XmlDocument).Assembly.Location,
			typeof(System.Runtime.Serialization.DataContractAttribute).Assembly.Location,
			typeof(System.Data.DataTable).Assembly.Location,
			typeof(System.ComponentModel.Composition.Primitives.Export).Assembly.Location,
			typeof(Microsoft.CSharp.RuntimeBinder.RuntimeBinderException).Assembly.Location,
			typeof(System.Windows.Window).Assembly.Location,
			typeof(System.Xaml.XamlReader).Assembly.Location,
			typeof(System.Windows.DependencyProperty).Assembly.Location,
			typeof(System.Windows.UIElement).Assembly.Location
		};

		private static readonly string[] RevenjDependencies = new[] {
			typeof(string).Assembly.Location,
			typeof(System.Uri).Assembly.Location,
			typeof(System.Data.DataTable).Assembly.Location,
			typeof(System.Configuration.ConfigurationManager).Assembly.Location,
			typeof(System.Linq.Enumerable).Assembly.Location,
			typeof(System.Drawing.Color).Assembly.Location,
			typeof(System.Xml.XmlDocument).Assembly.Location,
			typeof(System.Xml.Linq.XElement).Assembly.Location,
			typeof(System.ComponentModel.Composition.Primitives.Export).Assembly.Location,
			typeof(System.Runtime.Serialization.DataContractAttribute).Assembly.Location,
			typeof(Microsoft.CSharp.RuntimeBinder.RuntimeBinderException).Assembly.Location
		};

		private string dslCompiler;
		public string CompilerPath
		{
			get { return dslCompiler; }
			set
			{
				dslCompiler = value;
				Compiler.SetupServer(dslCompiler);
			}
		}

		private static Version RevenjServerVersion(LibraryInfo library)
		{
			Version version;
			var nuget = library.Nugets.Find(it => "revenj".Equals(it.Project, StringComparison.InvariantCultureIgnoreCase));
			if (library.BuildType == BuildTypes.DotNetStandard && nuget != null && Version.TryParse(nuget.Version, out version))
				return version;
			if (!library.DependenciesExists) return null;
			var file = Directory.GetFiles(library.DependenciesPath, "Revenj.Core.dll", SearchOption.TopDirectoryOnly);
			if (file == null || file.Length != 1) return null;
			var fvi = FileVersionInfo.GetVersionInfo(file[0]);
			return new Version(fvi.FileVersion);
		}
		private static Version NoVersion(LibraryInfo library)
		{
			return null;
		}

		private LibraryInfo oldPocoLibrary;
		public static readonly LibraryInfo PocoLibraryDefault =
			new LibraryInfo(
				"Poco",
				"dotnet_poco",
				_ => null,
				false,
				PocoDependencies,
				NoDependencies(),
				BuildTypes.DotNetStandard,
				".cs",
				BuildTypes.LegacyDotNet, BuildTypes.DotNetStandard);
		public readonly LibraryInfo PocoLibrary = PocoLibraryDefault.Clone();
		private LibraryInfo oldClientLibrary;
		public static readonly LibraryInfo ClientLibraryDefault =
			new LibraryInfo(
				"Client",
				"dotnet_client",
				NoVersion,
				true,
				PocoDependencies,
				RevenjClient(),
				BuildTypes.DotNetStandard,
				".cs",
				BuildTypes.LegacyDotNet, BuildTypes.DotNetStandard);
		public readonly LibraryInfo ClientLibrary = ClientLibraryDefault.Clone();
		private LibraryInfo oldPortableLibrary;
		public static readonly LibraryInfo PortableLibraryDefault =
			new LibraryInfo(
				"Portable",
				"dotnet_portable",
				NoVersion,
				true,
				new string[0],
				NoDependencies(),
				BuildTypes.LegacyDotNet,
				".cs");
		public readonly LibraryInfo PortableLibrary = PortableLibraryDefault.Clone();
		private LibraryInfo oldPhpSource;
		public static readonly LibraryInfo PhpSourceDefault =
			new LibraryInfo(
				"Php",
				"php_client",
				NoVersion,
				false,
				new string[0],
				NoDependencies(),
				BuildTypes.Source,
				".php");
		public readonly LibraryInfo PhpSource = PhpSourceDefault.Clone();
		private LibraryInfo oldTypescriptSource;
		public static readonly LibraryInfo TypescriptSourceDefault =
			new LibraryInfo(
				"Typescript",
				"typescript",
				NoVersion,
				false,
				new string[0],
				NoDependencies(),
				BuildTypes.Source,
				string.Empty);
		public readonly LibraryInfo TypescriptSource = TypescriptSourceDefault.Clone();
		private LibraryInfo oldWpfLibrary;
		public static readonly LibraryInfo WpfLibraryDefault =
			new LibraryInfo(
				"Wpf",
				"wpf",
				NoVersion,
				true,
				WpfDependencies,
				NoDependencies(),
				BuildTypes.LegacyDotNet,
				".cs");
		public readonly LibraryInfo WpfLibrary = WpfLibraryDefault.Clone();
		private LibraryInfo oldPostgresLibrary;
		public static readonly LibraryInfo PostgresLibraryDefault =
			new LibraryInfo(
				"Postgres",
				"dotnet_server_postgres",
				RevenjServerVersion,
				true,
				RevenjDependencies,
				RevenjStandard(),
				BuildTypes.LegacyDotNet,
				".cs",
				BuildTypes.LegacyDotNet, BuildTypes.DotNetStandard);
		public readonly LibraryInfo PostgresLibrary = PostgresLibraryDefault.Clone();
		private LibraryInfo oldOracleLibrary;
		public static readonly LibraryInfo OracleLibraryDefault =
			new LibraryInfo(
				"Oracle",
				"dotnet_server_oracle",
				RevenjServerVersion,
				true,
				RevenjDependencies,
				NoDependencies(),
				BuildTypes.LegacyDotNet,
				".cs");
		public readonly LibraryInfo OracleLibrary = OracleLibraryDefault.Clone();

		private readonly LibraryInfo[] Targets;

		public CompileTargets()
		{
			Targets = new[] { PocoLibrary, ClientLibrary, PortableLibrary, PhpSource, TypescriptSource, WpfLibrary, PostgresLibrary, OracleLibrary };
		}

		public bool HasChanges()
		{
			return !PocoLibrary.Equals(oldPocoLibrary)
				|| !ClientLibrary.Equals(oldClientLibrary)
				|| !PortableLibrary.Equals(oldPortableLibrary)
				|| !PhpSource.Equals(oldPhpSource)
				|| !TypescriptSource.Equals(oldTypescriptSource)
				|| !WpfLibrary.Equals(oldWpfLibrary)
				|| !PostgresLibrary.Equals(oldPostgresLibrary)
				|| !OracleLibrary.Equals(oldOracleLibrary);
		}

		public void Reset(string compilerPath)
		{
			CompilerPath = compilerPath;
			oldPocoLibrary = PocoLibrary.Clone();
			oldClientLibrary = ClientLibrary.Clone();
			oldPortableLibrary = PortableLibrary.Clone();
			oldPhpSource = PhpSource.Clone();
			oldTypescriptSource = TypescriptSource.Clone();
			oldWpfLibrary = WpfLibrary.Clone();
			oldPostgresLibrary = PostgresLibrary.Clone();
			oldOracleLibrary = OracleLibrary.Clone();
		}

		public Either<LibraryInfo> ChooseLibrary(string info)
		{
			foreach (var t in Targets)
				if (t.Type == info)
					return Either.Success(t);
			return Either<LibraryInfo>.Fail("Unknown library: " + info);
		}

		private Either<Dictionary<string, string>> RunCompiler(string dslCompiler, LibraryInfo target, List<string> dsls)
		{
			var sb = new StringBuilder();
			sb.Append("target=").Append(target.CompilerName);
			if (target.WithActiveRecord)
				sb.Append(" settings=active-record");
			if (!target.WithHelperMethods)
				sb.Append(" settings=no-helpers");
			if (target.WithManualJson)
				sb.Append(" settings=manual-json");
			if (target.UseUtc)
				sb.Append(" settings=utc");
			if (target.MinimalSerialization)
				sb.Append(" settings=minimal-serialization");
			if (target.NoPrepareExecute)
				sb.Append(" settings=no-prepare-execute");
			if (target.Legacy)
				sb.Append(" settings=legacy");
			if (target.MutableSnowflake)
				sb.Append(" settings=mutable-snowflake");
			if (!string.IsNullOrWhiteSpace(target.Namespace))
				sb.Append(" namespace=").Append(target.Namespace);
			if (string.IsNullOrEmpty(target.Extension))
				sb.Append(" file-extension");
			var version = target.Version();
			if (version != null)
				sb.Append(" library=" + version.ToString());
			sb.Append(" format=xml");
			var result = Compiler.CompileDsl(sb, dsls, null, cms => XElement.Load(cms));
			if (result.Success)
			{
				var dict =
					(from x in result.Value.Elements()
					 let elem = x.Elements()
					 select new { key = elem.First().Value, value = elem.Last().Value })
					 .ToDictionary(it => it.key, it => it.value);
				return Either.Success(dict);
			}
			var error = string.IsNullOrEmpty(result.Error)
				? "Unknown error"
				: result.Error;
			return Either<Dictionary<string, string>>.Fail(error);
		}

		public Either<List<string>> Compile(List<string> dsls)
		{
			var compiled = new List<string>();
			if (!File.Exists(CompilerPath))
				return Either<List<string>>.Fail("Unable to find DSL Platform compiler: " + CompilerPath);
			foreach (var t in Targets)
			{
				if (t.Compile)
				{
					if (!t.TargetExists)
						return Either<List<string>>.Fail("Could not find " + t.Type + " target folder: " + t.Target);
					if (t.RequireDependenciesLegacy && !t.DependenciesExists && t.BuildType == BuildTypes.LegacyDotNet)
						return Either<List<string>>.Fail("Could not find " + t.Type + " dependencies folder: " + t.Dependencies);
					var result = RunCompiler(CompilerPath, t, dsls);
					if (!result.Success)
						return Either<List<string>>.Fail(result.Error);
					ProcessResult(t, result.Value, false);
					compiled.Add(t.Type);
				}
			}
			return Either.Success(compiled);
		}

		public void CompileAll(Dictionary<string, string> files)
		{
			ProcessResult(PocoLibrary, files, true);
			ProcessResult(ClientLibrary, files, true);
			ProcessResult(PortableLibrary, files, true);
			ProcessResult(WpfLibrary, files, true);
			ProcessResult(PhpSource, files, true);
			ProcessResult(TypescriptSource, files, true);
			ProcessResult(PostgresLibrary, files, true);
			ProcessResult(OracleLibrary, files, true);
		}

		private void CopySource(LibraryInfo info, Dictionary<string, string> files, bool filter)
		{
			if (!info.Compile) return;
			var sources =
				(from f in files
				 let name = filter ? f.Key.Substring(1) : f.Key + info.Extension
				 select new { Key = name, f.Value })
				.ToDictionary(it => it.Key, it => it.Value);
			DumpToDisk(info.TargetPath, sources, info.Extension, 3);
		}

		private static readonly char[] InvalidFileChars = Path.GetInvalidFileNameChars().Except(new[] { '/' }).ToArray();

		private static void DumpToDisk(string folder, Dictionary<string, string> files, string extension, int retries)
		{
			try
			{
				foreach (var f in Directory.GetFiles(folder))
					if (f.EndsWith(extension))
						File.Delete(f);
				foreach (var d in Directory.GetDirectories(folder))
					Directory.Delete(d, true);
				foreach (var kv in files)
				{
					var name = kv.Key;
					if (name.IndexOfAny(InvalidFileChars) != -1)
					{
						foreach (var ch in InvalidFileChars)
							name = name.Replace(ch, '_');
					}
					var fp = Path.Combine(folder, name);
					var dir = Path.GetDirectoryName(fp);
					if (!Directory.Exists(dir))
						Directory.CreateDirectory(dir);
					File.WriteAllText(fp, kv.Value);
				}
			}
			catch
			{
				if (retries > 0)
					DumpToDisk(folder, files, extension, retries - 1);
				else
					throw;
			}
		}

		private void ProcessResult(LibraryInfo info, Dictionary<string, string> files, bool filter)
		{
			if (info.BuildType == BuildTypes.Source)
			{
				CopySource(info, files, filter);
				return;
			}
			var target = Path.Combine(info.TargetPath, info.Name + ".dll");
			if (info.BuildType == BuildTypes.LegacyDotNet)
			{
				var references =
					info.ReferencesLegacy
					.Concat(info.DependenciesExists ? Directory.GetFiles(info.DependenciesPath, "*.dll") : new string[0])
					.Except(info.DependenciesExists ? new[] { Path.Combine(info.DependenciesPath, info.Name + ".dll") } : new string[0]);
				var sources = files.Select(it => it.Value).ToArray();
				Compiler.GenerateAssembly(target, sources, references);
			}
			else
			{
				var depLen = info.DependenciesPath.Length - info.Dependencies.Length;
				var references =
					(info.DependenciesExists ? Directory.GetFiles(info.DependenciesPath, "*.dll").Select(it => it.Substring(depLen)) : new string[0])
					.Except(info.DependenciesExists ? new[] { Path.Combine(info.Dependencies, info.Name + ".dll") } : new string[0]);
				var file = Compiler.BuildDotnet(info, references.ToArray(), files);
				File.Copy(file, target, true);
			}
		}
	}
}
