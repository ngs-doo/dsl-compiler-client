using System;
using System.Linq;
using System.ComponentModel.Design;
using System.Diagnostics.CodeAnalysis;
using System.Runtime.InteropServices;
using EnvDTE;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;
using System.Windows;

namespace DSLPlatform
{
	[PackageRegistration(		UseManagedResourcesOnly = true
#if !VS2010	   , AllowsBackgroundLoading = true
#endif	)]	[InstalledProductRegistration("#110", "#112", "1.0", IconResourceID = 400)] // Info on this package for Help/About
	[ProvideMenuResource("Menus.ctmenu", 1)]
	[ProvideSolutionProps(SolutionPersistanceKey)]
	[ProvideToolWindow(typeof(ToolWindow))]
	[Guid(GuidList.guidDSLPlatformPkgString)]
	[SuppressMessage("StyleCop.CSharp.DocumentationRules", "SA1650:ElementDocumentationMustBeSpelledCorrectly", Justification = "pkgdef, VS and vsixmanifest are valid VS terms")]
	public sealed class DSLPlatformPackage :
#if VS2010
		Package
#else
		AsyncPackage
#endif
		, IVsPersistSolutionProps
	{
		private readonly ToolPresenter Presenter = new ToolPresenter();
		public const string SolutionPersistanceKey = "DslPlatformSolutionProperties";
		private SolutionEvents DteSolutionEvents;
		private OleMenuCommand MenuItemCompile;

#if VS2010
		protected override void Initialize()
		{
			base.Initialize();

			// Add our command handlers for menu (commands must exist in the .vsct file)
			var mcs = GetService(typeof(IMenuCommandService)) as OleMenuCommandService;
			ConfigureCommands(mcs);
			var window = this.FindToolWindow(typeof(ToolWindow), 0, true);
			var dte = GetService(typeof(DTE)) as DTE;
			ConfigureDTE(window, dte);
		}
#else
		protected override async System.Threading.Tasks.Task InitializeAsync(System.Threading.CancellationToken cancellationToken, IProgress<ServiceProgressData> progress)
		{
			await base.InitializeAsync(cancellationToken, progress);

			await this.JoinableTaskFactory.SwitchToMainThreadAsync(cancellationToken);
			var mcs = await GetServiceAsync((typeof(IMenuCommandService))) as OleMenuCommandService;
			ConfigureCommands(mcs);
			var dte = await GetServiceAsync(typeof(DTE)) as DTE;
			var window = this.FindToolWindow(typeof(ToolWindow), 0, true);
			ConfigureDTE(window, dte);
		}
#endif
		private void ConfigureDTE(ToolWindowPane window, DTE dte)
		{
			if (window == null || window.Frame == null || dte == null) return;
#if !VS2010
			ThreadHelper.ThrowIfNotOnUIThread();
#endif
			Presenter.Initialize(window.Content as ToolContent, dte);
			DteSolutionEvents = dte.Events.SolutionEvents;
			DteSolutionEvents.Opened += () => Presenter.PrepareWindow();
			DteSolutionEvents.AfterClosing += () => Presenter.SolutionClosed();
		}

		private void ConfigureCommands(OleMenuCommandService mcs)
		{
			if (mcs == null) return;

			var menuCommandID = new CommandID(GuidList.guidDSLPlatformCmdSet, PkgCmdIDList.cmdDslPlatformCmd);
			var menuItem = new MenuCommand(CreateDslPlatformWindow, menuCommandID);
			mcs.AddCommand(menuItem);

			var toolwndCommandID = new CommandID(GuidList.guidDSLPlatformCmdSet, PkgCmdIDList.cmdDslPlatformTool);
			var menuToolWin = new MenuCommand(ShowDslPlatformWindow, toolwndCommandID);
			mcs.AddCommand(menuToolWin);

			var compileCommandID = new CommandID(GuidList.guidDSLPlatformCmdSet, PkgCmdIDList.cmdCompileDslCmd);
			MenuItemCompile = new OleMenuCommand(CompileDsl, compileCommandID);
			MenuItemCompile.BeforeQueryStatus += MenuItemCompile_BeforeQueryStatus;
			MenuItemCompile.Visible = false;
			mcs.AddCommand(MenuItemCompile);
		}

		void MenuItemCompile_BeforeQueryStatus(object sender, EventArgs e)
		{
#if !VS2010
			ThreadHelper.ThrowIfNotOnUIThread();
#endif
			var dte = GetService(typeof(DTE)) as DTE;
			if (dte == null) return;
			MenuItemCompile.Visible = Presenter.CanCompile() && dte.ActiveWindow != null
				&& (dte.ActiveWindow.Caption.EndsWith(".dsl", StringComparison.InvariantCultureIgnoreCase)
					|| dte.ActiveWindow.Caption.EndsWith(".ddd", StringComparison.InvariantCultureIgnoreCase));
		}

		private void CreateDslPlatformWindow(object sender, EventArgs e)
		{
			if (string.IsNullOrEmpty(Presenter.DslCompiler))
			{
				var result =
					MessageBox.Show(
						"No DSL Platform configuration detected. Do you wish to create new DSL Platform project?",
						"Missing configuration",
						MessageBoxButton.OKCancel,
						MessageBoxImage.Question,
						MessageBoxResult.OK);
				if (result == MessageBoxResult.OK)
					Presenter.CreateNewSolution();
			}
			else
			{
				DslPlatformWindow();
			}
		}

		private void ShowDslPlatformWindow(object sender, EventArgs e)
		{
			DslPlatformWindow();
		}

		private void CompileDsl(object sender, EventArgs e)
		{
			if (Presenter.CanCompile()) Presenter.Compile.Execute(null);
		}

		private void DslPlatformWindow()
		{
#if !VS2010
			ThreadHelper.ThrowIfNotOnUIThread();
#endif
			var window = this.FindToolWindow(typeof(ToolWindow), 0, true);
			if (window == null || window.Frame == null)
				throw new NotSupportedException("Can not create tool window.");
			var windowFrame = (IVsWindowFrame)window.Frame;
			Microsoft.VisualStudio.ErrorHandler.ThrowOnFailure(windowFrame.Show());
		}

		private bool TryReadBool(string property, IPropertyBag pBag, bool defaultValue = false)
		{
			try
			{
				object cs;
				pBag.Read(property, out cs, null, 0, null);
				bool val;
				bool.TryParse(cs.ToString(), out val);
				return val;
			}
			catch { return defaultValue; }
		}

		private string TryReadString(string property, IPropertyBag pBag)
		{
			try
			{
				object cs;
				pBag.Read(property, out cs, null, 0, null);
				return cs.ToString();
			}
			catch { return null; }
		}
		
		private void ReadInfo(DatabaseInfo info, IPropertyBag pBag)
		{
			info.ConnectionString = TryReadString(info.Name + ".ConnectionString", pBag);
			info.SqlScriptsPath = TryReadString(info.Name + ".SqlScriptsPath", pBag);
			info.ConfirmUnsafe = TryReadBool(info.Name + ".ConfirmUnsafe", pBag, false);
			info.DiffBefore = TryReadBool(info.Name + ".DiffBefore", pBag, true);
			info.ApplyMigration = TryReadBool(info.Name + ".ApplyMigration", pBag, false);
			info.VarraySize = TryReadString(info.Name + ".VarraySize", pBag);
			info.GrantRole = TryReadString(info.Name + ".GrantRole", pBag);
			info.CompileMigration = TryReadBool(info.Name + ".CompileMigration", pBag, false);
		}

		private void ReadInfo(LibraryInfo info, IPropertyBag pBag)
		{
			info.CompileOption = TryReadBool(info.Type + ".Compile", pBag);
			string buildType = TryReadString(info.Type + ".BuildType", pBag);
			if (!string.IsNullOrEmpty(buildType))
				info.BuildType = (BuildTypes)Enum.Parse(typeof(BuildTypes), buildType);
			if (info.BuildType != BuildTypes.Source)
				info.Name = TryReadString(info.Type + ".Name", pBag) ?? info.Name;
			info.Target = TryReadString(info.Type + ".Target", pBag) ?? info.Target;
			info.Dependencies = TryReadString(info.Type + ".Dependencies", pBag) ?? info.Dependencies;
			var nugets = TryReadString(info.Type + ".Nugets", pBag);
			if (!string.IsNullOrWhiteSpace(nugets))
			{
				var values = nugets.Split(';');
				info.Nugets = values.Select(it =>
				{
					var parts = it.Split(':');
					return new LibraryInfo.Nuget { Project = parts[0], Version = parts[parts.Length - 1] };
				}).ToList();
			}
			info.Namespace = TryReadString(info.Type + ".Namespace", pBag) ?? info.Namespace;
			info.WithActiveRecord = TryReadBool(info.Type + ".ActiveRecord", pBag, info.WithActiveRecord);
			info.WithHelperMethods = TryReadBool(info.Type + ".HelperMethods", pBag, info.WithHelperMethods);
			info.WithManualJson = TryReadBool(info.Type + ".WithManualJson", pBag, info.WithManualJson);
			info.UseUtc = TryReadBool(info.Type + ".UseUtc", pBag, info.UseUtc);
			info.Legacy = TryReadBool(info.Type + ".Legacy", pBag, info.Legacy);
			info.MinimalSerialization = TryReadBool(info.Type + ".MinimalSerialization", pBag, info.MinimalSerialization);
			info.NoPrepareExecute = TryReadBool(info.Type + ".NoPrepareExecute", pBag, info.NoPrepareExecute);
			info.MutableSnowflake = TryReadBool(info.Type + ".MutableSnowflake", pBag, info.MutableSnowflake);
		}
		
		public int ReadSolutionProps(IVsHierarchy pHierarchy, string pszProjectName, string pszProjectMk, string pszKey, int fPreLoad, IPropertyBag pPropBag)
		{
			try
			{
				Presenter.SetupBasePath();
				ReadInfo(Presenter.PostgresDb, pPropBag);
				ReadInfo(Presenter.OracleDb, pPropBag);
				ReadInfo(Presenter.PocoLibrary, pPropBag);
				ReadInfo(Presenter.ClientLibrary, pPropBag);
				ReadInfo(Presenter.PortableLibrary, pPropBag);
				ReadInfo(Presenter.PhpLibrary, pPropBag);
				ReadInfo(Presenter.TypescriptLibrary, pPropBag);
				ReadInfo(Presenter.WpfLibrary, pPropBag);
				ReadInfo(Presenter.PostgresLibrary, pPropBag);
				ReadInfo(Presenter.OracleLibrary, pPropBag);
			}
			catch { }
			return VSConstants.S_OK;
		}

		public int LoadUserOptions(IVsSolutionPersistence pPersistence, uint grfLoadOpts) { return VSConstants.S_OK; }
		public int OnProjectLoadFailure(IVsHierarchy pStubHierarchy, string pszProjectName, string pszProjectMk, string pszKey) { return VSConstants.S_OK; }
		public int QuerySaveSolutionProps(IVsHierarchy pHierarchy, VSQUERYSAVESLNPROPS[] pqsspSave)
		{
			if (pHierarchy == null)
			{
				var result = VSQUERYSAVESLNPROPS.QSP_HasNoProps;

				if (Presenter.HasChangedConfiguration)
					result = VSQUERYSAVESLNPROPS.QSP_HasDirtyProps;
				else
					result = VSQUERYSAVESLNPROPS.QSP_HasNoDirtyProps;
				pqsspSave[0] = result;
			}
			return VSConstants.S_OK;
		}
		public int ReadUserOptions(IStream pOptionsStream, string pszKey) { return VSConstants.S_OK; }
		public int SaveSolutionProps(IVsHierarchy pHierarchy, IVsSolutionPersistence pPersistence)
		{
			if (pHierarchy == null)
			{
				pPersistence.SavePackageSolutionProps(1 /* TRUE */, null, this, SolutionPersistanceKey);
				Presenter.ResetConfiguration();
			}

			return VSConstants.S_OK;
		}
		public int SaveUserOptions(IVsSolutionPersistence pPersistence) { return VSConstants.S_OK; }
		
		private void WriteInfo(DatabaseInfo info, IPropertyBag pBag)
		{
			var reference = new DatabaseInfo(info.Name);
			object val;
			if (reference.ConnectionString != info.ConnectionString && info.ConnectionString != null)
			{
				val = info.ConnectionString;
				pBag.Write(info.Name + ".ConnectionString", ref val);
			}
			if (reference.SqlScriptsPath != info.SqlScriptsPath && info.SqlScriptsPath != null)
			{
				val = info.SqlScriptsPath;
				pBag.Write(info.Name + ".SqlScriptsPath", ref val);
			}
			if (reference.ConfirmUnsafe != info.ConfirmUnsafe)
			{
				val = info.ConfirmUnsafe.ToString();
				pBag.Write(info.Name + ".ConfirmUnsafe", ref val);
			}
			if (reference.DiffBefore != info.DiffBefore)
			{
				val = info.DiffBefore.ToString();
				pBag.Write(info.Name + ".DiffBefore", ref val);
			}
			if (reference.ApplyMigration != info.ApplyMigration)
			{
				val = info.ApplyMigration.ToString();
				pBag.Write(info.Name + ".ApplyMigration", ref val);
			}
			if (reference.VarraySize != info.VarraySize && info.VarraySize != null)
			{
				val = info.VarraySize;
				pBag.Write(info.Name + ".VarraySize", ref val);
			}
			if (reference.GrantRole != info.GrantRole && info.GrantRole != null)
			{
				val = info.GrantRole;
				pBag.Write(info.Name + ".GrantRole", ref val);
			}
			if (reference.CompileMigration != info.CompileMigration)
			{
				val = info.CompileMigration.ToString();
				pBag.Write(info.Name + ".CompileMigration", ref val);
			}
		}

		private void WriteInfo(LibraryInfo info, LibraryInfo reference, IPropertyBag pBag)
		{
			object val;
			if (reference.CompileOption != info.CompileOption)
			{
				val = info.CompileOption.ToString();
				pBag.Write(info.Type + ".Compile", ref val);
			}
			if (reference.BuildType != info.BuildType)
			{
				val = info.BuildType.ToString();
				pBag.Write(info.Type + ".BuildType", ref val);
			}
			if (reference.Name != info.Name)
			{
				val = info.Name;
				pBag.Write(info.Type + ".Name", ref val);
			}
			if (reference.Target != info.Target)
			{
				val = info.Target;
				pBag.Write(info.Type + ".Target", ref val);
			}
			if (reference.Dependencies != info.Dependencies)
			{
				val = info.Dependencies;
				pBag.Write(info.Type + ".Dependencies", ref val);
			}
			if (!LibraryInfo.Nuget.Equal(info.Nugets, reference.Nugets))
			{
				val = string.Join(";", info.Nugets.Select(it => it.Project + ":" + it.Version));
				pBag.Write(info.Type + ".Nugets", ref val);
			}
			if (reference.Namespace != info.Namespace)
			{
				val = info.Namespace;
				pBag.Write(info.Type + ".Namespace", ref val);
			}
			if (reference.WithActiveRecord != info.WithActiveRecord)
			{
				val = info.WithActiveRecord.ToString();
				pBag.Write(info.Type + ".ActiveRecord", ref val);
			}
			if (reference.WithHelperMethods != info.WithHelperMethods)
			{
				val = info.WithHelperMethods.ToString();
				pBag.Write(info.Type + ".HelperMethods", ref val);
			}
			if (reference.WithManualJson != info.WithManualJson)
			{
				val = info.WithManualJson.ToString();
				pBag.Write(info.Type + ".WithManualJson", ref val);
			}
			if (reference.UseUtc != info.UseUtc)
			{
				val = info.UseUtc.ToString();
				pBag.Write(info.Type + ".UseUtc", ref val);
			}
			if (reference.Legacy != info.Legacy)
			{
				val = info.Legacy.ToString();
				pBag.Write(info.Type + ".Legacy", ref val);
			}
			if (reference.MinimalSerialization != info.MinimalSerialization)
			{
				val = info.MinimalSerialization.ToString();
				pBag.Write(info.Type + ".MinimalSerialization", ref val);
			}
			if (reference.NoPrepareExecute != info.NoPrepareExecute)
			{
				val = info.NoPrepareExecute.ToString();
				pBag.Write(info.Type + ".NoPrepareExecute", ref val);
			}
			if (reference.MutableSnowflake != info.MutableSnowflake)
			{
				val = info.MutableSnowflake.ToString();
				pBag.Write(info.Type + ".MutableSnowflake", ref val);
			}
		}
		
		public int WriteSolutionProps(IVsHierarchy pHierarchy, string pszKey, IPropertyBag pPropBag)
		{
			if (pHierarchy != null)
				return VSConstants.S_OK;
			else if (pPropBag == null)
				return VSConstants.E_POINTER;
			try
			{
				WriteInfo(Presenter.PostgresDb, pPropBag);
				WriteInfo(Presenter.OracleDb, pPropBag);
				WriteInfo(Presenter.PocoLibrary, CompileTargets.PocoLibraryDefault, pPropBag);
				WriteInfo(Presenter.ClientLibrary, CompileTargets.ClientLibraryDefault, pPropBag);
				WriteInfo(Presenter.PortableLibrary, CompileTargets.PortableLibraryDefault, pPropBag);
				WriteInfo(Presenter.PhpLibrary, CompileTargets.PhpSourceDefault, pPropBag);
				WriteInfo(Presenter.TypescriptLibrary, CompileTargets.TypescriptSourceDefault, pPropBag);
				WriteInfo(Presenter.WpfLibrary, CompileTargets.WpfLibraryDefault, pPropBag);
				WriteInfo(Presenter.PostgresLibrary, CompileTargets.PostgresLibraryDefault, pPropBag);
				WriteInfo(Presenter.OracleLibrary, CompileTargets.OracleLibraryDefault, pPropBag);
			}
			catch
			{
				return VSConstants.S_FALSE;
			}
			return VSConstants.S_OK;
		}
		public int WriteUserOptions(IStream pOptionsStream, string pszKey) { return VSConstants.S_OK; }
	}
}
