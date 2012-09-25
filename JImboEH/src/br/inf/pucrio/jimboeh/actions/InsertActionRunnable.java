package br.inf.pucrio.jimboeh.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.lucene.index.IndexWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.statushandlers.StatusManager;

import br.inf.pucrio.jimboeh.Activator;
import br.inf.pucrio.jimboeh.model.MethodContext;
import br.inf.pucrio.jimboeh.parser.MethodVisitor;
import br.inf.pucrio.jimboeh.util.UtilAST;
import br.inf.pucrio.jimboeh.util.UtilIndex;

public class InsertActionRunnable implements IRunnableWithProgress
{
	public ISelection selection;
	private final Set<IMethod> methodsToIndex;
	private final IndexWriter writer;
	private int insertedCount;

	public InsertActionRunnable(final Set<IMethod> methodsToIndex, final IndexWriter writer)
	{
		this.methodsToIndex = methodsToIndex;
		this.writer = writer;
		setInsertedCount( 0 );
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

				final MethodVisitor visitor = new MethodVisitor( true );

				methodNode.accept( visitor );

				if (visitor.isTrivialHandler())
				{
					continue;
				}

				final MethodContext context = visitor.getContext();

				if (context == null)
				{
					continue;
				}

				final Set<String> exceptionsHandled = context.getExceptionsHandled();

				final boolean handlesException = !exceptionsHandled.isEmpty();

				if (handlesException)
				{
					try
					{
						UtilIndex.insertIntoIndex( writer, context );
						setInsertedCount( getInsertedCount() + 1 );
					}
					catch (final IOException e)
					{
						final IStatus status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
						Activator.getDefault().getLog().log( status );
					}
				}

				monitor.worked( 1 );
			}

			monitor.done();
		}
		catch (final CoreException e)
		{
			StatusManager.getManager().handle( e.getStatus(), StatusManager.SHOW | StatusManager.LOG );
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (final IOException e)
				{
					final Status status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e );
					Activator.getDefault().getLog().log( status );
				}
			}
		}
	}

	public int getInsertedCount()
	{
		return insertedCount;
	}

	public void setInsertedCount(int insertedCount)
	{
		this.insertedCount = insertedCount;
	}
}