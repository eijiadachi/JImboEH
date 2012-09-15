package br.inf.pucrio.jimboeh.views;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.util.UtilUI;

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

	public static final String ID = "jimboeh.views.SearchResultView";

	private TableViewer viewer;

	private Action doubleClickAction;

	private List<String> content;

	public SearchResultView()
	{
	}

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

				try
				{
					final DetailedResultView detailedResultView = UtilUI.getDetailedResultView();

					detailedResultView.setContent( obj.toString() );
				}
				catch (final PartInitException e)
				{
					final IStatus status = e.getStatus();
					final StatusManager statusManager = StatusManager.getManager();
					statusManager.handle( status, StatusManager.SHOW | StatusManager.LOG );
				}
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

	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}
}
