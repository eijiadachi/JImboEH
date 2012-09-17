package br.inf.pucrio.jimboeh;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
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
		try
		{
			index = FSDirectory.open( new File( "/Users/eijiadachi/git/EH-Recommender/EH-Recommender/index" ) );
			reader = IndexReader.open( index );
			searcher = new IndexSearcher( reader );
			topDocs = searcher.search( new TermQuery( new Term( "handles", "IOException" ) ), 10 );
			final ScoreDoc[] docs = topDocs.scoreDocs;
			for (final ScoreDoc doc : docs)
			{
				final int id = doc.doc;
				final Document d = searcher.doc( id );
				result.add( d );
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
