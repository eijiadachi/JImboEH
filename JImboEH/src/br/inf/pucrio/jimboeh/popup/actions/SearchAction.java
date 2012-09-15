package br.inf.pucrio.jimboeh.popup.actions;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.JImboEHFacade;
import br.inf.pucrio.jimboeh.util.UtilUI;
import br.inf.pucrio.jimboeh.views.SearchResultView;

public class SearchAction implements IEditorActionDelegate
{

	private IFile file;

	@Override
	public void run(final IAction action)
	{
		try
		{
			final ITextSelection textSelection = UtilUI.getCurrentTextSelection();

			final MethodDeclaration currentMethodDeclaration = UtilUI.getCurrentMethodDeclaration( file, textSelection );

			final List<String> recommendations = JImboEHFacade.recommend( currentMethodDeclaration );

			final SearchResultView resultView = UtilUI.getSearchResultView();

			resultView.setContent( recommendations );

			MessageDialog.openInformation( null, "JImboEH", "Search Action was executed." );
		}
		catch (final IOException e)
		{
			final IStatus status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
			final StatusManager statusManager = StatusManager.getManager();
			statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
		}
		catch (final CoreException e)
		{
			final IStatus status = e.getStatus();
			final StatusManager statusManager = StatusManager.getManager();
			statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
		}

	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection)
	{
		// Do Nothing
	}

	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor)
	{
		file = UtilUI.getCurrentFile( targetEditor );
	}

}
