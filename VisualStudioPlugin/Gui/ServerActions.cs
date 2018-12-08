using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Text;
using System.Xml.Linq;
using EnvDTE;
using Ionic.Zip;
using Npgsql;
using Oracle.ManagedDataAccess.Client;

namespace DSLPlatform
{
	internal class ServerActions
	{
		private static readonly Dictionary<string, Func<string, Stream>> DownloadMethods =
			new Dictionary<string, Func<string, Stream>> {
				  {"dsl-platform", DownloadPlatform},
				  {"github-revenj", DownloadGithubRevenj}
		};

		private static Stream DownloadPlatform(string zip)
		{
			try
			{
				var request = (HttpWebRequest)HttpWebRequest.Create("https://tools.dsl-platform.com/" + zip);
				request.PreAuthenticate = true;
				request.KeepAlive = false;
				request.Method = "GET";
				var response = (HttpWebResponse)request.GetResponse();
				return response.GetResponseStream();
			}
			catch (WebException ex)
			{
				string message;
				try
				{
					var ct = ex.Response.Headers["Content-Type"] ?? string.Empty;
					if (ct.StartsWith("application/xml"))
						message = XElement.Load(ex.Response.GetResponseStream()).Value;
					else
						message = new StreamReader(ex.Response.GetResponseStream()).ReadToEnd();
				}
				catch
				{
					throw ex;
				}
				throw new Exception(message, ex);
			}
		}

		private static Stream DownloadGithubRevenj(string zip)
		{
			try
			{
				ServicePointManager.SecurityProtocol = SecurityProtocolType.Tls | SecurityProtocolType.Ssl3 | (SecurityProtocolType)3072;
				var latest = (HttpWebRequest)HttpWebRequest.Create("https://github.com/ngs-doo/revenj/releases/latest");
				latest.KeepAlive = false;
				latest.AllowAutoRedirect = false;
				var redirect = (HttpWebResponse)latest.GetResponse();
				var tag = "1.4.2";
				if (redirect.StatusCode == HttpStatusCode.Redirect)
				{
					var location = redirect.GetResponseHeader("Location");
					tag = location.Substring(location.LastIndexOf('/') + 1);
				}
				var request = (HttpWebRequest)HttpWebRequest.Create("https://github.com/ngs-doo/revenj/releases/download/" + tag + "/" + zip);
				request.KeepAlive = false;
				var response = (HttpWebResponse)request.GetResponse();
				return response.GetResponseStream();
			}
			catch (WebException ex)
			{
				string message;
				try
				{
					if (ex.Response == null)
					{
						System.Diagnostics.Process.Start("https://github.com/ngs-doo/revenj/releases/latest");
						throw new Exception("Unable to connect to Github. Please setup the project manually", ex);
					}
					var ct = ex.Response.Headers["Content-Type"] ?? string.Empty;
					if (ct.StartsWith("application/xml"))
						message = XElement.Load(ex.Response.GetResponseStream()).Value;
					else
						message = new StreamReader(ex.Response.GetResponseStream()).ReadToEnd();
					System.Diagnostics.Process.Start("https://github.com/ngs-doo/revenj/releases/latest");
				}
				catch
				{
					throw ex;
				}
				throw new Exception(message, ex);
			}
			catch
			{
				System.Diagnostics.Process.Start("https://github.com/ngs-doo/revenj/releases/latest");
				throw;
			}
		}

		private static Either<T> GetRemote<T>(string url, Func<string, Stream> download, Func<Stream, T> handleResult)
		{
			try
			{
				var stream = download(url);
				return Either.Success(handleResult(stream));
			}
			catch (Exception ex)
			{
				return Either<T>.Fail(ex.Message);
			}
		}

		public static Either<string> ExtractPostgresDsl(DatabaseInfo db)
		{
			return BuildZip(ReadInfoFromPostgres, db);
		}

		public static Either<string> ExtractOracleDsl(DatabaseInfo db)
		{
			return BuildZip(ReadInfoFromOracle, db);
		}

		private static Either<string> BuildZip(Func<DatabaseInfo, DbInfo> getInfo, DatabaseInfo info)
		{
			try
			{
				var db = getInfo(info);
				var path = Path.Combine(Compiler.TempPath, db.Target + ".zip");
				using (var zip = new ZipFile())
				{
					foreach (var file in db.Dsl)
						zip.AddEntry(file.Key, file.Value, Encoding.UTF8);
					zip.Save(path);
				}
				return Either.Success(path);
			}
			catch (Exception ex)
			{
				return Either<string>.Fail(ex.Message);
			}
		}

		public static Either<bool> DownloadZip(string fromWhere, string zipName, string path)
		{
			Func<string, Stream> download;
			if (fromWhere == null || !DownloadMethods.TryGetValue(fromWhere, out download))
				download = DownloadPlatform;
			return
				GetRemote(
					zipName,
					download,
					stream =>
					{
						var name = Path.GetTempFileName();
						using (var fs = new FileStream(name, FileMode.Create, FileAccess.Write))
							stream.CopyTo(fs);
						var zipFiles = new List<string>();
						using (var zf = new ZipFile(name))
						{
							foreach (var ze in zf.Entries)
							{
								zipFiles.Add(ze.FileName);
								ze.Extract(path, ExtractExistingFileAction.OverwriteSilently);
							}
						}
						DeleteFile(name);
						var diskFiles = Directory.GetFiles(path, "*.*", SearchOption.AllDirectories);
						return diskFiles.Length == zipFiles.Count;
					});
		}

		private static Dictionary<string, string> GatherCurrentDsl(DTE dte)
		{
			var dsls = new Dictionary<string, string>();
			foreach (Project p in dte.Solution.Projects)
			{
				if (p.Kind != Constants.vsProjectKindUnmodeled)
					GatherCurrentDsl(p.ProjectItems, dsls);
			}
			return dsls;
		}

		private static void GatherCurrentDsl(ProjectItems items, Dictionary<string, string> dsls)
		{
			foreach (ProjectItem pi in items)
			{
				if ((pi.Name.EndsWith(".dsl", StringComparison.InvariantCultureIgnoreCase)
					|| pi.Name.EndsWith(".ddd", StringComparison.InvariantCultureIgnoreCase))
					&& pi.FileNames[1] != null)
				{
					var path = pi.FileNames[1];
					var name = path.StartsWith(LibraryInfo.BasePath) ? path.Substring(LibraryInfo.BasePath.Length) : path;
					dsls.Add(name.Replace("\\", "/"), File.ReadAllText(path));
				}
				if (pi.ProjectItems != null)
					GatherCurrentDsl(pi.ProjectItems, dsls);
				var subproject = pi.SubProject;
				if (subproject != null && subproject.ProjectItems != null)
					GatherCurrentDsl(subproject.ProjectItems, dsls);
			}
		}

		private static List<string> LocateCurrentDsl(DTE dte)
		{
			var dsls = new List<string>();
			foreach (Project p in dte.Solution.Projects)
			{
				if (p.Kind != Constants.vsProjectKindUnmodeled)
					LocateCurrentDsl(p.ProjectItems, dsls);
			}
			return dsls;
		}

		private static void LocateCurrentDsl(ProjectItems items, List<string> dsls)
		{
			foreach (ProjectItem pi in items)
			{
				if ((pi.Name.EndsWith(".dsl", StringComparison.InvariantCultureIgnoreCase)
					|| pi.Name.EndsWith(".ddd", StringComparison.InvariantCultureIgnoreCase))
					&& pi.FileNames[1] != null)
				{
					dsls.Add(pi.FileNames[1]);
				}
				if (pi.ProjectItems != null)
					LocateCurrentDsl(pi.ProjectItems, dsls);
				var subproject = pi.SubProject;
				if (subproject != null && subproject.ProjectItems != null)
					LocateCurrentDsl(subproject.ProjectItems, dsls);
			}
		}

		private static Stream Serialize<T>(T value)
		{
			var ser = new DataContractSerializer(typeof(T));
			var ms = new ChunkedMemoryStream();
			ser.WriteObject(ms, value);
			ms.Position = 0;
			return ms;
		}

		private static T Deserialize<T>(Stream stream)
		{
			var ser = new DataContractSerializer(typeof(T));
			return (T)ser.ReadObject(stream);
		}

		public static Either<string> Parse(DTE dte)
		{
			try
			{
				var dsls = LocateCurrentDsl(dte);
				if (dsls.Count == 0) return Either.Success("Nothing to parse. No DSL files found.");
				var result = Compiler.CompileDsl(new StringBuilder(), dsls, null, _ => 0);
				if (result.Success)
					return Either.Success("Parse OK");
				return Either<string>.Fail(result.Error);
			}
			catch (Exception ex)
			{
				return Either<string>.Fail(ex.Message);
			}
		}

		public class DiffResult
		{
			public Dictionary<string, string> OldDsl;
			public Dictionary<string, string> NewDsl;
			public SchemaChange[] DbChanges;
		}

		private static Either<DiffResult> DiffDatabase(DTE dte, string dslCompiler, DatabaseInfo db, Func<DatabaseInfo, DbInfo> getInfo)
		{
			try
			{
				var dsls = GatherCurrentDsl(dte);
				if (dsls.Count == 0)
					return Either<DiffResult>.Fail("No DSL files found. Please check your solution for .dsl/.ddd files which are used for compilation.");
				var dbInfo = getInfo(db);
				if (!db.CompileMigration)
					return Either.Success(new DiffResult { OldDsl = dbInfo.Dsl, NewDsl = dsls });
				var files = LocateCurrentDsl(dte);
				if (files.Count == 0)
					return Either<DiffResult>.Fail("No DSL files found. Please check your solution for .dsl/.ddd files which are used for compilation.");
				var result = RunMigration(dbInfo, db, files);
				if (!result.Success)
					return Either<DiffResult>.Fail(result.Error);
				return Either.Success(ProcessDiffStream(result.Value, dbInfo, dsls));
			}
			catch (Exception ex)
			{
				return Either<DiffResult>.Fail(ex.Message);
			}
		}

		private static DiffResult ProcessDiffStream(Stream stream, DbInfo dbInfo, Dictionary<string, string> dsls)
		{
			var changes = ExtractChanges(stream);
			return new DiffResult
			{
				OldDsl = dbInfo.Dsl,
				NewDsl = dsls,
				DbChanges = changes
			};
		}

		private static SchemaChange[] ExtractChanges(Stream stream)
		{
			var sr = new StreamReader(stream, Encoding.UTF8);
			var changes = new List<SchemaChange>();
			string lastLine = sr.ReadLine();
			if (lastLine == "/*MIGRATION_DESCRIPTION")
			{
				var comments = new List<string>();
				do
				{
					lastLine = sr.ReadLine();
					if (lastLine.Length > 0)
						comments.Add(lastLine);
					else if (sr.EndOfStream)
						break;
				} while (lastLine != "MIGRATION_DESCRIPTION*/");
				if (comments.Count > 0)
					comments.RemoveAt(comments.Count - 1);
				for (int i = 1; i < comments.Count; i += 2)
				{
					var line = comments[i - 1];
					if (line.StartsWith("--REMOVE: "))
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Remove, Definition = line.Substring("--REMOVE: ".Length), Description = comments[i] });
					else if (line.StartsWith("--CREATE: "))
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Create, Definition = line.Substring("--CREATE: ".Length), Description = comments[i] });
					else if (line.StartsWith("--RENAME: "))
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Rename, Definition = line.Substring("--RENAME: ".Length), Description = comments[i] });
					else if (line.StartsWith("--MOVE: "))
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Move, Definition = line.Substring("--MOVE: ".Length), Description = comments[i] });
					else if (line.StartsWith("--COPY: "))
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Copy, Definition = line.Substring("--COPY: ".Length), Description = comments[i] });
					else
						changes.Add(new SchemaChange { Type = SchemaChange.ChangeType.Unknown, Definition = line, Description = comments[i] });
				}
			}
			return changes.ToArray();
		}

		public static Either<DiffResult> PostgresDiff(DTE dte, string dslCompiler, DatabaseInfo postgresDb)
		{
			return DiffDatabase(dte, dslCompiler, postgresDb, ReadInfoFromPostgres);
		}

		public static Either<DiffResult> OracleDiff(DTE dte, string dslCompiler, DatabaseInfo oracleDb)
		{
			return DiffDatabase(dte, dslCompiler, oracleDb, ReadInfoFromOracle);
		}

		public static Either<string> Compile(
			DTE dte,
			CompileTargets targets,
			DatabaseInfo postgresDb,
			DatabaseInfo oracleDb,
			bool confirmedPostgres,
			bool confirmedOracle)
		{
			try
			{
				var dsls = LocateCurrentDsl(dte);
				if (dsls.Count == 0)
					return Either<string>.Fail("No DSL files found. Please check your solution for .dsl/.ddd files which are used for compilation.");
				var compilation = targets.Compile(dsls);
				if (!compilation.Success)
					return Either<string>.Fail(compilation.Error);
				if (postgresDb.CompileMigration)
				{
					var dbInfo = ReadInfoFromPostgres(postgresDb);
					var result = RunMigration(dbInfo, postgresDb, dsls);
					if (!result.Success)
						return Either<string>.Fail(result.Error);
					ProcessMigrationStream(confirmedPostgres || !postgresDb.ConfirmUnsafe, result.Value, postgresDb, ApplyPostgresMigration);
				}
				if (oracleDb.CompileMigration)
				{
					var dbInfo = ReadInfoFromOracle(oracleDb);
					var result = RunMigration(dbInfo, oracleDb, dsls);
					if (!result.Success)
						return Either<string>.Fail(result.Error);
					ProcessMigrationStream(confirmedOracle || !oracleDb.ConfirmUnsafe, result.Value, oracleDb, ApplyOracleMigration);
				}
				return Either.Success(Compiler.TempPath);
			}
			catch (Exception ex)
			{
				return Either<string>.Fail(ex.Message);
			}
		}

		internal static void DeleteFile(string file, int retries = 3)
		{
			try
			{
				File.Delete(file);
			}
			catch
			{
				if (retries > 0)
				{
					if (File.Exists(file))
					{
						System.Threading.Thread.Sleep(100);
						DeleteFile(file, retries - 1);
					}
				}
				else throw;
			}
		}

		private static Either<ChunkedMemoryStream> RunMigration(DbInfo info, DatabaseInfo db, List<string> dsls)
		{
			var sb = new StringBuilder();
			sb.Append("target=").Append(info.Target).Append(info.Database.Major).Append('.').Append(info.Database.Minor);
			if (info.Compiler != null)
				sb.Append(" previous-compiler=").Append(info.Compiler.ToString());
			if (!string.IsNullOrEmpty(db.VarraySize))
				sb.Append(" varray=").Append(db.VarraySize);
			if (!string.IsNullOrEmpty(db.GrantRole))
				sb.Append(" role=").Append(db.GrantRole);
			string tempFile = null;
			if (info.Dsl != null && info.Dsl.Count > 0)
			{
				tempFile = Path.GetTempFileName();
				foreach (var d in info.Dsl.Values)
					File.AppendAllText(tempFile, d);
				sb.Append(" \"previous-dsl=").Append(tempFile).Append('"');
			}
			try
			{
				return Compiler.CompileDsl(sb, dsls, null, cms => new ChunkedMemoryStream(cms));
			}
			finally
			{
				if (tempFile != null)
					DeleteFile(tempFile);
			}
		}

		private static void SaveStreamToFile(string folder, string name, Stream stream)
		{
			if (!Directory.Exists(folder))
				Directory.CreateDirectory(folder);
			stream.Position = 0;
			using (var fs = new FileStream(Path.Combine(folder, name), FileMode.Create))
				stream.CopyTo(fs);
		}

		private static string ProcessMigrationStream(
			bool force,
			ChunkedMemoryStream stream,
			DatabaseInfo dbInfo,
			Action<ChunkedMemoryStream, bool, DatabaseInfo> applyMigration)
		{
			var customPath = dbInfo.SqlScriptsPathExists;
			var sqlPath = customPath
				? Path.Combine(LibraryInfo.BasePath, dbInfo.SqlScriptsPath)
				: Compiler.TempPath;
			var name = dbInfo.Name + "-Migration-" + DateTime.Now.ToString("yyyyMMddHHmmss") + ".sql";
			if (dbInfo.ApplyMigration && applyMigration != null)
			{
				try
				{
					applyMigration(stream, force, dbInfo);
					if (customPath)
						SaveStreamToFile(sqlPath, "Applied-" + name, stream);
				}
				catch
				{
					if (customPath)
						SaveStreamToFile(sqlPath, "Failed-" + name, stream);
					throw;
				}
			}
			else
			{
				SaveStreamToFile(sqlPath, name, stream);
				System.Diagnostics.Process.Start(sqlPath);
			}
			return Compiler.TempPath;
		}

		class DbInfo
		{
			public string Target;
			public Dictionary<string, string> Dsl;
			public Version Compiler;
			public Version Database;
		}

		public static Dictionary<string, string> ConvertHstore(string value)
		{
			var dict = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(value))
				return dict;
			var parts = value.Substring(1, value.Length - 2).Split(new[] { "\", \"", "\",\"" }, StringSplitOptions.None);
			foreach (var p in parts)
			{
				var splt = p.Split(new[] { "\"=>\"" }, StringSplitOptions.None);
				var left = splt[0].Replace("\\\"", "\"").Replace("\\\\", "\\");
				var right = splt[1].Replace("\\\"", "\"").Replace("\\\\", "\\");
				dict[left] = right;
			}
			return dict;
		}

		private static DbInfo ReadInfoFromOracle(DatabaseInfo dbInfo)
		{
			var oracleVersion = new Version(10, 2);
			try
			{
				using (var conn = new OracleConnection(dbInfo.ConnectionString))
				{
					var com = conn.CreateCommand();
					conn.Open();
					if (!Version.TryParse(conn.ServerVersion, out oracleVersion))
						oracleVersion = new Version(10, 2);
					IDataReader dr;
					try
					{
						com.CommandText = "SELECT dsls, version FROM (SELECT dsls, version FROM \"-DSL-\".database_migration ORDER BY ordinal DESC) sq WHERE ROWNUM <= 1";
						dr = com.ExecuteReader();
					}
					catch
					{
						com.CommandText = "SELECT dsls, version FROM (SELECT dsls, version FROM \"-NGS-\".database_migration ORDER BY ordinal DESC) sq WHERE ROWNUM <= 1";
						dr = com.ExecuteReader();
					}
					if (dr.Read())
					{
						var dsl = ConvertHstore(dr.GetString(0));
						var compilerVersion = Version.Parse(dr.GetString(1));
						return new DbInfo { Target = "oracle", Dsl = dsl, Compiler = compilerVersion, Database = oracleVersion };
					}
					conn.Close();
				}
			}
			catch (OracleException ex)
			{
				if (ex.Number != 942)
					throw new ApplicationException(@"Unable to read Oracle info.
Error: " + ex.Message, ex);
			}
			return new DbInfo { Target = "oracle", Dsl = new Dictionary<string, string>(), Compiler = null, Database = oracleVersion };
		}

		private static DbInfo ReadInfoFromPostgres(DatabaseInfo dbInfo)
		{
			var postgresVersion = new Version(9, 3);
			try
			{
				using (var conn = new NpgsqlConnection(dbInfo.ConnectionString))
				{
					var com = conn.CreateCommand();
					conn.Open();
					if (!Version.TryParse(conn.ServerVersion, out postgresVersion))
						postgresVersion = new Version(9, 3);
					IDataReader dr;
					try
					{
						com.CommandText = "SELECT dsls, version FROM \"-DSL-\".database_migration ORDER BY ordinal DESC LIMIT 1";
						dr = com.ExecuteReader();
					}
					catch
					{
						com.CommandText = "SELECT dsls, version FROM \"-NGS-\".database_migration ORDER BY ordinal DESC LIMIT 1";
						dr = com.ExecuteReader();
					}
					if (dr.Read())
					{
						var dsl = ConvertHstore(dr.GetString(0));
						var compilerVersion = Version.Parse(dr.GetString(1));
						return new DbInfo { Target = "postgres", Dsl = dsl, Compiler = compilerVersion, Database = postgresVersion };
					}
					conn.Close();
				}
			}
			catch (NpgsqlException ex)
			{
				if (ex.Code != "3F000" && ex.Code != "42P01")
					throw new ApplicationException(@"Unable to read Postgres info.
Error: " + ex.Message, ex);
			}
			return new DbInfo { Target = "postgres", Dsl = new Dictionary<string, string>(), Compiler = null, Database = postgresVersion };
		}

		private static void CheckForce(Stream stream)
		{
			var changes = ExtractChanges(stream);
			if (changes.Any(it => it.Type == SchemaChange.ChangeType.Remove || it.Type == SchemaChange.ChangeType.Unknown))
				throw new ApplicationException(@"Objects in database will be removed.
Since database safe mode is enabled, destructible migration can't be performed.
Modifications:
" + string.Join(Environment.NewLine, changes.Select(it => it.Description)));
			stream.Position = 0;
		}

		private static void ApplyPostgresMigration(Stream stream, bool force, DatabaseInfo dbInfo)
		{
			if (!force)
				CheckForce(stream);
			using (var conn = new Npgsql.NpgsqlConnection(dbInfo.ConnectionString))
			{
				conn.Open();
				var com = new Npgsql.NpgsqlCommand(stream);
				com.Connection = conn;
				com.ExecuteNonQuery();
				conn.Close();
			}
		}

		private static void ApplyOracleMigration(Stream stream, bool force, DatabaseInfo dbInfo)
		{
			if (!force)
				CheckForce(stream);
			using (var conn = new OracleConnection(dbInfo.ConnectionString))
			{
				var com = conn.CreateCommand();
				conn.Open();
				var sr = new StreamReader(stream, Encoding.UTF8);
				string lastLine = sr.ReadLine();
				if (lastLine == "/*MIGRATION_DESCRIPTION")
				{
					do
					{
						lastLine = sr.ReadLine();
						if (sr.EndOfStream)
							break;
					} while (lastLine != "MIGRATION_DESCRIPTION*/");
				}
				var sb = new StringBuilder();
				do
				{
					lastLine = sr.ReadLine();
					if (lastLine == "/")
					{
						com.CommandText = sb.ToString().Trim();
						if (com.CommandText.Length > 0)
							com.ExecuteNonQuery();
						sb.Clear();
					}
					else if (sb.Length > 0 || !string.IsNullOrWhiteSpace(lastLine)) sb.AppendLine(lastLine);
				} while (!sr.EndOfStream);
				com.CommandText = sb.ToString().Trim();
				if (com.CommandText.Length > 0)
					com.ExecuteNonQuery();
				conn.Close();
			}
		}
	}
}
