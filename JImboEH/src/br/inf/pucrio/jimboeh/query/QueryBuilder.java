package br.inf.pucrio.jimboeh.query;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.util.UtilBean;

public class QueryBuilder
{
	public static Query buildQuery(final MethodContext context)
	{
		final BooleanQuery bQuery = new BooleanQuery();

		final Map<String, Object> contextDescription = UtilBean.describeBean( context );

		contextDescription.remove( "codeSnippet" );

		final Set<Entry<String, Object>> entrySet = contextDescription.entrySet();
		for (final Entry<String, Object> entry : entrySet)
		{
			final String name = entry.getKey();
			final Object value = entry.getValue();

			if (value instanceof Iterable)
			{
				final Iterable<?> iterable = (Iterable<?>) value;
				for (final Object object : iterable)
				{
					final String objectStr = String.valueOf( object );
					final Term term = new Term( name, objectStr );
					final TermQuery termQuery = new TermQuery( term );
					bQuery.add( termQuery, BooleanClause.Occur.SHOULD );
				}
			}
			else
			{
				final String valueStr = String.valueOf( value );
				final Term term = new Term( name, valueStr );
				final TermQuery termQuery = new TermQuery( term );
				bQuery.add( termQuery, BooleanClause.Occur.SHOULD );
			}
		}

		return bQuery;
	}
}
