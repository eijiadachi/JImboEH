package br.inf.pucrio.jimboeh.util;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

public final class UtilIndex
{
	private static Version LUCENE_VERSION = Version.LUCENE_35;

	private static Analyzer ANALYZER = new StandardAnalyzer( LUCENE_VERSION );

	public static IndexReader createIndexReader(final File file) throws CorruptIndexException, IOException
	{
		final FSDirectory directory = FSDirectory.open( file );
		final IndexReader reader = IndexReader.open( directory );
		return reader;
	}

	public static IndexReader createIndexReader(final String path) throws CorruptIndexException, IOException
	{
		final File file = new File( path );
		final IndexReader reader = createIndexReader( file );
		return reader;
	}

	public static IndexSearcher createIndexSearcher(final File file) throws CorruptIndexException, IOException
	{
		final IndexReader reader = createIndexReader( file );
		final IndexSearcher searcher = new IndexSearcher( reader );
		return searcher;
	}

	public static IndexSearcher createIndexSearcher(final String path) throws CorruptIndexException, IOException
	{
		final File file = new File( path );
		final IndexSearcher searcher = createIndexSearcher( file );
		return searcher;
	}

	public static IndexWriter createIndexWriter(final File file) throws CorruptIndexException,
			LockObtainFailedException, IOException
	{
		final FSDirectory directory = FSDirectory.open( file );

		final IndexWriter writer = new IndexWriter( directory, new IndexWriterConfig( LUCENE_VERSION, ANALYZER ) );

		return writer;
	}

	public static IndexWriter createIndexWriter(final String path) throws CorruptIndexException,
			LockObtainFailedException, IOException
	{
		final File file = new File( path );
		final IndexWriter indexWriter = createIndexWriter( file );
		return indexWriter;
	}

	public static TopDocs performSearch(final IndexSearcher searcher, final Query query, final int maxResults)
			throws IOException
	{
		final TopDocs topDocs = searcher.search( query, maxResults );
		return topDocs;
	}

	private UtilIndex()
	{
		super();
	}
}
