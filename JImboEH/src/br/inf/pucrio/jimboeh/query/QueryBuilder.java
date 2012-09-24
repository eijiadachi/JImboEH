package br.inf.pucrio.jimboeh.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

		final Set<String> exceptionsToHandle = context.getExceptionsThrown();

		final List<TermQuery> exceptionsHandledTermQueryList = buildTermQueryList( exceptionsToHandle,
				"exceptionsHandled", 5.0f );

		final Set<String> methodsCalled = context.getMethodsCalled();
		final List<TermQuery> methodsCalledTermQueryList = buildTermQueryList( methodsCalled, "methodsCalled", 1.0f );

		final Set<String> variablesTypesUsed = context.getVariablesTypesUsed();

		final List<TermQuery> variablesTypesUsedTermQueryList = buildTermQueryList( variablesTypesUsed,
				"variablesTypesUsed", 1.0f );

		final List<TermQuery> allTermsQueryList = new ArrayList<TermQuery>();
		allTermsQueryList.addAll( exceptionsHandledTermQueryList );
		allTermsQueryList.addAll( methodsCalledTermQueryList );
		allTermsQueryList.addAll( variablesTypesUsedTermQueryList );

		for (final TermQuery termQuery : allTermsQueryList)
		{
			bQuery.add( termQuery, BooleanClause.Occur.SHOULD );
		}

		return bQuery;
	}

	private static <T extends Object> List<TermQuery> buildTermQueryList(final Set<T> termValues,
			final String termName, final Float termBoost)
	{
		final List<TermQuery> termQueryList = new ArrayList<TermQuery>();
		for (final T termValue : termValues)
		{
			final String termValueStr = String.valueOf( termValue );
			final Term term = new Term( termName, termValueStr );
			final TermQuery termQuery = new TermQuery( term );
			termQuery.setBoost( termBoost );
			termQueryList.add( termQuery );
		}
		return termQueryList;
	}
}
