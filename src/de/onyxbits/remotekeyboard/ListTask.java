package de.onyxbits.remotekeyboard;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

/**
 * Scans the device for installed apps on behalf of the settings activity.
 * @author patrick
 *
 */
class ListTask extends AsyncTask<Object, Object, Object> {

	private SettingsActivity context;
	private SortablePackageInfo[] result;
	
	public ListTask(SettingsActivity ctx) {
		this.context=ctx;
	}
	
	@Override
	protected Object doInBackground(Object... o) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> list = pm.getInstalledPackages(0);
		SortablePackageInfo spitmp[] = new SortablePackageInfo[list.size()];
		Iterator<PackageInfo> it = list.iterator();
		int idx = 0;
		while (it.hasNext()) {
			PackageInfo info = it.next();
			CharSequence tmp = pm.getApplicationLabel(info.applicationInfo);
			// We are only interested in apps that actually have a launcher
			if (pm.getLaunchIntentForPackage(info.packageName) != null) {
				spitmp[idx] = new SortablePackageInfo(info.packageName, tmp);
				idx++;
			}
		}
		result = new SortablePackageInfo[idx];
		System.arraycopy(spitmp, 0, result, 0, idx);
		Arrays.sort(result);

		return null;
	}
	
	@Override
	protected void onPostExecute(Object o) {
		context.onListAvailable(result);
	}

}
