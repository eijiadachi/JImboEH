package br.inf.pucrio.jimboeh.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import br.inf.pucrio.jimboeh.model.MethodContext;

public final class UtilBean
{
	public static Map<String, Object> describeBean(final Object object)
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

	private UtilBean()
	{
		super();
	}
}
