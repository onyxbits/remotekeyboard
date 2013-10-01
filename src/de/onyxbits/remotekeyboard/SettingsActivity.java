package de.onyxbits.remotekeyboard;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		setProgressBarIndeterminate(true);
		setProgressBarVisibility(true);
		new ListTask(this).execute("");
	}

	/**
	 * Callback for actually building the UI after we got a list of startable
	 * apps.
	 * 
	 * @param spi
	 *          list of startable apps.
	 */
	protected void onListAvailable(SortablePackageInfo[] spi) {
		setProgressBarIndeterminate(false);
		setProgressBarVisibility(false);

		CharSequence[] names = new String[spi.length];
		CharSequence[] displayNames = new String[spi.length];
		for (int i = 0; i < spi.length; i++) {
			names[i] = spi[i].packageName;
			displayNames[i] = spi[i].displayName;
		}

		addPreferencesFromResource(R.xml.pref_settings);

		int[] fkeys = { Decoder.SYM_F1, Decoder.SYM_F2, Decoder.SYM_F3,
				Decoder.SYM_F4, Decoder.SYM_F5, Decoder.SYM_F6, Decoder.SYM_F7,
				Decoder.SYM_F8, Decoder.SYM_F9, Decoder.SYM_F10, Decoder.SYM_F11,
				Decoder.SYM_F12 };
		for (int i : fkeys) {
			ListPreference preference = (ListPreference) findPreference(CtrlInputAction.PREF_QUICKLAUNCHER
					+ "." + i);
			preference.setEntries(displayNames);
			preference.setEntryValues(names);
			preference.setSummary(((ListPreference) preference).getEntry());
		}
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (sharedPref.getString(TelnetEditorShell.PREF_PASSWORD, "").equals("")) {
			findPreference(TelnetEditorShell.PREF_PASSWORD).setSummary(
					R.string.msg_password_not_set);
		}
		else {
			findPreference(TelnetEditorShell.PREF_PASSWORD).setSummary(
					R.string.msg_password_set);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference preference = findPreference(key);
		if (preference instanceof ListPreference) {
			preference.setSummary(((ListPreference) preference).getEntry());
		}
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (sharedPref.getString(TelnetEditorShell.PREF_PASSWORD, "").equals("")) {
			findPreference(TelnetEditorShell.PREF_PASSWORD).setSummary(
					R.string.msg_password_not_set);
		}
		else {
			findPreference(TelnetEditorShell.PREF_PASSWORD).setSummary(
					R.string.msg_password_set);
		}

	}

}
