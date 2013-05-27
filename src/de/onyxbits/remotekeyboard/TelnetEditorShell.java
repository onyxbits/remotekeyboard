package de.onyxbits.remotekeyboard;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.hardware.input.InputManager;
import android.inputmethodservice.InputMethodService;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.ColorHelper;
import net.wimpi.telnetd.io.toolkit.BufferOverflowException;
import net.wimpi.telnetd.io.toolkit.Editarea;
import net.wimpi.telnetd.io.toolkit.Editfield;
import net.wimpi.telnetd.io.toolkit.InputFilter;
import net.wimpi.telnetd.io.toolkit.Label;
import net.wimpi.telnetd.io.toolkit.Statusbar;
import net.wimpi.telnetd.io.toolkit.Titlebar;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;

public class TelnetEditorShell implements Shell {

	public static final String TAG = "EditorShell";
	public static final char[] SPINNER = { '-', '/', '|', '\\' };
	private int spinnerIndex;

	protected static TelnetEditorShell self;

	protected Titlebar titleBar;
	protected Statusbar statusBar;
	protected Label content;

	protected BasicTerminalIO m_IO;

	// NOTE: We start out with 1 object and expand dynamically when more
	// are needed. For normal typing, 1 object usually suffices. When holding
	// a key at a normal key repeat rate, we might need 3-4 objects. However,
	// when the user uses copy and paste, the object count may go through the
	// roof.
	private InputAction actionPool[] = { new InputAction() };

	public TelnetEditorShell() {

	}

	@Override
	public void connectionIdle(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionTimedOut(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionLogoutRequest(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionSentBreak(ConnectionEvent ce) {
		// TODO Auto-generated method stub

	}

	/**
	 * Pick an action from the actionPool that is not in use
	 * 
	 * @return a pooled inputaction.
	 */
	private InputAction getFreeInputAction() {
		for (int i = 0; i < actionPool.length; i++) {
			if (!actionPool[i].inUse) {
				return actionPool[i];
			}
		}

		// No free items in the pool
		if (actionPool.length < 20) {
			// Expand the pool
			Log.d(TAG, "Buffer to small -> expanding to " + actionPool.length
					+ " entries");
			InputAction[] tmp = new InputAction[actionPool.length + 1];
			System.arraycopy(actionPool, 0, tmp, 0, actionPool.length);
			tmp[tmp.length - 1] = new InputAction();
			actionPool = tmp;
			return actionPool[actionPool.length - 1];
		}
		else {
			// Throttle (the user is probably posting massive text via copy&paste)
			while (true) {
				try {
					Thread.sleep(10);
				}
				catch (InterruptedException e) {
				}
				for (int i = 0; i < actionPool.length; i++) {
					if (!actionPool[i].inUse) {
						return actionPool[i];
					}
				}
			}
		}
	}

	@Override
	public void run(Connection con) {
		m_IO = con.getTerminalIO();

		// Ensure the screen does not go off while we are connected.
		PowerManager pm = (PowerManager) RemoteKeyboardService.self
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(
				PowerManager.FULL_WAKE_LOCK, TAG);
		wakeLock.acquire();

		Resources res = RemoteKeyboardService.self.getResources();

		RemoteKeyboardService.self.updateNotification(con.getConnectionData()
				.getInetAddress());

		try {
			// Make the terminal window look pretty/informative.
			titleBar = new Titlebar(m_IO, "titlebar");
			titleBar.setTitleText(res.getString(R.string.terminal_title));
			titleBar.setAlignment(Titlebar.ALIGN_LEFT);
			titleBar.setForegroundColor(ColorHelper.WHITE);
			titleBar.setBackgroundColor(ColorHelper.BLUE);

			content = new Label(m_IO, "content");
			content.setLocation(0, 2);

			statusBar = new Statusbar(m_IO, "statusbar");
			statusBar.setStatusText(res.getString(R.string.terminal_statusbar));
			statusBar.setAlignment(Titlebar.ALIGN_LEFT);
			statusBar.setForegroundColor(ColorHelper.WHITE);
			statusBar.setBackgroundColor(ColorHelper.BLUE);

			showText(getWelcomeScreen());

			int in;
			int offset = 0;
			InputAction inputAction = null;
			long lastEvent = SystemClock.uptimeMillis();

			// Main loop starts here
			while (true) {
				in = m_IO.read();
				if (in == TerminalIO.IOERROR || in == TerminalIO.HANDLED) {
					// NOTE: TerminalIO.read() internally transforms LOGOUTREEQUEST
					// into HANDLED.
					break;
				}
				if (offset == 0) {
					inputAction = getFreeInputAction();
					inputAction.inUse = true;
					inputAction.symbol = in;
					inputAction.sequence = inputAction.buffer[InputAction.getBuffer(in)];
				}
				inputAction.sequence[offset] = (byte) in;
				offset++;
				if (offset == inputAction.sequence.length) {
					offset = 0;
					inputAction.myService = RemoteKeyboardService.self;
					// FIXME: When the user uses copy&paste, the UI may be clogged up
					// since the garbage collector cleans up after posting actions.
					// Throttling helps but what we really need is a way to merge
					// actions.
					long now = SystemClock.uptimeMillis();
					;
					if ((now - lastEvent) > 30) {
						try {
							Thread.sleep(30);
						}
						catch (InterruptedException e) {
						}
					}
					lastEvent = now;
					RemoteKeyboardService.self.handler.post(inputAction);
				}
			}

			m_IO.eraseScreen();
			m_IO.flush();
		}
		catch (EOFException e) {
			// User disconnected disgracefully -> don't care.
		}
		catch (IOException e) {
			Log.w(TAG, e);
		}
		finally {
			RemoteKeyboardService.self.updateNotification(null);
			wakeLock.release();
			self = null;
		}
	}

	/**
	 * Put some text in the area between title and statusbar
	 * 
	 * @param text
	 *          what to display
	 */
	public void showText(String text) {
		try {
			m_IO.eraseScreen();
			content.setText(text);
			titleBar.draw();
			content.draw();
			statusBar.draw();
			m_IO.setCursor(m_IO.getRows() - 1, m_IO.getColumns() - 1);
			m_IO.flush();
		}
		catch (IOException e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * Produce a welcome screen
	 * 
	 * @return text to dump on the screen on session startup
	 */
	private String getWelcomeScreen() {
		try {
			RemoteKeyboardService myService = RemoteKeyboardService.self;
			AssetManager assetManager = myService.getResources().getAssets();
			InputStream inputStream = assetManager.open("welcomescreen.txt");
			Scanner s = new Scanner(inputStream).useDelimiter("\\A");
			return s.next();
		}
		catch (Exception exp) {
			Log.w(TAG, exp);
		}
		return "";
	}

	public static Shell createShell() {
		self = new TelnetEditorShell();
		return self;
	}

}
