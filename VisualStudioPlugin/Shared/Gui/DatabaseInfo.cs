using System.IO;
using System.Text;
using System.Windows.Media;

namespace DSLPlatform
{
	internal class DatabaseInfo
	{
		private string oldConnectionString;
		public string ConnectionString { get; set; }

		public string Name { get; private set; }

		public DatabaseInfo(string name)
		{
			this.Name = name;
			ConfirmUnsafe = false;
			DiffBefore = true;
			ApplyMigration = false;
			VarraySize = "32768";
			GrantRole = "PUBLIC";
			ConnectionString = string.Empty;
			Reset();
		}

		private bool oldDiffBefore;
		public bool DiffBefore { get; set; }
		private bool oldConfirmUnsafe;
		public bool ConfirmUnsafe { get; set; }
		private bool oldApplyMigration;
		public bool ApplyMigration { get; set; }

		private string oldVarraySize;
		public string VarraySize { get; set; }
		private string oldGrantRole;
		public string GrantRole { get; set; }

		private string oldSqlScriptsPath;
		public string SqlScriptsPath { get; set; }

		public bool SqlScriptsPathExists
		{
			get
			{
				return !string.IsNullOrEmpty(SqlScriptsPath)
					&& Directory.Exists(Path.Combine(LibraryInfo.BasePath, SqlScriptsPath));
			}
		}
		public Brush SqlScriptsPathColor
		{
			get
			{
				return SqlScriptsPathExists ? Brushes.Black : Brushes.Red;
			}
		}
		public bool ConnectionStringDefined
		{
			get
			{
				return !string.IsNullOrEmpty(ConnectionString);
			}
		}

		private bool oldCompileMigration;
		private bool compile;
		public bool CompileMigration
		{
			get { return compile; }
			set
			{
				if (ConnectionStringDefined && (string.IsNullOrEmpty(SqlScriptsPath) || SqlScriptsPathExists))
					compile = value;
				else
					compile = false;
			}
		}

		public string MigrationStatusDescription
		{
			get
			{
				if (!ConnectionStringDefined)
					return @"Connection string is not defined. For database compilation (SQL migration files) valid connection string must be provided.
Please define connection string from configuration options";
				if (!string.IsNullOrEmpty(SqlScriptsPath) && !SqlScriptsPathExists)
					return "Unable to find specified SQL scripts path for " + Name + ": " + SqlScriptsPath + @".
Please fix path or choose another from configuration options";
				var sb = new StringBuilder("SQL migration script will be created ");
				if (!string.IsNullOrEmpty(SqlScriptsPath))
					sb.Append("in ").Append(SqlScriptsPath).Append(" ");
				if (ApplyMigration)
					sb.Append("and applied directly to the database.");
				else
					sb.Append("but will not be applied to the database.");
				return sb.ToString();
			}
		}

		public bool CanDiff
		{
			get
			{
				return !string.IsNullOrEmpty(ConnectionString) && (string.IsNullOrEmpty(SqlScriptsPath) || SqlScriptsPathExists);
			}
		}

		public bool HasChanges()
		{
			return oldDiffBefore != DiffBefore
					|| oldConfirmUnsafe != ConfirmUnsafe
					|| oldApplyMigration != ApplyMigration
					|| oldVarraySize != VarraySize
					|| oldGrantRole != GrantRole
					|| oldSqlScriptsPath != SqlScriptsPath
					|| oldConnectionString != ConnectionString
					|| oldCompileMigration != CompileMigration;
		}

		public void Reset()
		{
			oldDiffBefore = DiffBefore;
			oldConfirmUnsafe = ConfirmUnsafe;
			oldApplyMigration = ApplyMigration;
			oldVarraySize = VarraySize;
			oldGrantRole = GrantRole;
			oldSqlScriptsPath = SqlScriptsPath;
			oldConnectionString = ConnectionString;
			oldCompileMigration = CompileMigration;
		}
	}
}
