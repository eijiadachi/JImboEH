package br.inf.pucrio.jimboeh.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.preferences.PreferenceConstants;
import br.inf.pucrio.jimboeh.ui.DialogConstructor;
import br.inf.pucrio.jimboeh.util.UtilIndex;

public class InsertAction implements IObjectActionDelegate
{

	private ISelection selection;

	private final Comparator<IMethod> comparator = new Comparator<IMethod>()
	{

		private String buildFullyQualifiedName(final IMethod arg0)
		{
			final String typeFullyQualifiedName1 = arg0.getDeclaringType().getFullyQualifiedName();
			final String elementName1 = arg0.getElementName();
			final String elementQualifiedName1 = String.format( "%s.%s", typeFullyQualifiedName1, elementName1 );
			return elementQualifiedName1;
		}

		// TODO ver como nao inserir esse cara, pois ele nao trata
		@Override
		public int compare(final IMethod arg0, final IMethod arg1)
		{
			final String qualifiedName0 = buildFullyQualifiedName( arg0 );
			final String qualifiedName1 = buildFullyQualifiedName( arg1 );
			return qualifiedName0.compareTo( qualifiedName1 );
		}
	};

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
			final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			final String path = preferenceStore.getString( PreferenceConstants.P_PATH );
			final IndexWriter writer = UtilIndex.createIndexWriter( path );

			final StructuredSelection structuredSelection = (StructuredSelection) selection;

			final Object firstElement = structuredSelection.getFirstElement();

			final Set<IMethod> methodsToIndex = new TreeSet<IMethod>( comparator );

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

			final InsertActionRunnable runnable = new InsertActionRunnable( methodsToIndex, writer );

			progressMonitorDialog.run( true, true, runnable );

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
		catch (final CorruptIndexException e)
		{
			final IStatus status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
			final StatusManager statusManager = StatusManager.getManager();
			statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
		}
		catch (final LockObtainFailedException e)
		{
			final IStatus status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
			final StatusManager statusManager = StatusManager.getManager();
			statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
		}
		catch (final IOException e)
		{
			final IStatus status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
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
		// There is nothing to do
	}

}
