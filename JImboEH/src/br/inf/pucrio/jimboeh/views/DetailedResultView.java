package br.inf.pucrio.jimboeh.views;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("restriction")
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

	private SourceViewer viewer;

	private String content;

	public DetailedResultView()
	{
	}

	private JavaSourceViewerConfiguration buildViewerConfiguration()
	{
		final JavaPlugin plugin = JavaPlugin.getDefault();

		final JavaTextTools javaTextTools = plugin.getJavaTextTools();

		final IPreferenceStore combinedPreferenceStore = plugin.getCombinedPreferenceStore();

		final JavaSourceViewerConfiguration configuration = new JavaSourceViewerConfiguration(
				javaTextTools.getColorManager(), combinedPreferenceStore, null, null );
		return configuration;
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
		final JavaSourceViewerConfiguration configuration = buildViewerConfiguration();
		final Font font = JFaceResources.getFont( JFaceResources.TEXT_FONT );

		viewer = new SourceViewer( container, null, SWT.BORDER | SWT.V_SCROLL );
		viewer.configure( configuration );
		viewer.setEditable( false );
		viewer.getTextWidget().setWordWrap( false );
		viewer.getTextWidget().setFont( font );

		final Control viewerControl = viewer.getControl();
		viewerControl.setLayoutData( GridDataFactory.fillDefaults().grab( true, true ).hint( 200, 300 ).create() );
	}

	public void setContent(final String content)
	{
		final Document doc = new Document();
		doc.set( content );

		viewer.setDocument( doc );
	}

	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}
}