package de.onyxbits.remotekeyboard;

import java.io.IOException;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import net.wimpi.telnetd.BootException;
import net.wimpi.telnetd.TelnetD;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteKeyboardService extends InputMethodService implements
		OnKeyboardActionListener {

	public static final String TAG = "RemoteKeyboardService";
	public static final int NOTIFICATION = 42;

	private TelnetD telnetServer;

	/**
	 * For posting InpuitActions on the UI thread.
	 */
	protected Handler handler;

	/**
	 * Reference to the running service
	 */
	protected static RemoteKeyboardService self;

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Properties props = new Properties();
		AssetManager assetManager = getResources().getAssets();
		self = this;
		handler = new Handler();

		try {
			InputStream inputStream = assetManager.open("telnetd.properties");
			props.load(inputStream);
			telnetServer = TelnetD.getReference();
			if (telnetServer == null) {
				telnetServer = TelnetD.createTelnetD(props);
			}
			telnetServer.start();

			updateNotification(null);
		}
		catch (IOException e) {
			Log.w(TAG, e);
		}
		catch (BootException e) {
			Log.w(TAG, e);
		}
	}

	@Override
	public View onCreateInputView() {
		// RemoteKeyboardView ret = (RemoteKeyboardView) getLayoutInflater().
		// inflate(R.layout.input,null);
		KeyboardView ret = new KeyboardView(this, null);
		ret.setKeyboard(new Keyboard(this, R.xml.keyboarddef));
		ret.setOnKeyboardActionListener(this);
		return ret;
	}

	@Override
	public void onInitializeInterface() {
		super.onInitializeInterface();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (telnetServer != null) {
			telnetServer.stop();
		}
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION);
		self = null;
	}

	@Override
	public void onPress(int primaryCode) {
		// SEE: res/xml/keyboarddef.xml for the definitions.
		switch (primaryCode) {
			case 0: {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showInputMethodPicker();
				break;
			}
			case 1: {
				/*
				 * Intent intent = new Intent(this, SettingsActivity.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 * startActivity(intent);
				 */
				break;
			}
			case 2: {
				try {
					InputConnection con = getCurrentInputConnection();
					CharSequence txt = con.getSelectedText(0);
					if (txt == null) {
						txt = getCurrentInputConnection().getExtractedText(
								new ExtractedTextRequest(), 0).text;
					}
					TelnetEditorShell.self.showText(txt + "");
				}
				catch (Exception exp) {
					Toast.makeText(this, R.string.err_sendtonirvana, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			}
			case 3: {
				try{
					TelnetEditorShell.self.disconnect();
				}
				catch (Exception e) {
					
				}
			}
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
	}

	/**
	 * Update the message in the notification area
	 * 
	 * @param remote
	 *          the remote host we are connected to or null if not connected.
	 */
	protected void updateNotification(InetAddress remote) {
		String title = getResources().getString(R.string.notification_title);
		String content = null;
		if (remote == null) {
			content = getResources().getString(R.string.notification_waiting);
		}
		else {
			content = getResources().getString(R.string.notification_peer,
					remote.getHostName());
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder
				.setContentText(content)
				.setContentTitle(title)
				.setOngoing(true)
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this,
								MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
				.setSmallIcon(R.drawable.ic_action_selectime);
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION, builder.build());
	}
}
