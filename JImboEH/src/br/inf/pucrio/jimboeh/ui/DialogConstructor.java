package br.inf.pucrio.jimboeh.ui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import br.inf.pucrio.jimboeh.Activator;

public final class DialogConstructor
{
	final static ViewerFilter filter = new ViewerFilter()
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
			else if (element instanceof IJavaProject || element instanceof ICompilationUnit
					|| element instanceof IPackageFragment)
			{
				return true;
			}

			return false;
		}
	};

	public static ElementTreeSelectionDialog buildDialog(final Object element) throws CoreException
	{
		ElementTreeSelectionDialog dialog;
		if (element instanceof IProject)
		{
			final IJavaModel root = JavaCore.create( ResourcesPlugin.getWorkspace().getRoot() );

			final IProject project = (IProject) element;
			final String projectName = project.getName();

			dialog = DialogConstructor.buildDialogForProjectInsertion( root, projectName );

		}
		else if (element instanceof ICompilationUnit)
		{
			final ICompilationUnit compilationUnit = (ICompilationUnit) element;

			final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			final StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider( true );

			final ILabelProvider labelProvider = new JavaElementLabelProvider( JavaElementLabelProvider.SHOW_TYPE );

			dialog = new ElementTreeSelectionDialog( shell, labelProvider, provider );

			final Class<?>[] acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class,
					IJavaProject.class, IFolder.class, ICompilationUnit.class, IMethod.class, IType.class };

			final ISelectionStatusValidator validator = new TypedElementSelectionValidatorExtension( acceptedClasses,
					true );

			final ViewerFilter filter = new ViewerFilter()
			{
				@Override
				public boolean select(final Viewer viewer, final Object parent, final Object element)
				{
					if (element instanceof IJavaProject)
					{
						return ((IJavaProject) element).getProject().getName()
								.equals( compilationUnit.getJavaProject().getProject().getName() );
					}
					else if (element instanceof IPackageFragmentRoot)
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
					else if (element instanceof ICompilationUnit)
					{
						final ICompilationUnit elementCompilationUnit = (ICompilationUnit) element;

						final String elementName = elementCompilationUnit.getElementName();
						final String elementName2 = compilationUnit.getElementName();
						return elementName.equals( elementName2 );
					}
					else if (element instanceof IPackageFragment || element instanceof IMethod
							|| element instanceof IType)
					{
						return true;
					}

					return false;
				}
			};

			dialog.setValidator( validator );
			dialog.setTitle( "JImboEH" );
			dialog.setMessage( "Choose the elements to insert" );
			dialog.setInput( compilationUnit.getParent() );
			dialog.addFilter( filter );
			dialog.setHelpAvailable( false );
			dialog.setAllowMultiple( true );
		}
		else
		{
			final String message = String.format( "The type '%s' is not supported by JImboEH.", element.getClass() );
			throw new CoreException( new Status( IStatus.INFO, Activator.PLUGIN_ID, message ) );
		}
		return dialog;
	}

	public static ElementTreeSelectionDialog buildDialogForCompilationUnitInsertion(
			final ICompilationUnit compilationUnit)
	{

		final Class<?>[] acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class,
				IJavaProject.class, IFolder.class, ICompilationUnit.class, IMethod.class, IType.class };

		final ElementTreeSelectionDialog dialog = buildTreeSelectionDialog( acceptedClasses,
				compilationUnit.getParent(), true );

		final ViewerFilter filter = new ViewerFilter()
		{
			@Override
			public boolean select(final Viewer viewer, final Object parent, final Object element)
			{
				if (element instanceof IJavaProject)
				{
					return ((IJavaProject) element).getProject().getName()
							.equals( compilationUnit.getJavaProject().getProject().getName() );
				}
				else if (element instanceof IPackageFragmentRoot)
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
				else if (element instanceof ICompilationUnit)
				{
					final ICompilationUnit elementCompilationUnit = (ICompilationUnit) element;

					final String elementName = elementCompilationUnit.getElementName();
					final String elementName2 = compilationUnit.getElementName();
					return elementName.equals( elementName2 );
				}
				else if (element instanceof IPackageFragment || element instanceof IMethod || element instanceof IType)
				{
					return true;
				}

				return false;
			}
		};

		dialog.addFilter( filter );

		return dialog;
	}

	public static ElementTreeSelectionDialog buildDialogForProjectInsertion(final Object inputElement,
			final String projectName)
	{
		final Class<?>[] acceptedClasses = new Class[] { IPackageFragmentRoot.class, IJavaProject.class, IFolder.class,
				ICompilationUnit.class, IPackageFragment.class, IProject.class };

		final ElementTreeSelectionDialog dialog = buildTreeSelectionDialog( acceptedClasses, inputElement, false );

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
					final IJavaProject project = (IJavaProject) element;
					final String elementName = project.getElementName();
					return projectName.equals( elementName );
				}
				else if (element instanceof ICompilationUnit || element instanceof IPackageFragment)
				{
					return true;
				}

				return false;
			}
		};

		dialog.addFilter( filter );

		return dialog;
	}

	private static ElementTreeSelectionDialog buildTreeSelectionDialog(final Class<?>[] acceptedClasses,
			final Object element, final boolean showBelowCompilationUnit)
	{
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		final ILabelProvider labelProvider = new JavaElementLabelProvider( JavaElementLabelProvider.SHOW_DEFAULT );

		final StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider(
				showBelowCompilationUnit );

		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog( shell, labelProvider, provider );

		final ISelectionStatusValidator validator = new TypedElementSelectionValidatorExtension( acceptedClasses, true );

		dialog.setTitle( "JImboEH" );
		dialog.setMessage( "Choose the elements to insert" );
		dialog.addFilter( filter );
		dialog.setHelpAvailable( false );
		dialog.setAllowMultiple( true );
		dialog.setValidator( validator );
		dialog.setInput( element );

		return dialog;
	}

}
