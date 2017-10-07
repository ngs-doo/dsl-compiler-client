using System;
using System.ComponentModel.Design;
using System.Runtime.InteropServices;
using System.Windows;
using EnvDTE;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;

namespace DDDLanguage
{
	/// <summary>
	/// This is the class that implements the package exposed by this assembly.
	///
	/// The minimum requirement for a class to be considered a valid package for Visual Studio
	/// is to implement the IVsPackage interface and register itself with the shell.
	/// This package uses the helper classes defined inside the Managed Package Framework (MPF)
	/// to do it: it derives from the Package class that provides the implementation of the 
	/// IVsPackage interface and uses the registration attributes defined in the framework to 
	/// register itself and its components with the shell.
	/// </summary>
	// This attribute tells the PkgDef creation utility (CreatePkgDef.exe) that this class is
	// a package.
	[PackageRegistration(UseManagedResourcesOnly = true)]
	// This attribute is used to register the informations needed to show the this package
	// in the Help/About dialog of Visual Studio.
	[InstalledProductRegistration("#110", "#112", "1.0", IconResourceID = 400)]
	// This attribute is needed to let the shell know that this package exposes some menus.
	[ProvideMenuResource("Menus.ctmenu", 1)]
	// This attribute registers a tool window exposed by this package.
	[ProvideSolutionProps(SolutionPersistanceKey)]
	[ProvideToolWindow(typeof(ToolWindow))]
	[Guid(GuidList.guidDDDLanguagePkgString)]
	public sealed class DDDLanguagePackage : Package, IVsPersistSolutionProps
	{
		private readonly ToolPresenter Presenter = new ToolPresenter();
		public const string SolutionPersistanceKey = "DslPlatformSolutionProperties";
		private SolutionEvents DteSolutionEvents;
		private OleMenuCommand MenuItemCompile;

		/// <summary>
		/// Default constructor of the package.
		/// Inside this method you can place any initialization code that does not require 
		/// any Visual Studio service because at this point the package object is created but 
		/// not sited yet inside Visual Studio environment. The place to do all the other 
		/// initialization is the Initialize method.
		/// </summary>
		public DDDLanguagePackage()
		{
		}

		/////////////////////////////////////////////////////////////////////////////
		// Overriden Package Implementation
		#region Package Members

		/// <summary>
		/// Initialization of the package; this method is called right after the package is sited, so this is the place
		/// where you can put all the initilaization code that rely on services provided by VisualStudio.
		/// </summary>
		protected override void Initialize()
		{
			//Trace.WriteLine(string.Format(CultureInfo.CurrentCulture, "Entering Initialize() of: {0}", this.ToString()));
			base.Initialize();

			// Add our command handlers for menu (commands must exist in the .vsct file)
			OleMenuCommandService mcs = GetService(typeof(IMenuCommandService)) as OleMenuCommandService;
			if (mcs != null)
			{
				// Create the command for the menu item.
				CommandID menuCommandID = new CommandID(GuidList.guidDDDLanguageCmdSet, (int)PkgCmdIDList.cmdDslPlatformCmd);
				MenuCommand menuItem = new MenuCommand(LoginDslPlatformWindow, menuCommandID);
				mcs.AddCommand(menuItem);
				// Create the command for the tool window
				CommandID toolwndCommandID = new CommandID(GuidList.guidDDDLanguageCmdSet, (int)PkgCmdIDList.cmdDslPlatformTool);
				MenuCommand menuToolWin = new MenuCommand(ShowDslPlatformWindow, toolwndCommandID);
				mcs.AddCommand(menuToolWin);

				CommandID compileCommandID = new CommandID(GuidList.guidDDDLanguageCmdSet, (int)PkgCmdIDList.cmdCompileDslCmd);
				MenuItemCompile = new OleMenuCommand(CompileDsl, compileCommandID);
				MenuItemCompile.BeforeQueryStatus += MenuItemCompile_BeforeQueryStatus;
				MenuItemCompile.Visible = false;
				mcs.AddCommand(MenuItemCompile);
			}
			ToolWindowPane window = this.FindToolWindow(typeof(ToolWindow), 0, true);
			var dte = GetService(typeof(DTE)) as DTE;
			if (window != null && window.Frame != null && dte != null)
			{
				Presenter.Initialize(window.Content as ToolContent, dte);
				DteSolutionEvents = dte.Events.SolutionEvents;
				DteSolutionEvents.Opened += () => Presenter.PrepareWindow();
				DteSolutionEvents.AfterClosing += () => Presenter.SolutionClosed();
			}
		}

		void MenuItemCompile_BeforeQueryStatus(object sender, EventArgs e)
		{
			var dte = GetService(typeof(DTE)) as DTE;
			MenuItemCompile.Visible = Presenter.CanCompile() && dte.ActiveWindow != null
				&& (dte.ActiveWindow.Caption.EndsWith(".dsl", StringComparison.InvariantCultureIgnoreCase)
					|| dte.ActiveWindow.Caption.EndsWith(".ddd", StringComparison.InvariantCultureIgnoreCase));
		}

		#endregion

		private void LoginDslPlatformWindow(object sender, EventArgs e)
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
			ToolWindowPane window = this.FindToolWindow(typeof(ToolWindow), 0, true);
			if (window == null || window.Frame == null)
				throw new NotSupportedException(Resources.CanNotCreateWindow);
			IVsWindowFrame windowFrame = (IVsWindowFrame)window.Frame;
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
			if (!info.SourceOnly)
				info.Name = TryReadString(info.Type + ".Name", pBag) ?? info.Name;
			info.Target = TryReadString(info.Type + ".Target", pBag) ?? info.Target;
			info.Dependencies = TryReadString(info.Type + ".Dependencies", pBag) ?? info.Dependencies;
			info.Namespace = TryReadString(info.Type + ".Namespace", pBag) ?? info.Namespace;
			info.WithActiveRecord = TryReadBool(info.Type + ".ActiveRecord", pBag, info.WithActiveRecord);
			info.WithHelperMethods = TryReadBool(info.Type + ".HelperMethods", pBag, info.WithHelperMethods);
			info.WithManualJson = TryReadBool(info.Type + ".WithManualJson", pBag, info.WithManualJson);
			info.UseUtc = TryReadBool(info.Type + ".UseUtc", pBag, info.UseUtc);
			info.Legacy = TryReadBool(info.Type + ".Legacy", pBag, info.Legacy);
			info.MinimalSerialization = TryReadBool(info.Type + ".MinimalSerialization", pBag, info.MinimalSerialization);
			info.NoPrepareExecute = TryReadBool(info.Type + ".NoPrepareExecute", pBag, info.NoPrepareExecute);
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

		private void WriteInfo(LibraryInfo info, IPropertyBag pBag)
		{
			var reference = new LibraryInfo(info.Type, null, null);
			object val;
			if (reference.CompileOption != info.CompileOption)
			{
				val = info.CompileOption.ToString();
				pBag.Write(info.Type + ".Compile", ref val);
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
				WriteInfo(Presenter.PocoLibrary, pPropBag);
				WriteInfo(Presenter.ClientLibrary, pPropBag);
				WriteInfo(Presenter.PortableLibrary, pPropBag);
				WriteInfo(Presenter.PhpLibrary, pPropBag);
				WriteInfo(Presenter.WpfLibrary, pPropBag);
				WriteInfo(Presenter.PostgresLibrary, pPropBag);
				WriteInfo(Presenter.OracleLibrary, pPropBag);
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
