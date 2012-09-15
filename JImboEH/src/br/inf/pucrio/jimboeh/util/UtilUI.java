package br.inf.pucrio.jimboeh.util;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.views.DetailedResultView;
import br.inf.pucrio.jimboeh.views.SearchResultView;

public class UtilUI
{
	public static IEditorPart getActiveEditor()
	{
		final IWorkbenchPage activePage = getActivePage();

		final IEditorPart activeEditor = activePage.getActiveEditor();

		return activeEditor;
	}

	public static IWorkbenchPage getActivePage()
	{
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		return activePage;
	}

	public static IFile getCurrentFile(final IEditorPart targetEditor)
	{
		final IEditorInput editorInput = targetEditor.getEditorInput();

		final IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;

		final IFile file = fileEditorInput.getFile();

		return file;
	}

	public static MethodDeclaration getCurrentMethodDeclaration(final IFile file, final ITextSelection textSelection)
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

	public static ITextSelection getCurrentTextSelection()
	{
		final IEditorPart activeEditor = getActiveEditor();

		final ITextSelection textSelection = getCurrentTextSelection( activeEditor );

		return textSelection;
	}

	public static ITextSelection getCurrentTextSelection(final IEditorPart targetEditor)
	{
		final IWorkbenchPartSite site = targetEditor.getSite();
		final ISelectionProvider selectionProvider = site.getSelectionProvider();
		final ISelection selection = selectionProvider.getSelection();

		final ITextSelection textSelection = (ITextSelection) selection;

		return textSelection;
	}

	public static DetailedResultView getDetailedResultView() throws PartInitException
	{
		final IViewPart showedView = getViewPart( DetailedResultView.ID );

		final DetailedResultView resultView = (DetailedResultView) showedView;

		return resultView;
	}

	public static SearchResultView getSearchResultView() throws PartInitException
	{
		final IViewPart showedView = getViewPart( SearchResultView.ID );

		final SearchResultView resultView = (SearchResultView) showedView;

		return resultView;
	}

	private static IViewPart getViewPart(final String viewPartId) throws PartInitException
	{
		final IWorkbenchPage activePage = getActivePage();

		final IViewPart showedView = activePage.showView( viewPartId );
		return showedView;
	}
}
