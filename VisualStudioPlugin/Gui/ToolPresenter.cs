using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Forms;
using System.Windows.Input;

namespace DDDLanguage
{
	internal class ToolPresenter : INotifyPropertyChanged
	{
		public string CompilerInfo { get; private set; }

		public string Message { get; private set; }

		private readonly CompileTargets Targets = new CompileTargets();

		private string oldDslCompiler;
		public string DslCompiler
		{
			get { return Targets.CompilerPath; }
			set { Targets.CompilerPath = value; }
		}

		public LibraryInfo PocoLibrary { get { return Targets.PocoLibrary; } }
		public LibraryInfo ClientLibrary { get { return Targets.ClientLibrary; } }
		public LibraryInfo PortableLibrary { get { return Targets.PortableLibrary; } }
		public LibraryInfo WpfLibrary { get { return Targets.WpfLibrary; } }
		public LibraryInfo OracleLibrary { get { return Targets.OracleLibrary; } }
		public LibraryInfo PostgresLibrary { get { return Targets.PostgresLibrary; } }
		public LibraryInfo PhpLibrary { get { return Targets.PhpSource; } }

		public DatabaseInfo PostgresDb { get; private set; }
		public DatabaseInfo OracleDb { get; private set; }

		public ICommand ExtractPostgresDsl { get; private set; }
		public ICommand ExtractOracleDsl { get; private set; }
		public ICommand Compile { get; private set; }
		public ICommand Parse { get; private set; }
		public ICommand PostgresDiff { get; private set; }
		public ICommand OracleDiff { get; private set; }

		public ICommand DownloadLibrary { get; private set; }
		public ICommand ChangeTarget { get; private set; }
		public ICommand ChangeDependencies { get; private set; }
		public ICommand ChangePostgresSqlScripts { get; private set; }
		public ICommand ChangeOracleSqlScripts { get; private set; }

		public ICommand ConfigurePoco { get; private set; }
		public ICommand ConfigureClient { get; private set; }
		public ICommand ConfigurePortable { get; private set; }
		public ICommand ConfigurePhp { get; private set; }
		public ICommand ConfigureWpf { get; private set; }
		public ICommand ConfigurePostgres { get; private set; }
		public ICommand ConfigureOracle { get; private set; }

		public ICommand BackToStatus { get; private set; }

		public ICommand ConfirmDiff { get; private set; }
		public ICommand ConfirmChanges { get; private set; }

		public ICommand DownloadCompiler { get; private set; }

		public Visibility ConfirmVisibility { get; private set; }

		private EnvDTE.DTE DTE;
		private System.Windows.Window DialogWindow;
		private ToolContent LastTool;

		public DiffModel[] DslDiff { get; private set; }
		public SchemaChange[] DbChanges { get; private set; }
		public Visibility MigrationsVisibility
		{
			get
			{
				return DbChanges != null && DbChanges.Length > 0 ? Visibility.Visible : Visibility.Collapsed;
			}
		}

		public bool HasChangedConfiguration
		{
			get
			{
				return OracleDb.HasChanges()
					|| PostgresDb.HasChanges()
					|| oldDslCompiler != DslCompiler
					|| Targets.HasChanges();
			}
		}

		public void ResetConfiguration()
		{
			OracleDb.Reset();
			PostgresDb.Reset();
			oldDslCompiler = DslCompiler;
			Targets.Reset(DslCompiler);
		}

		public ToolPresenter()
		{
			PostgresDb = new DatabaseInfo("Postgres");
			OracleDb = new DatabaseInfo("Oracle");
			ExtractPostgresDsl =
				new RelayCommand(
					() => TryAction("Loading latest applied DSL ...", () => ServerActions.ExtractPostgresDsl(PostgresDb)).OnSuccess((path) => Process.Start(path)),
					() => PostgresDb.ConnectionStringDefined);
			ExtractOracleDsl =
				new RelayCommand(
					() => TryAction("Loading latest applied DSL ...", () => ServerActions.ExtractOracleDsl(OracleDb)).OnSuccess((path) => Process.Start(path)),
					() => OracleDb.ConnectionStringDefined);
			Compile = new RelayCommand(CompileAction, CanCompile);
			Parse =
				new RelayCommand(
					() => TryAction("Parsing DSL ...", () => ServerActions.Parse(DTE)).OnSuccess(m => { Message = m; ChangeMessage(); }),
					HasCompiler);
			PostgresDiff = new RelayCommand(() => CreateDiffAction(ServerActions.PostgresDiff, PostgresDb), () => PostgresDb.CanDiff);
			OracleDiff = new RelayCommand(() => CreateDiffAction(ServerActions.OracleDiff, OracleDb), () => OracleDb.CanDiff);
			DownloadLibrary = new RelayCommand(arg => DonwloadLibraryAction(arg as string), () => true);
			ChangeTarget = new RelayCommand(arg => ChangePathAction(arg as string, i => i.TargetPath, (i, p) => i.Target = p), () => true);
			ChangeDependencies =
				new RelayCommand(
					arg => ChangePathAction(
						arg as string,
						i => i.DependenciesPath,
						(i, p) => i.Dependencies = p),
					() => true);
			ChangePostgresSqlScripts = new RelayCommand(() => ChangeSqlPath(PostgresDb));
			ChangeOracleSqlScripts = new RelayCommand(() => ChangeSqlPath(OracleDb));
			ConfigurePoco = new RelayCommand(() => OpenConfigurationAction(new ConfigurationPocoControl()));
			ConfigureClient = new RelayCommand(() => OpenConfigurationAction(new ConfigurationClientControl()));
			ConfigurePortable = new RelayCommand(() => OpenConfigurationAction(new ConfigurationPortableControl()));
			ConfigurePhp = new RelayCommand(() => OpenConfigurationAction(new ConfigurationPhpControl()));
			ConfigureWpf = new RelayCommand(() => OpenConfigurationAction(new ConfigurationWpfControl()));
			ConfigurePostgres = new RelayCommand(() => OpenConfigurationAction(new ConfigurationPostgresControl()));
			ConfigureOracle = new RelayCommand(() => OpenConfigurationAction(new ConfigurationOracleControl()));
			BackToStatus = new RelayCommand(GoBackAction);
			ConfirmDiff = new RelayCommand(StepThroughCompilation, CanCompile);
			DownloadCompiler =
				new RelayCommand(
					() => TryAction("Downloading DSL compiler...", () => DownloadLatestCompiler()));
			ConfirmVisibility = Visibility.Collapsed;
			ResetConfiguration();

			SyntaxParser.Parsed += SyntaxParser_Parsed;
		}

		private void SyntaxParser_Parsed(object sender, SyntaxParser.ParsedArgs e)
		{
			Message = e.Error;
			ChangeMessage();
		}

		private CompilationStep CurrentStep = CompilationStep.Done;

		enum CompilationStep
		{
			Starting = 0,
			PostgresDiff = 1,
			OracleDiff = 4,
			ConfirmedPostgres = 8,
			ConfirmedOracle = 16,
			PostgresConfirmation = 32,
			OracleConfirmation = 64,
			Done
		}

		private void CompileAction()
		{
			CurrentStep = CompilationStep.Starting;
			StepThroughCompilation();
		}

		private void StepThroughCompilation()
		{
			CloseDialog();
			if (CurrentStep == CompilationStep.Starting && PostgresDb.CompileMigration && PostgresDb.DiffBefore)
			{
				CurrentStep = CompilationStep.PostgresDiff;
				DiffDatabase(ServerActions.PostgresDiff, PostgresDb, "Postgres");
			}
			else if ((CurrentStep == CompilationStep.Starting || ((CurrentStep & CompilationStep.PostgresDiff) != 0)) && OracleDb.CompileMigration && OracleDb.DiffBefore)
			{
				CurrentStep = CompilationStep.OracleDiff;
				DiffDatabase(ServerActions.OracleDiff, OracleDb, "Oracle");
			}
			else
			{
				if ((CurrentStep & CompilationStep.PostgresDiff) != 0
					|| (CurrentStep & CompilationStep.PostgresConfirmation) != 0)
				{
					CurrentStep = CurrentStep | CompilationStep.ConfirmedPostgres;
				}
				if ((CurrentStep & CompilationStep.OracleDiff) != 0
					|| (CurrentStep & CompilationStep.OracleConfirmation) != 0)
				{
					CurrentStep = CurrentStep | CompilationStep.ConfirmedOracle;
				}
				CompileDsl((CurrentStep & CompilationStep.ConfirmedPostgres) != 0, (CurrentStep & CompilationStep.ConfirmedOracle) != 0);
			}
		}

		private void DiffDatabase(
			Func<EnvDTE.DTE, string, DatabaseInfo, Either<ServerActions.DiffResult>> action,
			DatabaseInfo dbInfo,
			string database)
		{
			ConfirmVisibility = Visibility.Visible;
			TryAction("Diffing " + database + " DSL ...", () => action(DTE, DslCompiler, dbInfo))
			.OnSuccess(diff =>
			{
				Message = "Diff created";
				ChangeMessage();
				var hasUnsafe = diff.DbChanges.Any(it => it.IsUnsafe());
				if ((dbInfo.DiffBefore || hasUnsafe)
					&& ShowDiff(diff.DbChanges, diff.OldDsl, diff.NewDsl, "Confirm " + database + " DSL changes"))
				{
					Message = hasUnsafe ? "Confirm unsafe changes" : "Confirm changes";
					ChangeMessage();
				}
				else
				{
					Message = dbInfo.ConfirmUnsafe ? "No unsafe changes found" : "No changes found";
					ChangeMessage();
					StepThroughCompilation();
				}
			});
		}

		private void CreateDiffAction(
			Func<EnvDTE.DTE, string, DatabaseInfo, Either<ServerActions.DiffResult>> action,
			DatabaseInfo dbInfo)
		{
			ConfirmVisibility = Visibility.Collapsed;
			TryAction("Diffing DSL ...", () => action(DTE, DslCompiler, dbInfo))
			.OnSuccess(diff =>
			{
				Message = "Diff created";
				ChangeMessage();
				ShowDiff(diff.DbChanges, diff.OldDsl, diff.NewDsl, "View DSL changes");
			});
		}

		private void DonwloadLibraryAction(string arg)
		{
			var splt = arg.Split(':');
			var lib = Targets.ChooseLibrary(splt[0]);
			if (!lib.Success)
			{
				Message = lib.Error;
				ChangeMessage();
				return;
			}
			var info = lib.Value;
			if (string.IsNullOrWhiteSpace(info.Dependencies))
				Message = "Dependency folder not specified. Please specify " + info.Name + " dependency folder";
			else
			{
				if (!info.DependenciesExists)
					Directory.CreateDirectory(info.DependenciesPath);
				TryAction("Downloading library...", () => ServerActions.DownloadZip(splt[1], splt[splt.Length - 1], info.DependenciesPath))
				.OnSuccess(ef =>
				{
					Message = "Library downloaded";
					if (!ef)
					{
						Message += ". Extra files found in library folder.";
						System.Diagnostics.Process.Start(info.DependenciesPath);
					}
					ChangeMessage();
				});
			}
			ChangeMessage();
		}

		private void ChangePathAction(string arg, Func<LibraryInfo, string> getPath, Action<LibraryInfo, string> setPath)
		{
			var fbd = new FolderBrowserDialog();
			var lib = Targets.ChooseLibrary(arg);
			if (!lib.Success)
			{
				Message = lib.Error;
				ChangeMessage();
				return;
			}
			var info = lib.Value;
			var startPath = getPath(info);
			if (Directory.Exists(startPath))
				fbd.SelectedPath = startPath;
			else
				fbd.SelectedPath = LibraryInfo.BasePath;
			if (fbd.ShowDialog() == DialogResult.OK && fbd.SelectedPath != startPath)
			{
				if (fbd.SelectedPath.StartsWith(LibraryInfo.BasePath, StringComparison.CurrentCultureIgnoreCase))
				{
					try { setPath(info, fbd.SelectedPath); }
					catch { }
					ChangeProperty(arg + "Library");
				}
				else
				{
					Message = "Invalid path selected";
					ChangeMessage();
				}
			}
		}

		private void ChangeSqlPath(DatabaseInfo dbInfo)
		{
			var fbd = new FolderBrowserDialog();
			var startPath = dbInfo.SqlScriptsPath;
			if (Directory.Exists(startPath))
				fbd.SelectedPath = startPath;
			else
				fbd.SelectedPath = LibraryInfo.BasePath;
			if (fbd.ShowDialog() == DialogResult.OK && fbd.SelectedPath != startPath)
			{
				if (fbd.SelectedPath.StartsWith(LibraryInfo.BasePath, StringComparison.CurrentCultureIgnoreCase))
				{
					try { dbInfo.SqlScriptsPath = fbd.SelectedPath.Substring(LibraryInfo.BasePath.Length); }
					catch { }
					ChangeProperty(dbInfo.Name + "Db");
				}
				else
				{
					Message = "Invalid path selected";
					ChangeMessage();
				}
			}
		}

		private void OpenConfigurationAction(FrameworkElement view)
		{
			Message = null;
			ChangeMessage();
			view.DataContext = this;
			LastTool.Content = view;
		}

		private void GoBackAction()
		{
			Message = null;
			ChangeMessage();
			LastTool.Content = new StatusControl { DataContext = this };
			CloseDialog();
		}

		public bool HasCompiler()
		{
			return DslCompiler != null && File.Exists(DslCompiler);
		}

		public bool CanCompile()
		{
			return HasCompiler()
				&& (PocoLibrary.Compile
				|| ClientLibrary.Compile || PortableLibrary.Compile
				|| WpfLibrary.Compile
				|| PhpLibrary.Compile
				|| PostgresLibrary.Compile || OracleLibrary.Compile
				|| PostgresDb.CompileMigration || OracleDb.CompileMigration);
		}

		private void CreateDiffWindow(string title)
		{
			var diffCtrl = new DiffControl();
			diffCtrl.DataContext = this;
			CloseDialog();
			DialogWindow = new System.Windows.Window { MinHeight = 150, MinWidth = 150 };
			DialogWindow.Title = title;
			DialogWindow.Content = diffCtrl;
			DialogWindow.DataContext = this;
			DialogWindow.ShowDialog();
		}

		private void CompileDsl(bool confirmedPostgres, bool confirmedOracle)
		{
			TryAction(
				"Compiling DSL ...",
				() => ServerActions.Compile(DTE, Targets, PostgresDb, OracleDb, confirmedPostgres, confirmedOracle))
			.ContinueWith(t =>
			{
				if (t.Exception == null && !t.Result.Success && t.Result.Error.Contains("Objects in database will be removed"))
				{
					Message = "Confirm unsafe migration";
					if (PostgresDb.CompileMigration && (CurrentStep & CompilationStep.ConfirmedPostgres) == 0)
					{
						CurrentStep = CompilationStep.PostgresConfirmation;
						DiffDatabase(ServerActions.PostgresDiff, PostgresDb, "Postgres");
					}
					else
					{
						CurrentStep = CompilationStep.OracleConfirmation;
						DiffDatabase(ServerActions.OracleDiff, OracleDb, "Oracle");
					}
				}
				else
				{
					CurrentStep = CompilationStep.Done;
					DbChanges = null;
					Message = t.Result.Error;
					ChangeMessage();
				}
			});
		}

		private void CloseDialog()
		{
			if (DialogWindow != null)
			{
				//TODO gui thread
				DialogWindow.Close();
				DialogWindow = null;
			}
		}

		public void Initialize(ToolContent tool, EnvDTE.DTE dte)
		{
			if (tool == null || dte == null)
				return;
			LastTool = tool;
			this.DTE = dte;
			Message = null;
			LastTool.Content = new AboutControl { DataContext = this };
		}

		public void SetupBasePath()
		{
			LibraryInfo.BasePath = DTE.Solution != null ? Path.GetDirectoryName(DTE.Solution.FullName) + Path.DirectorySeparatorChar : null;
		}

		public void PrepareWindow()
		{
			Message = null;
			ChangeMessage();
			SetupBasePath();

			ResetConfiguration();
			Message = null;
			LastTool.Content = new StatusControl { DataContext = this };

			if (DslCompiler == null)
			{
				var fp = Path.Combine(Compiler.CompilerPath, "dsl-compiler.exe");
				if (File.Exists(fp))
					DslCompiler = fp;
			}
			CheckCompiler();
			ChangeMessage();
		}

		public void SolutionClosed()
		{
			CloseDialog();
			Message = null;
			DslCompiler = null;
			if (LastTool != null)
				LastTool.Content = new AboutControl { DataContext = this };
		}

		public void CreateNewSolution()
		{
			PrepareWindow();
			SetupCompiler();
		}

		private void SetupCompiler()
		{
			var oc = DslCompiler;
			if (string.IsNullOrEmpty(oc))
				oc = "dsl-compiler.exe";
			if (!File.Exists(oc))
			{
				var fp = Path.Combine(Compiler.CompilerPath, oc);
				if (File.Exists(fp))
					DslCompiler = fp;
			}

			if (DslCompiler == null || !File.Exists(DslCompiler))
			{
				Message = "Compiler not found.";
				ChangeMessage();
				var downRes =
					System.Windows.MessageBox.Show(
						"Compiler not found. Do you wish to download it?",
						"DSL Platform compiler",
						MessageBoxButton.YesNoCancel,
						MessageBoxImage.Question,
						MessageBoxResult.Yes);
				if (downRes == MessageBoxResult.Yes)
					Task.Factory.StartNew(DownloadCompilerAndLogIn);
				else if (downRes == MessageBoxResult.No)
				{
					var ofd = new OpenFileDialog();
					ofd.InitialDirectory = LibraryInfo.BasePath;
					ofd.CheckFileExists = true;
					ofd.FileName = "dsl-compiler.exe";
					ofd.Filter = "DSL compiler | *.exe";
					ofd.DefaultExt = ".exe";
					ofd.Title = "Select DSL compiler";
					var res = ofd.ShowDialog();
					if (res == DialogResult.OK)
					{
						DslCompiler = ofd.FileName;
						CheckCompiler();
					}
				}
			}
			else CheckCompiler();
			ChangeMessage();
		}

		private void CheckCompiler()
		{
			Message = null;
			LastTool.Dispatcher.BeginInvoke((Action)(() => LastTool.Content = new StatusControl { DataContext = this }));
			ResetConfiguration();
			ChangeMessage();
			if (!File.Exists(DslCompiler))
			{
				CompilerInfo = "Compiler: not found";
			}
			else
			{
				var fi = new FileInfo(DslCompiler);
				var asm = System.Reflection.AssemblyName.GetAssemblyName(DslCompiler);
				CompilerInfo = "Compiler: " + asm.Version;
				Targets.CompilerPath = DslCompiler;
			}
			ChangeProperty("CompilerInfo");
		}

		private Either<string> DownloadLatestCompiler()
		{
			try
			{
				Message = "Downloading DSL compiler...";
				ChangeMessage();
				Compiler.Stop(false);
				var result = ServerActions.DownloadZip("dsl-platform", "dsl-compiler.zip", Compiler.CompilerPath);
				if (result.Success)
				{
					var newPath = Path.Combine(Compiler.CompilerPath, "dsl-compiler.exe");
					var fi = new FileInfo(newPath);
					var asm = System.Reflection.AssemblyName.GetAssemblyName(newPath);
					CompilerInfo = "Compiler: " + asm.Version;
					oldDslCompiler = DslCompiler;
					Targets.CompilerPath = newPath;
					DslCompiler = newPath;
					ChangeProperty("CompilerInfo");
					return Either.Success(DslCompiler);
				}
				return Either<string>.Fail(result.Error);
			}
			catch (Exception ex)
			{
				return Either<string>.Fail(ex.Message);
			}
		}

		private void DownloadCompilerAndLogIn()
		{
			try
			{
				Message = "Downloading DSL compiler...";
				ChangeMessage();
				Compiler.Stop(false);
				var result = ServerActions.DownloadZip("dsl-platform", "dsl-compiler.zip", Compiler.CompilerPath);
				if (result.Success)
				{
					var old = DslCompiler;
					DslCompiler = Path.Combine(Compiler.CompilerPath, "dsl-compiler.exe");
					CheckCompiler();
					oldDslCompiler = old;
				}
				else Message = result.Error;
				ChangeMessage();
			}
			catch (Exception ex)
			{
				Message = ex.Message;
				ChangeMessage();
			}
		}

		private bool ShowDiff(
			SchemaChange[] dbChanges,
			Dictionary<string, string> old,
			Dictionary<string, string> current,
			string title)
		{
			var changes = new List<DiffModel>();
			var diff = new DiffPlex.Differ();
			foreach (var k in current)
			{
				if (old.ContainsKey(k.Key))
				{
					var oldValue = old[k.Key];

					var modified = DiffModel.ModificationType.NotModified;
					if (oldValue != k.Value)
					{
						var ld = diff.CreateLineDiffs(oldValue, k.Value, true);
						if (ld.DiffBlocks.Count > 0)
							modified = DiffModel.ModificationType.Modified;
					}
					changes.Add(new DiffModel { FileName = k.Key, OriginalSource = old[k.Key], NewSource = k.Value, Modified = modified });
				}
				else changes.Add(new DiffModel { FileName = k.Key, NewSource = k.Value, Modified = DiffModel.ModificationType.Created });
			}
			foreach (var k in old)
			{
				if (!current.ContainsKey(k.Key))
					changes.Add(new DiffModel { FileName = k.Key, OriginalSource = k.Value, Modified = DiffModel.ModificationType.Deleted });
			}
			var hasChanges = changes.Any(it => it.Modified != DiffModel.ModificationType.NotModified);
			DbChanges = dbChanges;
			DslDiff = changes.ToArray();
			if (hasChanges)
				LastTool.Dispatcher.BeginInvoke((Action)(() => CreateDiffWindow(title)));
			return hasChanges;
		}

		private Task<Either<T>> TryAction<T>(string description, Func<Either<T>> action)
		{
			Message = description;
			ChangeMessage();
			return Task.Factory.StartNew(() =>
			{
				var result = action();
				Message = result.Error;
				ChangeMessage();
				if (DialogWindow != null)
					DialogWindow.Dispatcher.Invoke((Action)(() => CloseDialog()));
				return result;
			});
		}

		private void ChangeMessage() { ChangeProperty("Message"); }

		private void ChangeProperty(string property)
		{
			LastTool.Dispatcher.BeginInvoke((Action)(() => PropertyChanged(this, new PropertyChangedEventArgs(property))));
		}

		public event PropertyChangedEventHandler PropertyChanged = (s, ea) => { };
	}
}
