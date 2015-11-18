using System;
using System.CodeDom.Compiler;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;
using Microsoft.CSharp;

namespace DDDLanguage
{
	internal static class Compiler
	{
		public static string RootPath
		{
			get
			{
				var path = Path.Combine(Path.GetTempPath(), "DSLPlatform");
				if (!Directory.Exists(path))
					Directory.CreateDirectory(path);
				return path;
			}
		}

		public static string GenerateAssembly(
			string assemblyPath,
			string[] sources,
			IEnumerable<string> references,
			int retries = 3)
		{
			var folder = Path.GetDirectoryName(assemblyPath);
			if (!Directory.Exists(folder))
				Directory.CreateDirectory(folder);

			var parameters = new CompilerParameters();
			parameters.GenerateExecutable = false;
			foreach (var r in references)
				parameters.ReferencedAssemblies.Add(r);
			parameters.GenerateInMemory = false;
			parameters.OutputAssembly = assemblyPath;
			parameters.IncludeDebugInformation = false;

			if (sources.Length == 0)
			{
				AppDomain.CurrentDomain.DefineDynamicAssembly(
					new AssemblyName(Path.GetFileNameWithoutExtension(assemblyPath)),
					AssemblyBuilderAccess.RunAndSave);
				return parameters.OutputAssembly;
			}
			using (var provider = new CSharpCodeProvider())
			{
				CompilerResults result = null;
				do
				{
					try
					{
						result = provider.CompileAssemblyFromSource(parameters, sources);
					}
					catch
					{
						retries--;
						if (retries < 0)
							throw;
						ServerActions.DeleteFile(assemblyPath);
					}
				} while (result == null);
				if (result.Errors.HasErrors)
				{
					var errors =
						string.Join(
							Environment.NewLine,
							(from CompilerError err in result.Errors
							 where !err.IsWarning
							 select err.ErrorText).Take(5));
					throw new ApplicationException(@"Error during compilation:
" + errors);
				}
				return parameters.OutputAssembly;
			}
		}

		public static Either<ChunkedMemoryStream> CompileDsl(string dslCompiler, StringBuilder sb, List<string> dsls)
		{
			foreach (var d in dsls)
			{
				var path = d.StartsWith(LibraryInfo.BasePath) ? d.Substring(LibraryInfo.BasePath.Length) : d;
				sb.Append(" \"dsl=").Append(path).Append('"');
			}
			var process =
				new System.Diagnostics.Process
				{
					StartInfo = new ProcessStartInfo(dslCompiler, sb.ToString())
					{
						CreateNoWindow = true,
						WindowStyle = ProcessWindowStyle.Hidden,
						RedirectStandardOutput = true,
						UseShellExecute = false,
						WorkingDirectory = LibraryInfo.BasePath
					}
				};
			process.Start();
			var cms = new ChunkedMemoryStream();
			process.StandardOutput.BaseStream.CopyTo(cms);
			process.WaitForExit();
			cms.Position = 0;
			if (process.ExitCode == 0)
				return Either.Success(cms);
			else
				return Either<ChunkedMemoryStream>.Fail(new StreamReader(cms).ReadToEnd());
		}
	}
}
