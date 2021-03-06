package br.inf.pucrio.jimboeh.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import br.inf.pucrio.jimboeh.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault( PreferenceConstants.P_PATH, "/" );
		store.setDefault( PreferenceConstants.P_BOOLEAN, false );
		store.setDefault( PreferenceConstants.P_INTEGER, 10 );
	}

}
