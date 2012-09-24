package br.inf.pucrio.jimboeh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.preferences.PreferenceConstants;
import br.inf.pucrio.jimboeh.query.QueryBuilder;

public class JImboEHFacade
{

	public static List<Document> recommend(final MethodDeclaration methodDeclaration) throws CoreException
	{
		final List<Document> result = new ArrayList<Document>();

		final MethodVisitor visitor = new MethodVisitor();

		methodDeclaration.accept( visitor );

		final MethodContext context = visitor.getContext();
		// TODO usar query

		final BooleanQuery query = QueryBuilder.buildQuery( context );

		// TODO colocar path
		Directory index;
		IndexReader reader;
		TopDocs topDocs;
		IndexSearcher searcher = null;
		final String path = Activator.getDefault().getPreferenceStore().getString( PreferenceConstants.P_PATH );
		final File file = new File( path );
		try
		{
			index = FSDirectory.open( file );
			reader = IndexReader.open( index );
			searcher = new IndexSearcher( reader );
			topDocs = searcher.search( new TermQuery( new Term( "handles", "IOException" ) ), 10 );
			final ScoreDoc[] docs = topDocs.scoreDocs;
			if (docs != null && docs.length == 0)
			{
				MessageDialog.openInformation( null, "JImboEH", "No results found." );
			}
			for (final ScoreDoc doc : docs)
			{
				final int id = doc.doc;
				final Document d = searcher.doc( id );
				result.add( d );
			}
		}
		catch (final IndexNotFoundException e)
		{
			final String message = String
					.format(
							"No valid index found on '%s'. Do you wish to create a new index on this path? You can change the path on the JImboEH preferences page.",
							path );
			final boolean openQuestionStatus = MessageDialog.openQuestion( null, "JImboEH", message );
			if (openQuestionStatus)
			{
				try
				{
					index = FSDirectory.open( file );
					final IndexWriter writer = new IndexWriter( index, new IndexWriterConfig( Version.LUCENE_35,
							new StandardAnalyzer( Version.LUCENE_35 ) ) );

					writer.close();
				}
				catch (final IOException e1)
				{
					final String message2 = String.format(
							"Could not create an index on '%s'. Go to Preferences > JImboEH and set a new path.", path );
					throw new CoreException( new Status( IStatus.ERROR, Activator.PLUGIN_ID, message2, e ) );
				}

			}
		}
		catch (final IOException e)
		{
			throw new CoreException( new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage() ) );
		}
		finally
		{
			if (searcher != null)
			{
				try
				{
					searcher.close();
				}
				catch (final IOException e)
				{
					throw new CoreException( new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage() ) );
				}
			}
		}

		return result;
	}
}
