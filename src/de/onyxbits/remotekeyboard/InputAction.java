package de.onyxbits.remotekeyboard;

import net.wimpi.telnetd.io.TerminalIO;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

/**
 * Models a key event sent from the remote keyboard via the telnet protocol and
 * provides a method for replaying that input on the UI thread (possibly
 * translating it from telnet to android semantics).
 * 
 * @author patrick
 * 
 */
class InputAction implements Runnable {
	
	public static final String TAG = "InputAction";

	/**
	 * What fell out of TerminalIO.read(). This can either be an ASCII character
	 * or a control character (e.g. TerminalIO.DELETE) or totally meaningless in
	 * case of a multibyte non-ASCII character (in which case the full UTF-8 byte
	 * sequence is stored in sequence).
	 */
	protected int symbol;

	/**
	 * UTF-8 byte sequence, reference to {@link buffer}.
	 */
	protected byte[] sequence;

	/**
	 * Buffers for reading multibyte characters from an UTF-8 encoded bytestream.
	 */
	protected byte[][] buffer = { new byte[1], new byte[2], new byte[3],
			new byte[4], new byte[5], new byte[6] };

	/**
	 * For sending raw key presses to the editor
	 */
	protected RemoteKeyboardService myService;

	public InputAction() {
	}

	/**
	 * We expect UTF-8 encoded characters. UTF-8 has variable encoding lengths ->
	 * Determine which buffer size is appropriate.
	 * 
	 * @param input
	 *          the byte that was read from the network and either represents the
	 *          first byte of an UTF-8 encoded character or a terminal key.
	 * @return an index into <code>buffer</code>.
	 */
	protected static int getBuffer(int input) {
		// A key like LEFT, RIGHT, etc.
		if (input > 255)
			return 0;
		// SEE: http://en.wikipedia.org/wiki/UTF-8#Description
		if ((((byte) input) & 252) == 252)
			return 5;
		if ((((byte) input) & 248) == 248)
			return 4;
		if ((((byte) input) & 240) == 240)
			return 3;
		if ((((byte) input) & 224) == 224)
			return 2;
		if ((((byte) input) & 192) == 192)
			return 1;
		return 0;
	}

	// @Override
	public void run() {
		Log.d(TAG, "" + symbol);
		InputConnection con = myService.getCurrentInputConnection();
		if (con == null) {
			return;
		}

		switch (symbol) {
			case TerminalIO.UNRECOGNIZED: {
				String s = myService.getResources().getString(
						R.string.err_esc_unsupported);
				Toast.makeText(myService, s, Toast.LENGTH_SHORT).show();
				break;
			}
			// FIXME: Dirty hack! Most telnet clients send DELETE instead of BACKSPACE
			// when the physical backspace key is pressed. This is usually
			// configurable but difficult to explain. So instead of explaining the
			// issue, we just make both symbols semantically equivalent.
			case TerminalIO.DEL:
			case TerminalIO.DELETE:
			case TerminalIO.BACKSPACE: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DEL));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
				break;
			}
			case TerminalIO.ENTER:
			case '\n': {
				// NOTE: Workaround for a bug in the telnet library! It should return
				// ENTER, but does return LF.
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_ENTER));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						KeyEvent.KEYCODE_ENTER));
				break;
			}
			case TerminalIO.LEFT: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DPAD_LEFT));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						KeyEvent.KEYCODE_DPAD_LEFT));
				break;
			}
			case TerminalIO.RIGHT: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DPAD_RIGHT));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						KeyEvent.KEYCODE_DPAD_RIGHT));
				break;
			}
			case TerminalIO.UP: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DPAD_UP));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						KeyEvent.KEYCODE_DPAD_UP));
				break;
			}
			case TerminalIO.DOWN: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_DPAD_DOWN));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
						KeyEvent.KEYCODE_DPAD_DOWN));
				break;
			}
			case TerminalIO.ESCAPE: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_BACK));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				break;
			}
			case TerminalIO.TABULATOR: {
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
						KeyEvent.KEYCODE_TAB));
				con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB));
				break;
			}

			// Hacky time! Redefine the semantics of CTRL-A to select-all
			case TerminalIO.COLORINIT: {
				ExtractedText text = con
						.getExtractedText(new ExtractedTextRequest(), 0);
				try {
					con.setSelection(0, text.text.length());
				}
				catch (NullPointerException e) {
					// Potentially, text or text.text can be null
				}
				break;
			}

			// Hacky time! Redefine the semantics of ASCII ETX (CTRL-C) to copy
			case 3: {
				con.performContextMenuAction(android.R.id.copy);
				break;
			}

			// Hacky time! Redefine the semantics of ASCII SYN (CTRL-V) to paste
			case 22: {
				con.performContextMenuAction(android.R.id.paste);
				break;
			}

			// Hacky time! Redefine the semantics of ASCII CAN (CTRL-X) to cut
			case 24: {
				con.performContextMenuAction(android.R.id.cut);
				break;
			}

			default: {
				con.commitText(new String(sequence), 1);
			}
		}
	}
}
