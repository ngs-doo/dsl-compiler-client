using System;
using System.Windows.Input;

namespace DDDLanguage
{
	internal class RelayCommand : ICommand
	{
		private readonly Action<object> Execute;
		private readonly Func<bool> CanExecute;

		public RelayCommand(Action execute)
			: this(execute, () => true) { }

		public RelayCommand(Action execute, Func<bool> canExecute)
		{
			this.Execute = o => execute();
			this.CanExecute = canExecute;
		}

		public RelayCommand(Action<object> execute, Func<bool> canExecute)
		{
			this.Execute = execute;
			this.CanExecute = canExecute;
		}

		bool ICommand.CanExecute(object parameter)
		{
			return CanExecute();
		}

		public event EventHandler CanExecuteChanged
		{
			add { CommandManager.RequerySuggested += value; }
			remove { CommandManager.RequerySuggested -= value; }
		}

		void ICommand.Execute(object parameter)
		{
			Execute(parameter);
		}
	}
}
