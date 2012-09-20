package br.inf.pucrio.jimboeh.actions;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

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

			if (firstElement instanceof IMethod)
			{
				final IMethod selectedMethod = (IMethod) firstElement;

				final MethodDeclaration methodNode = UtilAST.astNode( selectedMethod );

				final MethodVisitor visitor = new MethodVisitor();

				methodNode.accept( visitor );

				final MethodContext context = visitor.getContext();

				MessageDialog.openInformation( null, "JImboEH", context.toString() );
			}
			else if (firstElement instanceof IProject)
			{
				final IProject project = (IProject) firstElement;

				final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				final StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
				final ILabelProvider labelProvider = new JavaElementLabelProvider(
						JavaElementLabelProvider.SHOW_DEFAULT );
				final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog( shell, labelProvider,
						provider );

				Class[] acceptedClasses = new Class[] { IPackageFragmentRoot.class, IJavaProject.class, IFolder.class,
						ICompilationUnit.class, IPackageFragment.class };
				final TypedElementSelectionValidator validator = new TypedElementSelectionValidator( acceptedClasses,
						true )
				{
					@Override
					public boolean isSelectedValid(final Object element)
					{
						try
						{
							if (element instanceof IJavaProject)
							{
								final IJavaProject jproject = (IJavaProject) element;
								final IPath path = jproject.getProject().getFullPath();
								final IPackageFragmentRoot findPackageFragmentRoot = jproject
										.findPackageFragmentRoot( path );
								return (findPackageFragmentRoot != null);
							}
							else if (element instanceof IPackageFragmentRoot)
							{
								final IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) element;
								final int kind = fragmentRoot.getKind();
								return kind == IPackageFragmentRoot.K_SOURCE;
							}
							else
							{
								return true;
							}
						}
						catch (final JavaModelException e)
						{
							JavaPlugin.log( e.getStatus() );
						}
						return false;
					}
				};

				acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class,
						IFolder.class };
				final ViewerFilter filter = new ViewerFilter()
				{
					@Override
					public boolean select(final Viewer viewer, final Object parent, final Object element)
					{
						if (element instanceof IPackageFragmentRoot)
						{
							try
							{
								return (((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE);
							}
							catch (final JavaModelException e)
							{
								return false;
							}
						}
						else if (element instanceof IJavaProject)
						{
							return ((IJavaProject) element).getProject().getName().equals( project.getName() );
						}
						else if (element instanceof ICompilationUnit || element instanceof IPackageFragment)
						{
							return true;
						}

						return false;
					}
				};

				dialog.setSorter( new JavaElementSorter() );
				dialog.setValidator( validator );
				dialog.setTitle( "" );
				dialog.setMessage( "" );
				dialog.setInput( JavaCore.create( ResourcesPlugin.getWorkspace().getRoot() ) );
				dialog.addFilter( filter );
				dialog.setHelpAvailable( false );
				dialog.setAllowMultiple( true );

				dialog.open();
			}
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
