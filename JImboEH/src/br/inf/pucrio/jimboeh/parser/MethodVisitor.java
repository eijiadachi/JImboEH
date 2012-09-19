package br.inf.pucrio.jimboeh.parser;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import br.inf.pucrio.jimboeh.model.MethodContext;

public class MethodVisitor extends ASTVisitor
{

	private final MethodContext context = new MethodContext();

	public MethodContext getContext()
	{
		return context;
	}

	@Override
	public boolean visit(final CatchClause node)
	{
		final SingleVariableDeclaration exceptionDeclaration = node.getException();

		getContext().addExceptionHandled( exceptionDeclaration );

		return true;
	}

	@Override
	public boolean visit(final ClassInstanceCreation node)
	{
		final Type variable = node.getType();

		getContext().addVariableTypeUsed( variable );

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(final MethodDeclaration node)
	{
		// TODO add declaringPackage to methodContext
		final IMethodBinding binding = node.resolveBinding();

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

		final Type returnType = node.getReturnType2();

		getContext().addReturnedType( returnType );

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

			fullyQualifiedName = declaringClassBinding.getQualifiedName();
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
