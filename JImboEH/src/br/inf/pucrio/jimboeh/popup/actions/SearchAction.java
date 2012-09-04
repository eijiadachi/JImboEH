package br.inf.pucrio.jimboeh.popup.actions;

import java.io.IOException;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.util.UtilAST;

public class SearchAction implements IEditorActionDelegate
{

	private IFile file;

	private IEditorPart getActiveEditor()
	{
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

		final IEditorPart activeEditor = activePage.getActiveEditor();

		return activeEditor;
	}

	private IFile getCurrentFile(final IEditorPart targetEditor)
	{
		final IEditorInput editorInput = targetEditor.getEditorInput();

		final IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;

		final IFile file = fileEditorInput.getFile();

		return file;
	}

	private MethodDeclaration getCurrentMethodDeclaration(final IFile file, final ITextSelection textSelection)
			throws IOException, CoreException
	{
		final ASTNode rootNode = UtilAST.astNode( file );

		final int offset = textSelection.getOffset();
		final int length = textSelection.getLength();

		final ASTNode currentNode = NodeFinder.perform( rootNode, offset, length );

		final MethodDeclaration currentMethodDeclaration = UtilAST.getMethodDeclarationParent( currentNode );

		if (currentMethodDeclaration == null)
		{
			final Status status = new Status( IStatus.WARNING, Activator.PLUGIN_ID,
					"The cursor is not localized within a method declaration." );

			throw new CoreException( status );
		}

		return currentMethodDeclaration;
	}

	private ITextSelection getCurrentTextSelection()
	{
		final IEditorPart activeEditor = getActiveEditor();

		final ITextSelection textSelection = getCurrentTextSelection( activeEditor );

		return textSelection;
	}

	private ITextSelection getCurrentTextSelection(final IEditorPart targetEditor)
	{
		final IWorkbenchPartSite site = targetEditor.getSite();
		final ISelectionProvider selectionProvider = site.getSelectionProvider();
		final ISelection selection = selectionProvider.getSelection();

		final ITextSelection textSelection = (ITextSelection) selection;

		return textSelection;
	}

	@Override
	public void run(final IAction action)
	{
		try
		{
			final ITextSelection textSelection = getCurrentTextSelection();

			final MethodDeclaration currentMethodDeclaration = getCurrentMethodDeclaration( file, textSelection );

			MessageDialog.openInformation( null, "JImboEH",
					"Search Action was executed." + currentMethodDeclaration.getName() );
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
		file = getCurrentFile( targetEditor );
	}

}
