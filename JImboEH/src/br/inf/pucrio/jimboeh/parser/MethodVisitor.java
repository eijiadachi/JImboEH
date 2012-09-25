package br.inf.pucrio.jimboeh.parser;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import br.inf.pucrio.jimboeh.model.MethodContext;

public class MethodVisitor extends ASTVisitor
{
	private final MethodContext context;

	private final boolean ignoreTrivialHandlers;

	private boolean implementsTrivialHandler;

	public MethodVisitor()
	{
		this( false );
	}

	public MethodVisitor(final boolean b)
	{
		ignoreTrivialHandlers = b;
		context = new MethodContext();
	}

	private boolean compareFullyQualifiedName(final MethodInvocation methodInvocation, final String methodNameStr)
	{
		final SimpleName methodName = methodInvocation.getName();

		final String fullyQualifiedName = methodName.getFullyQualifiedName();

		final boolean isEqual = fullyQualifiedName.equals( methodNameStr );

		return isEqual;

	}

	public MethodContext getContext()
	{
		return context;
	}

	private boolean isAssertion(final MethodInvocation methodInvocation)
	{
		final SimpleName methodName = methodInvocation.getName();

		final String fullyQualifiedName = methodName.getFullyQualifiedName();

		final boolean isAssertion = fullyQualifiedName.startsWith( "assert" );

		return isAssertion;
	}

	private boolean isEmptyCatch(final CatchClause node)
	{
		final Block body = node.getBody();
		final List<?> statements = body.statements();
		final boolean isEmptyCatch = statements.isEmpty();

		return isEmptyCatch;
	}

	private boolean isFail(final MethodInvocation methodInvocation)
	{
		final boolean isFail = compareFullyQualifiedName( methodInvocation, "fail" );

		return isFail;
	}

	private boolean isPrintln(final MethodInvocation methodInvocation)
	{
		final Expression expression = methodInvocation.getExpression();

		if (expression != null && expression instanceof QualifiedName)
		{
			final String expressionName = ((QualifiedName) expression).getFullyQualifiedName();

			final boolean isPrintln = (expressionName.equals( "System.out" ) || expressionName.equals( "System.err" ))
					&& compareFullyQualifiedName( methodInvocation, "println" );

			return isPrintln;
		}

		return false;
	}

	private boolean isPrintStackTrace(final MethodInvocation methodInvocation)
	{
		final boolean isPrintStackTrace = compareFullyQualifiedName( methodInvocation, "printStackTrace" );

		return isPrintStackTrace;
	}

	public boolean isTrivialHandler()
	{
		return implementsTrivialHandler;
	}

	private boolean isTrivialHandler(final CatchClause node)
	{
		final Block body = node.getBody();
		final List<?> statements = body.statements();

		boolean isTrivial = true;
		for (final Object statement : statements)
		{
			if (statement instanceof ExpressionStatement)
			{
				final ExpressionStatement expressionStatement = (ExpressionStatement) statement;

				final Expression expression = expressionStatement.getExpression();

				// if all expressions within the catch block are invocation
				// to one of the methods: printStackTrace(), or fail() or
				// println(), then this catch block is considered as being
				// trivial
				if (expression instanceof MethodInvocation)
				{
					final MethodInvocation methodInvocation = (MethodInvocation) expression;

					isTrivial &= isPrintStackTrace( methodInvocation ) || isFail( methodInvocation )
							|| isPrintln( methodInvocation ) || isAssertion( methodInvocation );
				}
				else
				{
					isTrivial = false;
					break;
				}

			}
			else if (statement instanceof ReturnStatement)
			{
				isTrivial &= true;
			}
			else
			{
				isTrivial = false;
				break;
			}
		}

		return isTrivial;
	}

	public void setImplementsTrivialHandler(final boolean implementsTrivialHandler)
	{
		this.implementsTrivialHandler = implementsTrivialHandler;
	}

	@Override
	public boolean visit(final CatchClause node)
	{
		final SingleVariableDeclaration exceptionDeclaration = node.getException();

		getContext().addExceptionHandled( exceptionDeclaration );

		setImplementsTrivialHandler( isTrivialHandler( node ) || isEmptyCatch( node ) );

		return true;
	}

	@Override
	public boolean visit(final ClassInstanceCreation node)
	{
		final Type variable = node.getType();

		getContext().addVariableTypeUsed( variable );

		final IMethodBinding constructorBinding = node.resolveConstructorBinding();

		final ITypeBinding[] exceptionTypes = constructorBinding.getExceptionTypes();
		for (final ITypeBinding iTypeBinding : exceptionTypes)
		{
			final String name = iTypeBinding.getQualifiedName();
			getContext().addExceptionThrown( name );
		}

		final ITypeBinding declaringClass = constructorBinding.getDeclaringClass();
		final String classQualifiedName = declaringClass.getQualifiedName();
		final String name = constructorBinding.getName();
		final String qualifiedName = String.format( "%s.%s", classQualifiedName, name );

		getContext().addMethodCalled( qualifiedName );

		// TODO ver como faz pra pegar o nome das variaveis
		final ITypeBinding[] parameterTypes = constructorBinding.getParameterTypes();
		for (final ITypeBinding iTypeBinding : parameterTypes)
		{
			final String varQualifiedName = iTypeBinding.getQualifiedName();
			getContext().addVariableTypeUsed( varQualifiedName );
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(final MethodDeclaration node)
	{
		// TODO add declaringPackage to methodContext
		final IMethodBinding binding = node.resolveBinding();

		if (binding == null)
		{
			return false;
		}

		final IJavaElement javaElement = binding.getJavaElement();
		final IMethod method = (IMethod) javaElement;
		try
		{
			final String sourceCodeSnippet = method.getSource();
			getContext().setCodeSnippet( sourceCodeSnippet );
		}
		catch (final JavaModelException e)
		{
			return false;
		}

		final String methodName = binding.getName();

		getContext().addMethodName( methodName );

		final ITypeBinding declaringClass = binding.getDeclaringClass();
		final String classQualifiedName = declaringClass.getQualifiedName();

		getContext().setEnclosingClass( classQualifiedName );

		final ITypeBinding[] exceptionTypes = binding.getExceptionTypes();
		for (final ITypeBinding typeBinding : exceptionTypes)
		{
			final String exceptionQualifiedName = typeBinding.getQualifiedName();
			getContext().addExceptionOnInterface( exceptionQualifiedName );
		}

		if (!node.isConstructor())
		{
			final Type returnType = node.getReturnType2();

			getContext().addReturnedType( returnType );
		}

		final List<SingleVariableDeclaration> parameters = node.parameters();

		getContext().addParameters( parameters );

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(final MethodInvocation node)
	{
		final IMethodBinding methodBinding = node.resolveMethodBinding();
		final String fullyQualifiedName;
		if (methodBinding == null)
		{
			final SimpleName name = node.getName();
			fullyQualifiedName = name.getFullyQualifiedName();
		}
		else
		{
			final ITypeBinding declaringClassBinding = methodBinding.getDeclaringClass();

			final String name = methodBinding.getName();

			final String declaringClassName = declaringClassBinding.getQualifiedName();

			fullyQualifiedName = String.format( "%s.%s", declaringClassName, name );

			final ITypeBinding[] exceptionTypes = methodBinding.getExceptionTypes();
			for (final ITypeBinding iTypeBinding : exceptionTypes)
			{
				final String qualifiedName = iTypeBinding.getQualifiedName();
				getContext().addExceptionThrown( qualifiedName );
			}
		}

		getContext().addMethodCalled( fullyQualifiedName );

		final List<Expression> arguments = node.arguments();

		getContext().addVariablesUsed( arguments );

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(final VariableDeclarationStatement node)
	{
		final List<VariableDeclarationFragment> fragments = node.fragments();
		for (final VariableDeclarationFragment fragment : fragments)
		{
			final SimpleName variableIdentifier = fragment.getName();
			getContext().addVariableIdentifierUsed( variableIdentifier );
		}

		final Type variable = node.getType();

		getContext().addVariableTypeUsed( variable );

		return true;
	}

}
