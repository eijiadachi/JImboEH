package br.inf.pucrio.jimboeh.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import br.inf.pucrio.jimboeh.Activator;

public class JImboEHPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public JImboEHPreferencePage()
	{
		super( GRID );
		setPreferenceStore( Activator.getDefault().getPreferenceStore() );
		setDescription( "JImboEH preferences page" );
	}

	@Override
	public void createFieldEditors()
	{
		addField( new DirectoryFieldEditor( PreferenceConstants.P_PATH, "&Index path:", getFieldEditorParent() ) );

		addField( new BooleanFieldEditor( PreferenceConstants.P_BOOLEAN, "&Open in Read-Only mode",
				getFieldEditorParent() ) );

		addField( new IntegerFieldEditor( PreferenceConstants.P_INTEGER, "&Maximum number of results showed",
				getFieldEditorParent() ) );
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		setPreferenceStore( Activator.getDefault().getPreferenceStore() );
	}

}