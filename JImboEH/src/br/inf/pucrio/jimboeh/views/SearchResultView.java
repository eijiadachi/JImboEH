package br.inf.pucrio.jimboeh.views;

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
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
			final Document doc = (Document) obj;
			final String methodName = doc.get( "methodName" );
			final String[] exceptions = doc.getValues( "exceptionsHandled" );
			final List<String> exceptionsList = Arrays.asList( exceptions );

			final String str = String.format( "%s handles %s", methodName, exceptionsList );

			return str;
		}

		@Override
		public Image getImage(final Object obj)
		{
			return PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE );
		}
	}

	public static final String ID = "jimboeh.views.SearchResultView";

	private TableViewer viewer;

	private Action doubleClickAction;

	private List<Document> content;

	public SearchResultView()
	{
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		viewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		viewer.setContentProvider( ArrayContentProvider.getInstance() );
		viewer.setLabelProvider( new ViewLabelProvider() );
		viewer.setSorter( new NameSorter() );
		if (content != null)
		{
			viewer.setInput( content );
		}
		else
		{
			viewer.setInput( getViewSite() );
		}

		final Table table = viewer.getTable();
		table.setHeaderVisible( true );
		table.setLinesVisible( true );

		final TableViewerColumn col1 = new TableViewerColumn( viewer, SWT.NONE );
		col1.getColumn().setWidth( 200 );
		col1.getColumn().setText( "Method Name:" );
		col1.setLabelProvider( new ColumnLabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				final Document d = (Document) element;
				final String enclosingClass = d.get( "enclosingClass" );
				final String methodName = d.get( "methodName" );

				if (enclosingClass == null || enclosingClass.isEmpty())
				{
					return methodName;
				}

				return String.format( "%s.%s", enclosingClass, methodName );
			}
		} );

		final TableViewerColumn col2 = new TableViewerColumn( viewer, SWT.NONE );
		col2.getColumn().setWidth( 300 );
		col2.getColumn().setText( "Exceptions Handled:" );
		col2.setLabelProvider( new ColumnLabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				final Document d = (Document) element;
				final String[] exceptions = d.getValues( "exceptionsHandled" );
				final List<String> exceptionsList = Arrays.asList( exceptions );
				return exceptionsList.toString();
			}
		} );

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
				final Document doc = (Document) obj;

				try
				{
					final CodeSnippetView detailedResultView = UtilUI.getDetailedResultView();

					final String str = doc.get( "codeSnippet" );

					detailedResultView.setContent( str );
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

	public void setContent(final List<Document> content)
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
