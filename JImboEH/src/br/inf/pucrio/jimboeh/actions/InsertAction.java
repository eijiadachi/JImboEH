package br.inf.pucrio.jimboeh.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.ui.DialogConstructor;
import br.inf.pucrio.jimboeh.util.UtilAST;

public class InsertAction implements IObjectActionDelegate
{

	private ISelection selection;

	private void insertSelection(final Object[] result)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void run(final IAction action)
	{
		try
		{
			final StructuredSelection structuredSelection = (StructuredSelection) selection;

			final Object firstElement = structuredSelection.getFirstElement();

			if (firstElement instanceof IMethod)
			{
				final IMethod selectedMethod = (IMethod) firstElement;

				final MethodDeclaration methodNode = UtilAST.astNode( selectedMethod );

				final MethodVisitor visitor = new MethodVisitor();

				methodNode.accept( visitor );

				final MethodContext context = visitor.getContext();

				MessageDialog.openInformation( null, "JImboEH", context.toString() );
			}
			else
			{
				final ElementTreeSelectionDialog dialog = DialogConstructor.buildDialog( firstElement );

				final int openStatus = dialog.open();
				if (openStatus == Window.OK)
				{
					final Object[] result = dialog.getResult();

					insertSelection( result );

					for (final Object object : result)
					{
						MessageDialog.openInformation( null, "JImboEH", object.getClass().getName() );
					}
				}
			}

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
		this.selection = selection;
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart)
	{
		// TODO Auto-generated method stub

	}

}
