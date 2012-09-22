package br.inf.pucrio.jimboeh.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
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

	private void insertSelectedElement(final IMethod method) throws JavaModelException
	{
		final MethodDeclaration methodNode = UtilAST.astNode( method );

		final MethodVisitor visitor = new MethodVisitor();

		methodNode.accept( visitor );

		final MethodContext context = visitor.getContext();

		final String contextStr = context.toString();

		final String methodName = method.getElementName();

		final String message = String.format( "Inserting method: %s\n\n%s", methodName, contextStr );

		MessageDialog.openInformation( null, "JImboEH", message );
	}

	private void processSelection(final Object[] result) throws JavaModelException
	{
		for (final Object object : result)
		{
			if (object instanceof IMethod)
			{
				final IMethod method = (IMethod) object;
				insertSelectedElement( method );
			}
			else if (object instanceof IParent)
			{
				final IParent element = (IParent) object;
				final IJavaElement[] children = element.getChildren();

				for (final IJavaElement child : children)
				{
					final Object[] selectedChild = new Object[] { child };
					processSelection( selectedChild );
				}

			}
		}

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

				insertSelectedElement( selectedMethod );
			}
			else
			{
				final ElementTreeSelectionDialog dialog = DialogConstructor.buildDialog( firstElement );

				final int openStatus = dialog.open();
				if (openStatus == Window.OK)
				{
					final Object[] result = dialog.getResult();

					processSelection( result );
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
