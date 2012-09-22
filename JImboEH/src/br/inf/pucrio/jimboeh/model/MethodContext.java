package br.inf.pucrio.jimboeh.model;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class MethodContext
{
	private Set<String> exceptionsHandled;

	private Set<String> exceptionsOnInterface;

	private Set<String> exceptionsThrown;

	private Set<String> methodsCalled;

	private Set<String> variablesTypesUsed;

	private Set<String> variablesIdentifiersUsed;

	private Set<String> parametersTypes;

	private Set<String> parametersIdentifiers;

	private String methodName;

	private String returnedType;

	private String enclosingClass;

	private String enclosingProject;

	public MethodContext()
	{
		this.setExceptionsHandled( new LinkedHashSet<String>() );
		this.setExceptionsOnInterface( new LinkedHashSet<String>() );
		this.setExceptionsThrown( new LinkedHashSet<String>() );
		this.setMethodsCalled( new LinkedHashSet<String>() );
		this.setVariablesTypesUsed( new LinkedHashSet<String>() );
		this.setVariablesIdentifiersUsed( new LinkedHashSet<String>() );
		this.setParametersTypes( new LinkedHashSet<String>() );
		this.setParametersIdentifiers( new LinkedHashSet<String>() );
	}

	public void addExceptionHandled(final SingleVariableDeclaration exceptionDeclaration)
	{
		final IVariableBinding binding = exceptionDeclaration.resolveBinding();
		final String qualifiedName = getQualifiedName( binding );
		this.addExceptionHandled( qualifiedName );

	}

	private void addExceptionHandled(final String exceptionStr)
	{
		this.getExceptionsHandled().add( exceptionStr );

	}

	public void addExceptionOnInterface(final String declaredException)
	{
		this.getExceptionsOnInterface().add( declaredException );
	}

	public void addExceptionThrown(final String exceptionThrown)
	{
		this.getExceptionsThrown().add( exceptionThrown );
	}

	public void addMethodCalled(final String qualifiedName)
	{
		this.getMethodsCalled().add( qualifiedName );
	}

	public void addMethodName(final String fullyQualifiedName)
	{
		this.setMethodName( fullyQualifiedName );

	}

	private void addParameterIdentifier(final SimpleName parameterIdentifier)
	{
		final String fullyQualifiedName = getQualifiedName( parameterIdentifier );
		this.addParameterIdentifier( fullyQualifiedName );
	}

	private void addParameterIdentifier(final String fullyQualifiedName)
	{
		this.getParametersIdentifiers().add( fullyQualifiedName );
	}

	public void addParameters(final List<SingleVariableDeclaration> parameters)
	{
		for (final SingleVariableDeclaration parameter : parameters)
		{
			final SimpleName parameterIdentifier = parameter.getName();
			this.addParameterIdentifier( parameterIdentifier );

			final Type parameterType = parameter.getType();
			this.addParameterType( parameterType );
		}

	}

	private void addParameterType(final Type parameterType)
	{
		final String parameterTypeStr = getQualifiedName( parameterType );
		this.getParametersTypes().add( parameterTypeStr );
	}

	public void addReturnedType(final Type returnType)
	{
		final String returnedType = getQualifiedName( returnType );
		this.setReturnedType( returnedType );

	}

	public void addVariableIdentifierUsed(final SimpleName variableIdentifier)
	{
		final String fullyQualifiedName = variableIdentifier.getFullyQualifiedName();
		this.getVariablesIdentifiersUsed().add( fullyQualifiedName );
	}

	public void addVariablesUsed(final List<Expression> arguments)
	{
		for (final Expression argument : arguments)
		{
			final ITypeBinding typeBinding = argument.resolveTypeBinding();
			final String qualifiedName;
			if (typeBinding == null)
			{
				qualifiedName = argument.toString();
			}
			else
			{
				qualifiedName = typeBinding.getQualifiedName();
			}
			this.addVariableTypeUsed( qualifiedName );
		}

	}

	public void addVariableTypeUsed(final String variableStr)
	{
		this.getVariablesTypesUsed().add( variableStr );
	}

	public void addVariableTypeUsed(final Type variable)
	{
		final String variableStr = getQualifiedName( variable );
		this.addVariableTypeUsed( variableStr );
	}

	public String getEnclosingClass()
	{
		return enclosingClass;
	}

	public String getEnclosingProject()
	{
		return enclosingProject;
	}

	public Set<String> getExceptionsHandled()
	{
		return exceptionsHandled;
	}

	public Set<String> getExceptionsOnInterface()
	{
		return exceptionsOnInterface;
	}

	public Set<String> getExceptionsThrown()
	{
		return exceptionsThrown;
	}

	public String getMethodName()
	{
		return methodName;
	}

	public Set<String> getMethodsCalled()
	{
		return methodsCalled;
	}

	public Set<String> getParametersIdentifiers()
	{
		return parametersIdentifiers;
	}

	public Set<String> getParametersTypes()
	{
		return parametersTypes;
	}

	private String getQualifiedName(final IVariableBinding binding)
	{
		final ITypeBinding typeBinding = binding.getType();
		final String qualifiedName = typeBinding.getQualifiedName();
		return qualifiedName;
	}

	private String getQualifiedName(final SimpleName simpleName)
	{
		final IBinding binding = simpleName.resolveBinding();
		final String fullyQualifiedName = binding.getName();
		return fullyQualifiedName;
	}

	private String getQualifiedName(final Type type)
	{
		final ITypeBinding binding = type.resolveBinding();
		if (binding == null)
		{
			return type.toString();
		}
		final String qualifiedName = binding.getQualifiedName();
		return qualifiedName;
	}

	public String getReturnedType()
	{
		return returnedType;
	}

	public Set<String> getVariablesIdentifiersUsed()
	{
		return variablesIdentifiersUsed;
	}

	public Set<String> getVariablesTypesUsed()
	{
		return variablesTypesUsed;
	}

	public void setEnclosingClass(final String enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	public void setEnclosingProject(final String enclosingProject)
	{
		this.enclosingProject = enclosingProject;
	}

	public void setExceptionsHandled(final Set<String> exceptionsHandled)
	{
		this.exceptionsHandled = exceptionsHandled;
	}

	public void setExceptionsOnInterface(final Set<String> exceptionsOnInterface)
	{
		this.exceptionsOnInterface = exceptionsOnInterface;
	}

	public void setExceptionsThrown(final Set<String> exceptionsThrown)
	{
		this.exceptionsThrown = exceptionsThrown;
	}

	public void setMethodName(final String methodName)
	{
		this.methodName = methodName;
	}

	public void setMethodsCalled(final Set<String> methodsCalled)
	{
		this.methodsCalled = methodsCalled;
	}

	public void setParametersIdentifiers(final Set<String> parametersIdentifiers)
	{
		this.parametersIdentifiers = parametersIdentifiers;
	}

	public void setParametersTypes(final Set<String> parametersTypes)
	{
		this.parametersTypes = parametersTypes;
	}

	public void setReturnedType(final String returnedType)
	{
		this.returnedType = returnedType;
	}

	public void setVariablesIdentifiersUsed(final Set<String> variablesIdentifiersUsed)
	{
		this.variablesIdentifiersUsed = variablesIdentifiersUsed;
	}

	public void setVariablesTypesUsed(final Set<String> variablesTypesUsed)
	{
		this.variablesTypesUsed = variablesTypesUsed;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final Field[] declaredFields = MethodContext.class.getDeclaredFields();
		for (final Field field : declaredFields)
		{
			try
			{
				final String fieldName = field.getName();
				final Object fieldValue = field.get( this );
				final String str = String.format( "%s - %s\n", fieldName, fieldValue );
				builder.append( str );
			}
			catch (final IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (final IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return builder.toString();
	}
}
