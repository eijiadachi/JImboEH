package br.inf.pucrio.jimboeh.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.ui.DialogConstructor;
import br.inf.pucrio.jimboeh.util.UtilAST;

public class InsertAction implements IObjectActionDelegate
{

	private ISelection selection;

	private void insertSelectedElements(final Set<IMethod> methodsToIndex, final IProgressMonitor monitor)
			throws JavaModelException
	{
		monitor.beginTask( "Indexing methods", methodsToIndex.size() );

		for (final IMethod method : methodsToIndex)
		{
			final MethodDeclaration methodNode = UtilAST.astNode( method );

			if (methodNode == null)
			{
				UtilAST.astNode( method );
				continue;
			}

			final MethodVisitor visitor = new MethodVisitor();

			methodNode.accept( visitor );

			final MethodContext context = visitor.getContext();

			final String contextStr = context.toString();

			final String methodName = method.getElementName();

			final String message = String.format( "Inserting method: %s\n\n%s", methodName, contextStr );

			monitor.worked( 1 );
		}

		final String message = String.format( "Inserting %s methods.\n", methodsToIndex.size() );

		monitor.done();

		MessageDialog.openInformation( null, "JImboEH", message );
	}

	private void processSelection(final Object[] selectedElements, final Set<IMethod> methodsToIndex)
			throws JavaModelException
	{
		for (final Object object : selectedElements)
		{
			if (object instanceof IMethod)
			{
				final IMethod method = (IMethod) object;
				methodsToIndex.add( method );
			}
			else if (object instanceof IParent)
			{
				final IParent element = (IParent) object;
				final IJavaElement[] children = element.getChildren();

				for (final IJavaElement child : children)
				{
					final Object[] selectedChild = new Object[] { child };
					processSelection( selectedChild, methodsToIndex );
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

			final Set<IMethod> methodsToIndex = new TreeSet<IMethod>( new Comparator<IMethod>()
			{

				private String buildFullyQualifiedName(final IMethod arg0)
				{
					final String typeFullyQualifiedName1 = arg0.getDeclaringType().getFullyQualifiedName();
					final String elementName1 = arg0.getElementName();
					final String elementQualifiedName1 = String.format( "%s.%s", typeFullyQualifiedName1, elementName1 );
					return elementQualifiedName1;
				}

				@Override
				public int compare(final IMethod arg0, final IMethod arg1)
				{
					final String qualifiedName0 = buildFullyQualifiedName( arg0 );
					final String qualifiedName1 = buildFullyQualifiedName( arg1 );
					return qualifiedName0.compareTo( qualifiedName1 );
				}
			} );

			if (firstElement instanceof IMethod)
			{
				final IMethod selectedMethod = (IMethod) firstElement;

				methodsToIndex.add( selectedMethod );
			}
			else
			{
				final ElementTreeSelectionDialog dialog = DialogConstructor.buildDialog( firstElement );

				final int openStatus = dialog.open();
				if (openStatus == Window.OK)
				{
					final Object[] result = dialog.getResult();

					processSelection( result, methodsToIndex );
				}

				dialog.close();
			}

			final ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog( PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell() );

			progressMonitorDialog.run( true, true, new InsertActionRunnable( methodsToIndex ) );

			final String message = String.format( "Inserted %s methods.\n", methodsToIndex.size() );

			MessageDialog.openInformation( null, "JImboEH", message );
		}
		catch (final CoreException e)
		{
			final IStatus status = e.getStatus();
			final StatusManager statusManager = StatusManager.getManager();
			statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
		}
		catch (final InvocationTargetException e)
		{
			final ILog log = Activator.getDefault().getLog();
			log.log( new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage() ) );
		}
		catch (final InterruptedException e)
		{
			final ILog log = Activator.getDefault().getLog();
			log.log( new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage() ) );
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
