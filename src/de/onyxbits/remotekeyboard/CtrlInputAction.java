package de.onyxbits.remotekeyboard;

import net.wimpi.telnetd.io.TerminalIO;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
class CtrlInputAction implements Runnable {

	public static final String TAG = "InputAction";

	/**
	 * A control character (anything thats not printable)
	 */
	protected int function;

	/**
	 * For sending raw key presses to the editor
	 */
	private RemoteKeyboardService myService;

	public CtrlInputAction(RemoteKeyboardService myService) {
		this.myService = myService;
	}

	// @Override
	public void run() {
		InputConnection con = myService.getCurrentInputConnection();
		if (con == null) {
			return;
		}

		switch (function) {
		// FIXME: Dirty hack! Most telnet clients send DELETE instead of BACKSPACE
		// when the physical backspace key is pressed. This is usually
		// configurable but difficult to explain. So instead of explaining the
		// issue, we just make both symbols semantically equivalent.
			case TerminalIO.DEL:
			case TerminalIO.DELETE:
			case TerminalIO.BACKSPACE: {
				typeKey(con, KeyEvent.KEYCODE_DEL);
				break;
			}
			case TerminalIO.ENTER:
			case '\n': {
				// NOTE: Workaround for a bug in the telnet library! It should return
				// ENTER, but does return LF.
				typeKey(con, KeyEvent.KEYCODE_ENTER);
				break;
			}
			case TerminalIO.TABULATOR: {
				typeKey(con, KeyEvent.KEYCODE_TAB);
				break;
			}
			case Decoder.SYM_CURSOR_LEFT: {
				typeKey(con, KeyEvent.KEYCODE_DPAD_LEFT);
				break;
			}
			case Decoder.SYM_CURSOR_RIGHT: {
				typeKey(con, KeyEvent.KEYCODE_DPAD_RIGHT);
				break;
			}
			case Decoder.SYM_CURSOR_UP: {
				typeKey(con, KeyEvent.KEYCODE_DPAD_UP);
				break;
			}
			case Decoder.SYM_CURSOR_DOWN: {
				typeKey(con, KeyEvent.KEYCODE_DPAD_DOWN);
				break;
			}
			case Decoder.SYM_CTRL_CURSOR_LEFT: {
				jumpBackward(con, ' ');
				break;
			}
			case Decoder.SYM_CTRL_CURSOR_RIGHT: {
				jumpForward(con, ' ');
				break;
			}
			case Decoder.SYM_INSERT: {
				// Dunno what to do with this one, yet.
				break;
			}
			case Decoder.SYM_DELETE: {
				if (con.getSelectedText(0)==null) {
					con.deleteSurroundingText(0, 1);
				}
				else {
					typeKey(con, KeyEvent.KEYCODE_DEL);
				}
				break;
			}
			case Decoder.SYM_HOME: {
				jumpBackward(con, '\n');
				break;
			}
			case Decoder.SYM_END: {
				jumpForward(con, '\n');
				break;
			}

			// Hacky time! We redefine ASCII control chars to our needs.
			case TerminalIO.COLORINIT: { // CTRL-A
				con.performContextMenuAction(android.R.id.selectAll);
				break;
			}
			case 3: { // CTRL-C
				con.performContextMenuAction(android.R.id.copy);
				break;
			}
			case 22: { // CTRL-V
				con.performContextMenuAction(android.R.id.paste);
				break;
			}
			case 24: { // CTRL-X
				con.performContextMenuAction(android.R.id.cut);
				break;
			}
			case 19: { // CTRL-S
				con.performEditorAction(EditorInfo.IME_ACTION_SEARCH);
				break;
			}
			case 12: { // CTRL-L
				con.performEditorAction(EditorInfo.IME_ACTION_SEND);
				break;
			}
			default: {
				String s = myService.getResources().getString(
						R.string.err_esc_unsupported);
				Toast.makeText(myService, s, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}

	/**
	 * Place the cursor on the next occurance of a symbol
	 * 
	 * @param con
	 *          driver
	 * @param symbol
	 *          the symbol to jump to
	 */
	private void jumpForward(InputConnection con, int symbol) {
		ExtractedText txt = con.getExtractedText(new ExtractedTextRequest(), 0);
		if (txt != null) {
			int pos = txt.text.toString().indexOf(symbol, txt.selectionEnd + 1);
			if (pos == -1) {
				pos = txt.text.length();
			}
			con.setSelection(pos, pos);
		}
	}

	/**
	 * Place the cursor on the last occusrance of a symbol
	 * 
	 * @param con
	 *          driver
	 * @param symbol
	 *          the symbol to jump to
	 */
	private void jumpBackward(InputConnection con, int symbol) {
		ExtractedText txt = con.getExtractedText(new ExtractedTextRequest(), 0);
		if (txt != null) {
			int pos = txt.text.toString().lastIndexOf(symbol, txt.selectionEnd - 2);
			pos++;

			con.setSelection(pos, pos);
		}

	}

	/**
	 * Send an down/up event
	 * 
	 * @con connection to sent with
	 * @param key
	 *          keycode
	 */
	private void typeKey(InputConnection con, int key) {
		con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
		con.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
	}
}
