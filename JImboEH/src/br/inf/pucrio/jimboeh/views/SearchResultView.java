package br.inf.pucrio.jimboeh.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SearchResultView extends ViewPart
{

	class NameSorter extends ViewerSorter
	{
	}

	class ViewContentProvider implements IStructuredContentProvider
	{
		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getElements(final Object parent)
		{
			if (content != null)
			{
				return content.toArray();
			}
			else
			{
				return new String[] {};
			}
		}

		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput)
		{
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		@Override
		public Image getColumnImage(final Object obj, final int index)
		{
			return getImage( obj );
		}

		@Override
		public String getColumnText(final Object obj, final int index)
		{
			final String baseStr = "public static void %s(String args){\n" + "doThis();" + "}";

			final String str = String.format( baseStr, obj );

			return str;
		}

		@Override
		public Image getImage(final Object obj)
		{
			return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_INFO_TSK );
		}
	}

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "jimboeh.views.SearchResultView";
	private TableViewer viewer;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	private Action doubleClickAction;
	private List<String> content;

	/**
	 * The constructor.
	 */
	public SearchResultView()
	{
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		viewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		viewer.setContentProvider( new ViewContentProvider() );
		viewer.setLabelProvider( new ViewLabelProvider() );
		viewer.setSorter( new NameSorter() );
		viewer.setInput( getViewSite() );

		makeActions();
		hookDoubleClickAction();
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener( new IDoubleClickListener()
		{
			@Override
			public void doubleClick(final DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		} );
	}

	private void makeActions()
	{
		doubleClickAction = new Action()
		{
			@Override
			public void run()
			{
				final ISelection selection = viewer.getSelection();
				final Object obj = ((IStructuredSelection) selection).getFirstElement();
				showMessage( "Double-click detected on " + obj.toString() );
			}
		};
	}

	public void setContent(final List<String> content)
	{
		final Control control = viewer.getControl();

		control.setRedraw( false );
		viewer.setInput( null );
		viewer.refresh();
		control.setRedraw( true );

		this.content = content;
		viewer.setInput( content );

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	private void showMessage(final String message)
	{
		MessageDialog.openInformation( viewer.getControl().getShell(), "Search Result View", message );
	}
}