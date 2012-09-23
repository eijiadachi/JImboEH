package br.inf.pucrio.jimboeh.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.util.UtilAST;

public class InsertActionRunnable implements IRunnableWithProgress
{
	public ISelection selection;
	private final Set<IMethod> methodsToIndex;

	public InsertActionRunnable(final Set<IMethod> methodsToIndex)
	{
		this.methodsToIndex = methodsToIndex;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		try
		{
			monitor.beginTask( "Indexing methods", methodsToIndex.size() );

			for (final IMethod method : methodsToIndex)
			{
				final MethodDeclaration methodNode = UtilAST.astNode( method );

				if (methodNode == null)
				{
					continue;
				}

				final MethodVisitor visitor = new MethodVisitor();

				methodNode.accept( visitor );

				final MethodContext context = visitor.getContext();

				final Set<String> exceptionsHandled = context.getExceptionsHandled();
				final boolean handlesException = !exceptionsHandled.isEmpty();
				if (handlesException)
				{
					final String contextStr = context.toString();

					final String methodName = method.getElementName();

					final String message = String.format( "Inserting method: %s\n\n%s", methodName, contextStr );
				}
				else
				{
					final String message = String.format( "Ignoring: '%s.%s'", context.getEnclosingClass(),
							context.getMethodName() );
					final IStatus status = new Status( IStatus.INFO, Activator.PLUGIN_ID, message );
					Activator.getDefault().getLog().log( status );
				}

				monitor.worked( 1 );
			}

			monitor.done();
		}
		catch (final CoreException e)
		{
			e.printStackTrace();
		}
	}
}