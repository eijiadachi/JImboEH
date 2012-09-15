package br.inf.pucrio.jimboeh.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class DetailedResultView extends ViewPart
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
				return new Object[] { content };
			}
			else
			{
				return new Object[] {};
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
			final String baseStr = "public" + x++;

			final String str = String.format( baseStr, obj );

			return str;
		}

		@Override
		public Image getImage(final Object obj)
		{
			return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_INFO_TSK );
		}
	}

	static int x = 0;

	private ScrolledComposite container;

	public static final String ID = "jimboeh.views.DetailedResultView";

	private TableViewer viewer;

	private String content;

	public DetailedResultView()
	{
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		container = new ScrolledComposite( parent, SWT.H_SCROLL | SWT.V_SCROLL );
		createSourceViewer( parent );
		container.setContent( viewer.getControl() );
		container.setExpandHorizontal( true );
		container.setExpandVertical( true );

	}

	private void createSourceViewer(final Composite parent)
	{
		viewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		viewer.setContentProvider( new ViewContentProvider() );
		viewer.setLabelProvider( new ViewLabelProvider() );
		viewer.setSorter( new NameSorter() );
		viewer.setInput( getViewSite() );
	}

	public void setContent(final String content)
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