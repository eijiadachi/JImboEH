package br.inf.pucrio.jimboeh.query;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import br.inf.pucrio.jimboeh.model.MethodContext;

public class QueryBuilder
{
	public static Query buildQuery(final MethodContext context)
	{
		final BooleanQuery bQuery = new BooleanQuery();

		final Map<String, Object> contextDescription = describeBean( context );

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

		final TermQuery query = new TermQuery( new Term( "handles", "IOException" ) );

		return query;
	}

	private static Map<String, Object> describeBean(final Object object)
	{
		final Map<String, Object> description = new TreeMap<String, Object>();

		final Field[] declaredFields = object.getClass().getDeclaredFields();
		for (final Field field : declaredFields)
		{
			try
			{
				final String name = field.getName();

				final String methodName = String.format( "get%s%s", name.substring( 0, 1 ).toUpperCase(),
						name.substring( 1 ) );

				final Method method = MethodContext.class.getMethod( methodName );

				final Object value = method.invoke( object );

				description.put( name, value );

			}
			catch (final IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (final IllegalAccessException e)
			{
				e.printStackTrace();
			}
			catch (final SecurityException e)
			{
				e.printStackTrace();
			}
			catch (final NoSuchMethodException e)
			{
				e.printStackTrace();
			}
			catch (final InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}

		return description;
	}
}
