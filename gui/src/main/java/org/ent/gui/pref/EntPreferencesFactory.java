package org.ent.gui.pref;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class EntPreferencesFactory implements PreferencesFactory {

	@Override
	public Preferences systemRoot() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Preferences userRoot() {
		return new EntPreferences();
	}

}
