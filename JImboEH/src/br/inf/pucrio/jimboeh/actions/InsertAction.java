package br.inf.pucrio.jimboeh.actions;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.util.UtilAST;

public class InsertAction implements IObjectActionDelegate
{

	private ISelection selection;

	@Override
	public void run(final IAction action)
	{
		try
		{
			final StructuredSelection structuredSelection = (StructuredSelection) selection;

			final Object firstElement = structuredSelection.getFirstElement();

			final IMethod selectedMethod = (IMethod) firstElement;

			final MethodDeclaration methodNode = UtilAST.astNode( selectedMethod );

			final MethodVisitor visitor = new MethodVisitor();

			methodNode.accept( visitor );

			final MethodContext context = visitor.getContext();

			MessageDialog.openInformation( null, "JImboEH", context.toString() );
		}
		catch (final JavaModelException e)
		{
			e.printStackTrace();
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
