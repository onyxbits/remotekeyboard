package de.onyxbits.remotekeyboard;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class QuicklauncherActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PackageManager pm = getPackageManager();
		List<PackageInfo> list = pm.getInstalledPackages(0);
		CharSequence[] names = new String[list.size()];
		CharSequence[] displayNames = new String[list.size()];
		SortablePackageInfo spi[] = new SortablePackageInfo[list.size()];
		Iterator<PackageInfo> it = list.iterator();
		int idx=0;
		while(it.hasNext()) {
			PackageInfo info = it.next();
			spi[idx] = new SortablePackageInfo(info.packageName,pm.getApplicationLabel(info.applicationInfo));
			idx++;
		}
		Arrays.sort(spi);
		for (int i=0;i<spi.length;i++) {
			names[i] = spi[i].packageName;
			displayNames[i] = spi[i].displayName;
		}

		addPreferencesFromResource(R.xml.pref_quicklaunchers);

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
		preference.setSummary(((ListPreference) preference).getEntry());
	}

}
