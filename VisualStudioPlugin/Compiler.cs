using System;
using System.CodeDom.Compiler;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;
using System.Threading;
using Microsoft.CSharp;

namespace DDDLanguage
{
	internal static class Compiler
	{
		private static string DslCompiler;
		private static Process RunningServer;
		private static int ServerPort;
		private static object sync = new object();
		private static DateTime StartedOn;

		static Compiler()
		{
			AppDomain.CurrentDomain.DomainUnload += (s, ea) => Stop(false);
			AppDomain.CurrentDomain.ProcessExit += (s, ea) => Stop(false);
		}

		public static void Stop(bool restart)
		{
			lock (sync)
			{
				if (RunningServer != null)
				{
					try { RunningServer.Kill(); }
					catch { }
					try { RunningServer.Close(); }
					catch { }
					RunningServer = null;
				}
				if (restart)
					SetupServer(DslCompiler);
			}
		}

		private static readonly Random Rnd = new Random();

		public static void SetupServer(string dslCompiler)
		{
			if (!File.Exists(dslCompiler))
				return;
			DslCompiler = dslCompiler;
			lock (sync)
			{
				if (RunningServer == null)
				{
					ServerPort = Rnd.Next(20000, 60000);
					RunningServer =
						new System.Diagnostics.Process
						{
							StartInfo = new ProcessStartInfo(DslCompiler, "server-mode port=" + ServerPort + " parent=" + Process.GetCurrentProcess().Id)
							{
								CreateNoWindow = true,
								WindowStyle = ProcessWindowStyle.Hidden,
								RedirectStandardOutput = false,
								UseShellExecute = false,
								WorkingDirectory = LibraryInfo.BasePath,
							}
						};
					RunningServer.Start();
					StartedOn = DateTime.Now;
				}
			}
		}

		//TODO: reuse TcpClient (keep-alive)
		public static TcpClient ConnectToServer(bool reset)
		{
			if (reset)
			{
				Stop(true);
				return null;
			}
			var tcp = new TcpClient(Socket.OSSupportsIPv6 ? AddressFamily.InterNetworkV6 : AddressFamily.InterNetwork);
			tcp.Connect(Socket.OSSupportsIPv6 ? IPAddress.IPv6Loopback : IPAddress.Loopback, ServerPort);
			if (!tcp.Connected)
				return null;
			tcp.Client.Blocking = true;
			tcp.SendTimeout = 3000;
			tcp.ReceiveTimeout = 30000;
			return tcp;
		}

		public static string CompilerPath
		{
			get
			{
				var path = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "DSLPlatform");
				if (!Directory.Exists(path))
					Directory.CreateDirectory(path);
				return path;
			}
		}

		public static string TempPath
		{
			get
			{
				var path = Path.Combine(Path.GetTempPath(), "DSLPlatform");
				if (!Directory.Exists(path))
					Directory.CreateDirectory(path);
				return path;
			}
		}

		private static readonly char[] InvalidFileChars = Path.GetInvalidFileNameChars().Except(new[] { '/' }).ToArray();

		public static string BuildDotnet(LibraryInfo library, string[] customDlls, Dictionary<string, string> files)
		{
			var folder = Path.Combine(TempPath, library.Type);
			if (!Directory.Exists(folder))
				Directory.CreateDirectory(folder);
			else
			{
				foreach (var f in Directory.GetFiles(folder, "*.cs"))
					File.Delete(f);
			}
			var outputName = library.Name.IndexOf('/') == -1 ? library.Name : library.Name.Substring(library.Name.LastIndexOf('/'));
			var csproj = new StringBuilder(@"<Project Sdk=""Microsoft.NET.Sdk"">
  <PropertyGroup>
	<TargetFramework>netstandard2.0</TargetFramework>
	<GenerateAssemblyInfo>false</GenerateAssemblyInfo>
	<AssemblyName>");
			csproj.Append(outputName);
			csproj.Append(@"</AssemblyName>
	<AppendTargetFrameworkToOutputPath>false</AppendTargetFrameworkToOutputPath>
  </PropertyGroup>");
			if (library.Nugets.Count != 0 || customDlls.Length != 0)
			{
				csproj.Append(@"  
  <ItemGroup>");
				foreach (var n in library.Nugets)
				{
					csproj.AppendFormat(@"
	<PackageReference Include=""{0}"" Version=""{1}"" />", n.Project, n.Version);
				}
				foreach (var dll in customDlls)
				{
					var fullPath = Path.Combine(LibraryInfo.BasePath, dll);
					csproj.AppendFormat(@"
	<Reference Include=""{0}"">
		<HintPath>{1}</HintPath>
	</Reference>", Path.GetFileNameWithoutExtension(dll), fullPath);
				}
				csproj.Append(@"  
  </ItemGroup>
");
			}
			csproj.Append(@"
</Project>");
			File.WriteAllText(Path.Combine(folder, library.Type + ".csproj"), csproj.ToString());
			foreach (var kv in files)
			{
				var name = kv.Key;
				if (name.IndexOfAny(InvalidFileChars) != -1)
				{
					foreach (var ch in InvalidFileChars)
						name = name.Replace(ch, '_');
				}
				File.WriteAllText(Path.Combine(folder, name + ".cs"), kv.Value);
			}
			var si = new ProcessStartInfo
			{
				FileName = "dotnet",
				WorkingDirectory = folder,
				Arguments = "build -c Release",
				UseShellExecute = false
			};
			si.EnvironmentVariables["DOTNET_CLI_TELEMETRY_OPTOUT"] = "1";
			var process = Process.Start(si);
			try
			{
				process.WaitForExit();
				if (process.ExitCode == 0)
					return Path.Combine(Path.Combine(Path.Combine(folder, "bin"), "Release"), outputName + ".dll");
				Process.Start(folder);
				throw new ApplicationException(@"Error during compilation. Try running the project manually");
			}
			finally
			{
				process.Dispose();
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

		private static readonly ThreadLocal<ChunkedMemoryStream> CMS = new ThreadLocal<ChunkedMemoryStream>(() => new ChunkedMemoryStream());
		private static readonly ThreadLocal<byte[]> Buffer = new ThreadLocal<byte[]>(() => new byte[8196]);

		public static Either<T> CompileDsl<T>(StringBuilder sb, List<string> dsls, string dsl, Func<ChunkedMemoryStream, T> extract)
		{
			TcpClient tcp = null;
			try
			{
				if (dsls != null)
				{
					foreach (var d in dsls)
					{
						var path = d.StartsWith(LibraryInfo.BasePath) ? d.Substring(LibraryInfo.BasePath.Length) : d;
						sb.Append(" \"dsl=").Append(path).Append('"');
					}
					sb.Append(" \"path=");
					sb.Append(LibraryInfo.BasePath);
					sb.Append('"');
				}
				try
				{
					tcp = ConnectToServer(false);
					if (tcp == null)
						tcp = ConnectToServer(true);
				}
				catch (SocketException)
				{
					if (DateTime.Now < StartedOn.AddMinutes(1))
						Thread.Sleep(1000);
					CloseTcp(tcp);
					tcp = ConnectToServer(false);
				}
				if (tcp == null)
				{
					if (dsls != null)
						return CompileDsl<T>(sb.ToString(), extract);
					return Either<T>.Fail("Unable to start DSL Platform compiler");
				}
				return SendRequest<T>(sb.ToString(), dsl, extract, tcp);
			}
			catch (Exception ex)
			{
				if (DateTime.Now < StartedOn.AddMinutes(1))
					Stop(true);
				if (dsls != null)
					return CompileDsl<T>(sb.ToString(), extract);
				return Either<T>.Fail(ex.Message);
			}
			finally { CloseTcp(tcp); }
		}

		public static Either<T> Load<T>(string query, Func<ChunkedMemoryStream, T> extract)
		{
			TcpClient tcp = null;
			try
			{
				tcp = ConnectToServer(false);
				if (tcp == null)
					tcp = ConnectToServer(true);
				if (tcp == null)
					return Either<T>.Fail("Unable to start DSL Platform compiler");
				return SendRequest(query, null, extract, tcp);
			}
			catch (Exception ex)
			{
				return Either<T>.Fail(ex.Message);
			}
			finally { CloseTcp(tcp); }
		}

		private static void CloseTcp(TcpClient tcp)
		{
			if (tcp != null)
			{
				try { tcp.Close(); }
				catch { }
			}
		}

		private static Either<T> SendRequest<T>(string arguments, string dsl, Func<ChunkedMemoryStream, T> extract, TcpClient tcp)
		{
			var cms = CMS.Value;
			var buf = Buffer.Value;
			tcp.Client.Send(Encoding.UTF8.GetBytes(arguments + " include-length\n"));
			if (dsl != null)
				tcp.Client.Send(Encoding.UTF8.GetBytes(dsl));
			cms.SetLength(0);
			var read = tcp.Client.Receive(buf, 4, SocketFlags.None);
			var succes = read == 4 && buf[0] == 'O';
			read = tcp.Client.Receive(buf, 4, SocketFlags.None);
			if (read != 4)
				return Either<T>.Fail("Invalid response from server. Expecting length.");
			var length = (buf[0] << 24) + (buf[1] << 16) + (buf[2] << 8) + buf[3];
			while (length > 0 && (read = tcp.Client.Receive(buf)) > 0)
			{
				length -= read;
				cms.Write(buf, 0, read);
			}
			cms.Position = 0;
			if (succes)
				return Either.Success(extract(cms));
			return Either<T>.Fail(new StreamReader(cms).ReadToEnd());
		}

		private static Either<T> CompileDsl<T>(string args, Func<ChunkedMemoryStream, T> extract)
		{
			var process =
				new System.Diagnostics.Process
				{
					StartInfo = new ProcessStartInfo(DslCompiler, args)
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
				return Either.Success(extract(cms));
			else
				return Either<T>.Fail(new StreamReader(cms).ReadToEnd());
		}
	}
}
