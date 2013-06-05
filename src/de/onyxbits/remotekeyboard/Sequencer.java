package de.onyxbits.remotekeyboard;

import net.wimpi.telnetd.io.TelnetIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.Terminal;

/**
 * Utility class for reading multibyte values (UTF-8 characters, escape
 * sequences) and figuring out what they mean.
 * 
 * @author patrick
 * 
 */
class Sequencer {

	/**
	 * Minimum value of all constants defined here (so we don't clash with the
	 * telnet lib).
	 */
	public static final int BASE = 10000;

	/**
	 * Returned if we haven't read a complete sequence, yet.
	 */
	public static final int INCOMPLETE = BASE + 1;

	/**
	 * Sequence did not match any known constant
	 */
	public static final int UNKNOWN = BASE + 2;

	public static final int INSERT = BASE + 101;
	public static final int DELETE = BASE + 102;
	public static final int HOME = BASE + 103;
	public static final int END = BASE + 104;
	public static final int PAGE_UP = BASE + 105;
	public static final int PAGE_DOWN = BASE + 106;

	public static final int F1 = BASE + 201;
	public static final int F2 = BASE + 202;
	public static final int F3 = BASE + 203;
	public static final int F4 = BASE + 204;
	public static final int F5 = BASE + 205;
	public static final int F6 = BASE + 206;
	public static final int F7 = BASE + 207;
	public static final int F8 = BASE + 208;
	public static final int F9 = BASE + 209;
	public static final int F10 = BASE + 210;
	public static final int F11 = BASE + 211;
	public static final int F12 = BASE + 212;

	public static final int CURSOR_UP = BASE + 301;
	public static final int CURSOR_RIGHT = BASE + 302;
	public static final int CURSOR_DOWN = BASE + 303;
	public static final int CURSOR_LEFT = BASE + 304;

	public static final int SHIFT_CURSOR_UP = BASE + 311;
	public static final int SHIFT_CURSOR_RIGHT = BASE + 312;
	public static final int SHIFT_CURSOR_DOWN = BASE + 313;
	public static final int SHIFT_CURSOR_LEFT = BASE + 314;

	public static final int CTRL_CURSOR_UP = BASE + 315;
	public static final int CTRL_CURSOR_RIGHT = BASE + 316;
	public static final int CTRL_CURSOR_DOWN = BASE + 317;
	public static final int CTRL_CURSOR_LEFT = BASE + 318;

	/**
	 * Escape sequence database. Interpretation comes first, followed by the
	 * sequence (minus the ESC[).
	 */
	private int sequences[][] = { 
			{ CURSOR_UP, 'A' }, 
			{ CURSOR_RIGHT, 'C' },
			{ CURSOR_DOWN, 'B' }, 
			{ CURSOR_LEFT, 'D' },
			{ HOME, 'H'},
			{ END, 'F'},
			{ INSERT, '2', '~' },
			{ DELETE, '3', '~' },
			{ PAGE_UP,'5', '~'},
			{ PAGE_UP,'6', '~'},
			{ CTRL_CURSOR_LEFT, '1',';','5','D'},
			{ CTRL_CURSOR_RIGHT, '1',';','5','C'},
			
	};
	
	/**
	 * Buffer for escape sequences
	 */
	private int[] cBuffer = new int[10];
	
	/**
	 * Buffer index (for escape sequences
	 */
	private int index;

	/**
	 * Buffers for reading multibyte characters from an UTF-8 encoded bytestream.
	 */
	protected byte[][] uBuffer = { new byte[1], new byte[2], new byte[3],
			new byte[4], new byte[5], new byte[6] };

	
	public Sequencer() {
	}

	/**
	 * Interpret an escape sequence.
	 * 
	 * @param symbol
	 *          symbol read from the network. Passing TerminalIO.ESCAPE resets the
	 *          internal state.
	 * @return INCOMPLETE if more symbols are needed, UNKNOWN if a sequence could
	 *         not be interpreted or any of the constants standing for a
	 *         interpreted sequence.
	 */
	public int interpret(int symbol) {
		if (symbol == TerminalIO.ESCAPE) {
			index = 0;
		}
		cBuffer[index] = symbol;
		if (index > 1 && cBuffer[0] == TerminalIO.ESCAPE
				&& cBuffer[1] == Terminal.LSB) {
			for (int x = 0; x < sequences.length; x++) {
				if (index == sequences[x].length) {
					boolean matched = true;
					for (int y = 1; y < sequences[x].length; y++) {
						if (cBuffer[y + 1] != sequences[x][y]) {
							matched = false;
							break;
						}
					}
					if (matched) {
						return sequences[x][0];
					}
				}
			}
		}
		if (index == uBuffer.length) {
			return UNKNOWN;
		}
		else {
			index++;
			return INCOMPLETE;
		}
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
	protected byte[] getBuffer(int input) {
		// A key like LEFT, RIGHT, etc.
		if (input > 255)
			return uBuffer[0];
		// SEE: http://en.wikipedia.org/wiki/UTF-8#Description
		if ((((byte) input) & 252) == 252)
			return uBuffer[5];
		if ((((byte) input) & 248) == 248)
			return uBuffer[4];
		if ((((byte) input) & 240) == 240)
			return uBuffer[3];
		if ((((byte) input) & 224) == 224)
			return uBuffer[2];
		if ((((byte) input) & 192) == 192)
			return uBuffer[1];
		return uBuffer[0];
	}
}
