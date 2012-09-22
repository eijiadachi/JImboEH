package br.inf.pucrio.jimboeh.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.ui.statushandlers.StatusManager;

@SuppressWarnings("restriction")
public final class TypedElementSelectionValidatorExtension extends TypedElementSelectionValidator
{
	public TypedElementSelectionValidatorExtension(final Class<?>[] acceptedTypes, final boolean allowMultipleSelection)
	{
		super( acceptedTypes, allowMultipleSelection );
	}

	@Override
	public boolean isSelectedValid(final Object element)
	{
		try
		{
			if (element instanceof IJavaProject)
			{
				final IJavaProject jproject = (IJavaProject) element;
				final IPath path = jproject.getProject().getFullPath();
				final IPackageFragmentRoot findPackageFragmentRoot = jproject.findPackageFragmentRoot( path );
				return (findPackageFragmentRoot != null);
			}
			else if (element instanceof IPackageFragmentRoot)
			{
				final IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) element;
				final int kind = fragmentRoot.getKind();
				return kind == IPackageFragmentRoot.K_SOURCE;
			}

			return true;
		}
		catch (final JavaModelException e)
		{
			StatusManager.getManager().addLoggedStatus( e.getStatus() );
		}

		return false;
	}
}