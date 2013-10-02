package de.onyxbits.remotekeyboard;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		DialogInterface.OnClickListener {

	public static String TAG = "MainActivity";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	// Based on code from WiFi Keyboard project.
	// https://code.google.com/p/wifikeyboard/
	public static ArrayList<String> getNetworkAddresses() {
		ArrayList<String> addrs = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr.isLoopbackAddress()) {
						continue;
					}
					if (addr instanceof Inet4Address) {
						// IPv4 addresses at the beginning of the list.
						addrs.add(0, addr.getHostAddress());
					} else {
						// IPv6 addresses at the end of the list.
						addrs.add(addr.getHostAddress());
					}
				}
			}
		} catch (SocketException e) {
			// Debug.d("failed to get network interfaces");
		}
		return addrs;
    }

	@Override
	protected void onResume() {
		super.onResume();
		AppRater.appLaunched(this);

		String ips = getNetworkAddresses().toString();
		TextView tv = (TextView) findViewById(R.id.quickinstructions);
		tv.setText(getResources().getString(R.string.app_quickinstuctions, ips));

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo> enabled = imm.getEnabledInputMethodList();
		Iterator<InputMethodInfo> it = enabled.iterator();

		boolean available = false;

		while (it.hasNext()) {
			available = it.next().getServiceName()
					.equals(RemoteKeyboardService.class.getCanonicalName());
			if (available) {
				break;
			}
		}
		if (!available) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.err_notenabled)
					.setTitle(R.string.err_notenabled_title)
					.setPositiveButton(android.R.string.yes, this)
					.setNegativeButton(android.R.string.no, this).create().show();

		}

		String shared = getIntent().getStringExtra(Intent.EXTRA_TEXT);
		if (available && shared != null) {
			tv = (TextView) findViewById(R.id.typetest);
			tv.setText(shared);
			if (TelnetEditorShell.self != null) {
				TelnetEditorShell.self.showText(shared);
				Toast.makeText(this, R.string.msg_sent, Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(this, R.string.err_noclient, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.item_help: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.homepage)));
				startActivity(browserIntent);
				break;
			}
			case R.id.item_replacements: {
				startActivity(new Intent(this, ReplacementsListActivity.class));
				break;
			}
			case R.id.item_settings: {
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			}
			case R.id.item_select: {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showInputMethodPicker();
				break;
			}
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// We are called from the RK is not enabled as IME method.
		if (which == DialogInterface.BUTTON_POSITIVE) {
			startActivity(new Intent(
					android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
		}
	}

}
