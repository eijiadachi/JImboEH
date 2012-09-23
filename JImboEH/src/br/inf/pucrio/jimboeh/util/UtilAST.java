package br.inf.pucrio.jimboeh.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;

public class UtilAST
{

	public static ASTNode astNode(final IFile resource) throws IOException, CoreException
	{
		final InputStream fileContents = resource.getContents();

		final BufferedReader reader = new BufferedReader( new InputStreamReader( fileContents ) );

		final StringBuffer sb = new StringBuffer();

		while (reader.ready())
		{
			sb.append( reader.readLine() );
		}

		final ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom( resource );

		final IProject project = resource.getProject();
		final IJavaProject javaProject = JavaCore.create( project );

		final ASTParser astParser = ASTParser.newParser( AST.JLS4 );
		astParser.setSource( compilationUnit );
		astParser.setKind( ASTParser.K_COMPILATION_UNIT );
		astParser.setResolveBindings( true );
		astParser.setBindingsRecovery( true );
		astParser.setProject( javaProject );

		final ASTNode rootNode = astParser.createAST( new NullProgressMonitor() );

		return rootNode;
	}

	public static MethodDeclaration astNode(final IMethod method) throws JavaModelException
	{
		final ICompilationUnit compilationUnit = method.getCompilationUnit();

		final ASTParser astParser = ASTParser.newParser( AST.JLS4 );
		astParser.setSource( compilationUnit );
		astParser.setKind( ASTParser.K_COMPILATION_UNIT );
		astParser.setResolveBindings( true );
		astParser.setBindingsRecovery( true );

		final ASTNode rootNode = astParser.createAST( null );

		// final CompilationUnit compilationUnitNode = (CompilationUnit)
		// rootNode;

		final String unitSource = compilationUnit.getSource();
		final String methodSource = method.getSource();

		final int indexOf = unitSource.indexOf( methodSource );

		final int length = methodSource.length();
		final ASTNode currentNode = NodeFinder.perform( rootNode, indexOf + 1, length - 1 );

		final MethodDeclaration methodDeclarationParent = getMethodDeclarationParent( currentNode );

		return methodDeclarationParent;

		// TODO ver resposta do StackOverflow
		// final String key = method.getKey();
		//
		// final ASTNode javaElement = compilationUnitNode.findDeclaringNode(
		// key );
		//
		// final MethodDeclaration methodDeclarationNode = (MethodDeclaration)
		// javaElement;
		//
		// return methodDeclarationNode;
	}

	public static MethodDeclaration getMethodDeclarationParent(final ASTNode node)
	{
		ASTNode parent = node;

		while (parent != null && !(parent instanceof MethodDeclaration))
		{
			parent = parent.getParent();
		}

		if (parent != null && parent instanceof MethodDeclaration)
		{
			return (MethodDeclaration) parent;
		}
		else
		{
			return null;
		}
	}

}
