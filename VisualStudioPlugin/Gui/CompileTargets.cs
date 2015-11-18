using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Xml.Linq;

namespace DDDLanguage
{
	internal class CompileTargets
	{
		public string CompilerPath { get; set; }

		private LibraryInfo oldPocoLibrary;
		public readonly LibraryInfo PocoLibrary;
		private LibraryInfo oldClientLibrary;
		public readonly LibraryInfo ClientLibrary;
		private LibraryInfo oldPortableLibrary;
		public readonly LibraryInfo PortableLibrary;
		private LibraryInfo oldPhpSource;
		public readonly LibraryInfo PhpSource;
		private LibraryInfo oldWpfLibrary;
		public readonly LibraryInfo WpfLibrary;
		private LibraryInfo oldPostgresLibrary;
		public readonly LibraryInfo PostgresLibrary;
		private LibraryInfo oldOracleLibrary;
		public readonly LibraryInfo OracleLibrary;

		private LibraryInfo[] Targets;

		public CompileTargets()
		{

			PocoLibrary = new LibraryInfo("Poco", "dotnet_poco", PocoDependencies);
			ClientLibrary = new LibraryInfo("Client", "dotnet_client", PocoDependencies);
			PortableLibrary = new LibraryInfo("Portable", "dotnet_portable", new string[0]);
			PhpSource = new LibraryInfo("Php", "php_client", new string[0], true, ".php");
			WpfLibrary = new LibraryInfo("Wpf", "wpf", WpfDependencies);
			PostgresLibrary = new LibraryInfo("Postgres", "dotnet_server_postgres", RevenjDependencies);
			OracleLibrary = new LibraryInfo("Oracle", "dotnet_server_oracle", RevenjDependencies);
			Targets = new[] { PocoLibrary, ClientLibrary, PortableLibrary, PhpSource, WpfLibrary, PostgresLibrary, OracleLibrary };
		}

		public bool HasChanges()
		{
			return !PocoLibrary.Equals(oldPocoLibrary)
				|| !ClientLibrary.Equals(oldClientLibrary)
				|| !PortableLibrary.Equals(oldPortableLibrary)
				|| !PhpSource.Equals(oldPhpSource)
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
			if (target.NoHelpers)
				sb.Append(" settings=no-helpers");
			var result = Compiler.CompileDsl(dslCompiler, sb, dsls);
			if (result.Success)
			{
				var xml = XElement.Load(result.Value);
				var dict =
					(from x in xml.Elements()
					 let elem = x.Elements()
					 select new { key = elem.First().Value, value = elem.Last().Value })
					 .ToDictionary(it => it.key, it => it.value);
				return Either.Success(dict);
			}
			else return Either<Dictionary<string, string>>.Fail(result.Error);
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
					if (!t.SourceOnly && !t.DependenciesExists)
						return Either<List<string>>.Fail("Could not find " + t.Type + " dependencies folder: " + t.Dependencies);
					var result = RunCompiler(CompilerPath, t, dsls);
					if (!result.Success)
						return Either<List<string>>.Fail(result.Error);
					if (t.SourceOnly)
						CopySource(t, result.Value, false);
					else
						Compile(t, result.Value, false);
					compiled.Add(t.Type);
				}
			}
			return Either.Success(compiled);
		}

		public void CompileAll(Dictionary<string, string> files)
		{
			Compile(PocoLibrary, files, true);
			Compile(ClientLibrary, files, true);
			Compile(PortableLibrary, files, true);
			Compile(WpfLibrary, files, true);
			CopySource(PhpSource, files, true);
			Compile(PostgresLibrary, files, true);
			Compile(OracleLibrary, files, true);
		}

		private void CopySource(LibraryInfo info, Dictionary<string, string> files, bool filter)
		{
			if (!info.Compile) return;
			var sources =
				(from f in files
				 let name = filter ? f.Key.Substring(1) : f.Key + info.Extension
				 select new { Key = name, f.Value })
				.ToDictionary(it => it.Key, it => it.Value);
			DumpToDisk(info.TargetPath, sources, 3);
		}

		private static readonly char[] InvalidFileChars = Path.GetInvalidFileNameChars();

		private static void DumpToDisk(string folder, Dictionary<string, string> files, int retries)
		{
			try
			{
				foreach (var f in Directory.GetFiles(folder))
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
					var fp = Path.Combine(folder, kv.Key);
					var dir = Path.GetDirectoryName(fp);
					if (!Directory.Exists(dir))
						Directory.CreateDirectory(dir);
					File.WriteAllText(fp, kv.Value);
				}
			}
			catch
			{
				if (retries > 0)
					DumpToDisk(folder, files, retries - 1);
				else
					throw;
			}
		}

		private void Compile(LibraryInfo info, Dictionary<string, string> files, bool filter)
		{
			if (!info.Compile) return;
			var references =
				info.References
				.Concat(Directory.GetFiles(info.DependenciesPath, "*.dll"))
				.Except(new[] { Path.Combine(info.DependenciesPath, info.Name + ".dll") });
			var sources = files.Select(it => it.Value).ToArray();
			var target = Path.Combine(info.TargetPath, info.Name + ".dll");
			var assembly = Compiler.GenerateAssembly(target, sources, references);
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
	}
}
