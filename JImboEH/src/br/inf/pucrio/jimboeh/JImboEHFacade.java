package br.inf.pucrio.jimboeh;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class JImboEHFacade
{

	public static List<String> recommend(final MethodDeclaration methodDeclaration)
	{
		final SimpleName name = methodDeclaration.getName();

		final String nameStr = name.toString();

		final List<String> result = new ArrayList<String>();
		for (int i = 0; i < 10; i++)
		{
			result.add( nameStr + i );
		}

		return result;
	}

}
