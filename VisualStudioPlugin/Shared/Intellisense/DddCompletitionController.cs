using System;
using System.ComponentModel.Composition;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.Editor;
using Microsoft.VisualStudio.Language.Intellisense;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.Text;
using Microsoft.VisualStudio.Text.Editor;
using Microsoft.VisualStudio.TextManager.Interop;
using Microsoft.VisualStudio.Utilities;

namespace DSLPlatform
{
	[Export(typeof(IVsTextViewCreationListener))]
	[ContentType("dsl")]
	[ContentType("ddd")]
	[TextViewRole(PredefinedTextViewRoles.Interactive)]
	internal sealed class DddTextViewCreationListener : IVsTextViewCreationListener
	{
		[Import]
		private IVsEditorAdaptersFactoryService AdaptersFactory = null;

		[Import]
		private ICompletionBroker CompletionBroker = null;

		public void VsTextViewCreated(IVsTextView textViewAdapter)
		{
			var view = AdaptersFactory.GetWpfTextView(textViewAdapter);

			var filter = new DddCommandFilter(view, CompletionBroker);

			IOleCommandTarget next;
			textViewAdapter.AddCommandFilter(filter, out next);
			filter.Next = next;
		}
	}

	internal sealed class DddCommandFilter : IOleCommandTarget
	{
		private ICompletionSession CurrentSession;

		public DddCommandFilter(IWpfTextView textView, ICompletionBroker broker)
		{
			this.TextView = textView;
			this.Broker = broker;
		}

		public IWpfTextView TextView { get; private set; }
		public ICompletionBroker Broker { get; private set; }
		public IOleCommandTarget Next { get; set; }

		public int Exec(ref Guid cmdGroup, uint cmdID, uint cmdExec, IntPtr pIn, IntPtr pOut)
		{
			bool handled = false;
			int hresult = VSConstants.S_OK;

			if (cmdGroup == VSConstants.VSStd2K)
			{
				switch ((VSConstants.VSStd2KCmdID)cmdID)
				{
					case VSConstants.VSStd2KCmdID.AUTOCOMPLETE:
					case VSConstants.VSStd2KCmdID.COMPLETEWORD:
						handled = StartSession();
						break;
					case VSConstants.VSStd2KCmdID.RETURN:
						handled = Complete(false);
						break;
					case VSConstants.VSStd2KCmdID.TAB:
						handled = Complete(true);
						break;
					case VSConstants.VSStd2KCmdID.CANCEL:
						handled = Cancel();
						break;
				}
			}

			if (!handled) hresult = Next.Exec(cmdGroup, cmdID, cmdExec, pIn, pOut);

			if (ErrorHandler.Succeeded(hresult))
			{
				if (cmdGroup == VSConstants.VSStd2K)
				{
					switch ((VSConstants.VSStd2KCmdID)cmdID)
					{
						case VSConstants.VSStd2KCmdID.TYPECHAR:
						case VSConstants.VSStd2KCmdID.BACKSPACE:
							Filter();
							break;
					}
				}
			}

			return hresult;
		}

		private void Filter()
		{
			if (CurrentSession == null) return;

			CurrentSession.SelectedCompletionSet.SelectBestMatch();
			CurrentSession.SelectedCompletionSet.Recalculate();
		}

		private bool Cancel()
		{
			if (CurrentSession == null) return false;

			CurrentSession.Dismiss();
			return true;
		}

		private bool Complete(bool force)
		{
			if (CurrentSession == null) return false;

			if (!CurrentSession.SelectedCompletionSet.SelectionStatus.IsSelected && !force)
			{
				CurrentSession.Dismiss();
				return false;
			}
			CurrentSession.Commit();
			return true;
		}

		private bool StartSession()
		{
			if (CurrentSession != null) return false;

			var caret = TextView.Caret.Position.BufferPosition;
			var snapshot = caret.Snapshot;

			if (!Broker.IsCompletionActive(TextView))
				CurrentSession = Broker.CreateCompletionSession(TextView, snapshot.CreateTrackingPoint(caret, PointTrackingMode.Positive), true);
			else
				CurrentSession = Broker.GetSessions(TextView)[0];

			CurrentSession.Dismissed += (_, __) => CurrentSession = null;
			try
			{
				CurrentSession.Start();
				return true;
			}
			catch
			{
				CurrentSession = null;
				return false;
			}
		}

		public int QueryStatus(ref Guid cmdGroup, uint cmds, OLECMD[] pCmds, IntPtr pText)
		{
			if (cmdGroup == VSConstants.VSStd2K)
			{
				switch ((VSConstants.VSStd2KCmdID)pCmds[0].cmdID)
				{
					case VSConstants.VSStd2KCmdID.AUTOCOMPLETE:
					case VSConstants.VSStd2KCmdID.COMPLETEWORD:
						pCmds[0].cmdf = (uint)OLECMDF.OLECMDF_ENABLED | (uint)OLECMDF.OLECMDF_SUPPORTED;
						return VSConstants.S_OK;
				}
			}
			return Next.QueryStatus(cmdGroup, cmds, pCmds, pText);
		}
	}
}