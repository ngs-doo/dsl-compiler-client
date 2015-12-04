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

		static Compiler()
		{
			AppDomain.CurrentDomain.DomainUnload += (s, ea) => Stop();
			AppDomain.CurrentDomain.ProcessExit += (s, ea) => Stop();
		}

		public static void Stop()
		{
			if (RunningServer != null)
			{
				try { RunningServer.Kill(); }
				catch { }
				try { RunningServer.Close(); }
				catch { }
				RunningServer = null;
			}
		}

		private static readonly Random Rnd = new Random();

		public static void SetupServer(string dslCompiler)
		{
			if (!File.Exists(dslCompiler))
				return;
			DslCompiler = dslCompiler;
			if (RunningServer == null)
			{
				ServerPort = Rnd.Next(12000, 25000);
				RunningServer =
					new System.Diagnostics.Process
					{
						StartInfo = new ProcessStartInfo(DslCompiler, "server-mode " + ServerPort)
						{
							CreateNoWindow = true,
							WindowStyle = ProcessWindowStyle.Hidden,
							RedirectStandardOutput = true,
							UseShellExecute = false,
							WorkingDirectory = LibraryInfo.BasePath,
						}
					};
				RunningServer.Start();
			}
		}

		public static TcpClient ConnectToServer(bool reset)
		{
			if (reset)
			{
				Stop();
				SetupServer(DslCompiler);
				Thread.Sleep(2000);
			}
			var tcp = new TcpClient(AddressFamily.InterNetworkV6);
			tcp.Connect(IPAddress.IPv6Loopback, ServerPort);
			if (!tcp.Connected)
				return null;
			tcp.Client.Blocking = true;
			tcp.SendTimeout = 30000;
			tcp.ReceiveTimeout = 30000;
			return tcp;
		}
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
				}
				sb.Append(" \"path=");
				sb.Append(LibraryInfo.BasePath);
				sb.Append('"');
				tcp = ConnectToServer(false);
				var cms = CMS.Value;
				var buf = Buffer.Value;
				if (tcp == null)
					tcp = ConnectToServer(true);
				if (tcp == null)
					return Either<T>.Fail("Unable to start DSL Platform compiler");
				sb.Append("\n");
				tcp.Client.Send(Encoding.UTF8.GetBytes(sb.ToString()));
				if (dsl != null)
					tcp.Client.Send(Encoding.UTF8.GetBytes(dsl));
				cms.SetLength(0);
				var read = tcp.Client.Receive(buf, 4, SocketFlags.None);
				var succes = read == 4 && buf[0] == 'O';
				while ((read = tcp.Client.Receive(buf)) > 0)
					cms.Write(buf, 0, read);
				cms.Position = 0;
				if (succes)
					return Either.Success(extract(cms));
				return Either<T>.Fail(new StreamReader(cms).ReadToEnd());
			}
			catch (Exception ex)
			{
				Stop();
				return Either<T>.Fail(ex.Message);
			}
			finally
			{
				if (tcp != null)
				{
					try { tcp.Close(); }
					catch { }
				}
			}
		}
	}
}
