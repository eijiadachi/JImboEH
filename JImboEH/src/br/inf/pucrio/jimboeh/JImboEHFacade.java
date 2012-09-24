package br.inf.pucrio.jimboeh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.preferences.PreferenceConstants;
import br.inf.pucrio.jimboeh.query.QueryBuilder;
import br.inf.pucrio.jimboeh.util.UtilIndex;

public class JImboEHFacade
{

	public static List<Document> recommend(final MethodDeclaration methodDeclaration) throws CoreException
	{
		final List<Document> result = new ArrayList<Document>();

		final MethodVisitor visitor = new MethodVisitor();

		methodDeclaration.accept( visitor );

		final MethodContext context = visitor.getContext();

		final Query query = QueryBuilder.buildQuery( context );

		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		final String path = preferenceStore.getString( PreferenceConstants.P_PATH );
		final int maxResults = preferenceStore.getInt( PreferenceConstants.P_INTEGER );

		TopDocs topDocs;
		IndexSearcher searcher = null;

		try
		{
			searcher = UtilIndex.createIndexSearcher( path );

			topDocs = UtilIndex.performSearch( searcher, query, maxResults );

			final ScoreDoc[] docs = topDocs.scoreDocs;

			if (docs != null && docs.length == 0)
			{
				MessageDialog.openInformation( null, "JImboEH", "No results found." );
			}
			else
			{
				for (final ScoreDoc doc : docs)
				{
					final int id = doc.doc;
					final Document d = searcher.doc( id );
					result.add( d );
				}
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
					final IndexWriter writer = UtilIndex.createIndexWriter( path );
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
