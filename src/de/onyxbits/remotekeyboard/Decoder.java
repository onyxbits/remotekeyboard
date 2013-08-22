package de.onyxbits.remotekeyboard;

import net.wimpi.telnetd.io.TerminalIO;


/**
 * Decodes the bytestream coming from the client, interprets control bytes,
 * sequences and unicode characters.
 * 
 * @author patrick
 * 
 */
class Decoder {

	/**
	 * All symbols start at this base to avoid clashing with the wimpi telnet
	 * lib.
	 */
	private static final int BASE = 10000;

	public static final int SYM_INSERT = BASE + 101;
	public static final int SYM_DELETE = BASE + 102;
	public static final int SYM_HOME = BASE + 103;
	public static final int SYM_END = BASE + 104;
	public static final int SYM_PAGE_UP = BASE + 105;
	public static final int SYM_PAGE_DOWN = BASE + 106;

	public static final int SYM_F1 = BASE + 201;
	public static final int SYM_F2 = BASE + 202;
	public static final int SYM_F3 = BASE + 203;
	public static final int SYM_F4 = BASE + 204;
	public static final int SYM_F5 = BASE + 205;
	public static final int SYM_F6 = BASE + 206;
	public static final int SYM_F7 = BASE + 207;
	public static final int SYM_F8 = BASE + 208;
	public static final int SYM_F9 = BASE + 209;
	public static final int SYM_F10 = BASE + 210;
	public static final int SYM_F11 = BASE + 211;
	public static final int SYM_F12 = BASE + 212;

	public static final int SYM_CURSOR_UP = BASE + 301;
	public static final int SYM_CURSOR_RIGHT = BASE + 302;
	public static final int SYM_CURSOR_DOWN = BASE + 303;
	public static final int SYM_CURSOR_LEFT = BASE + 304;

	public static final int SYM_SHIFT_CURSOR_UP = BASE + 311;
	public static final int SYM_SHIFT_CURSOR_RIGHT = BASE + 312;
	public static final int SYM_SHIFT_CURSOR_DOWN = BASE + 313;
	public static final int SYM_SHIFT_CURSOR_LEFT = BASE + 314;

	public static final int SYM_CTRL_CURSOR_UP = BASE + 315;
	public static final int SYM_CTRL_CURSOR_RIGHT = BASE + 316;
	public static final int SYM_CTRL_CURSOR_DOWN = BASE + 317;
	public static final int SYM_CTRL_CURSOR_LEFT = BASE + 318;
	

	/**
	 * Used by matchEscapeSeequence. NOTE: In order to keep this small, the first
	 * slot contains the decoded symbol instead of the escape character.
	 */
	private int sequences[][] = { 
			{ SYM_CURSOR_UP, '[', 'A' },
			{ SYM_CURSOR_RIGHT, '[', 'C' }, 
			{ SYM_CURSOR_DOWN, '[', 'B' },
			{ SYM_CURSOR_LEFT, '[', 'D' }, 
			{ SYM_HOME, '[', 'H' },
			{ SYM_END, '[', 'F' }, 
			{ SYM_INSERT, '[', '2', '~' },
			{ SYM_DELETE, '[', '3', '~' }, 
			{ SYM_PAGE_UP, '[', '5', '~' },
			{ SYM_PAGE_UP, '[', '6', '~' },
			{ SYM_CTRL_CURSOR_LEFT, '[', '1', ';', '5', 'D' },
			{ SYM_CTRL_CURSOR_RIGHT, '[', '1', ';', '5', 'C' },
			{ SYM_SHIFT_CURSOR_UP, '[','1',';','2','A'},
			{ SYM_SHIFT_CURSOR_DOWN, '[','1',';','2','B'},
			{ SYM_SHIFT_CURSOR_RIGHT, '[','1',';','2','C'},
			{ SYM_SHIFT_CURSOR_LEFT, '[','1',';','2','D'},
			{ SYM_F1,'O','P'},
			{ SYM_F2,'O','Q'},
			{ SYM_F3,'O','R'},
			{ SYM_F4,'O','S'},
			{ SYM_F5,'[','1','5','~'},
			{ SYM_F6,'[','1','7','~'},
			{ SYM_F7,'[','1','8','~'},
			{ SYM_F8,'[','1','9','~'},
			{ SYM_F9,'[','2','0','~'},
			{ SYM_F10,'[','2','1','~'},
			{ SYM_F11,'[','2','3','~'},
			{ SYM_F12,'[','2','4','~'},
	};

	/**
	 * Single byte control char (ASCII code < 32)
	 */
	private static final int CONTROLCHAR = 2;

	/**
	 * Decoding an escape sequence
	 */
	private static final int ESCSEQ = 3;

	/**
	 * Decoded sequence is a unicode character
	 */
	public static final int PRINTABLE = 4;

	/**
	 * Decoding needs more symbols.
	 */
	public static final int INCOMPLETE = 5;

	/**
	 * Decoded sequence is a function code
	 */
	public static final int FUNCTIONCODE = 6;

	/**
	 * An escape sequence that is not supported
	 */
	public static final int UNSUPPORTED = -1;

	private int type = PRINTABLE;
	private int expectedLength;
	private byte[] buffer = new byte[6];
	private int index;
	private int functionCode;
	private boolean complete = true;

	public Decoder() {
	}

	/**
	 * Figure out what a symbol in a byte stream means
	 * 
	 * @param symbol
	 *          the symbol to look at
	 * @return either INCOMPLETE, PRINTABLE or FUNCTIONCODE, telling the caller
	 *         how to (or not to) retrieve the decoded symbol.
	 */
	public int decode(int symbol) {
		buffer[index] = (byte) symbol;

		if (complete) {
			// Check if we need to start a sequence
			if ((symbol >= 0 && symbol < 32) || (symbol > 255)) {
				// NOTE: >255 means the telnet lib already mapped it to a function code.
				type = CONTROLCHAR;
				functionCode = symbol;
			}
			if (symbol == TerminalIO.ESCAPE) {
				type = ESCSEQ;
				complete = false;
				functionCode = UNSUPPORTED;
			}
			if (symbol >= 32 && symbol <= 255) {
				type = PRINTABLE;
				expectedLength = runLength(symbol);
				complete = (expectedLength == 1);
			}
		}

		if (!complete) {
			// Check if if the sequence is completed. NOTE: This block may interleave
			// with the previous one -> must not be an else branch.
			if (type == ESCSEQ) {
				matchEscapeSequence();
			}
			else {
				complete = (index == expectedLength - 1);
			}
		}

		if (complete) {
			index = 0;
			int ret = PRINTABLE;
			if (type == CONTROLCHAR || type == ESCSEQ) {
				ret = FUNCTIONCODE;
			}
			return ret;
		}
		else {
			index++;
			return INCOMPLETE;
		}
	}

	/**
	 * Interpret the internal buffer as a Unicode sequence.
	 * 
	 * @return the string representation of the buffer's contents
	 */
	public String getPrintable() {
		return new String(buffer, 0, expectedLength);
	}

	/**
	 * Interpret the buffers contents as a function code.
	 * 
	 * @return a constant, describing the semantics of the buffer. This may be an
	 *         ASCII control char, a constant from the telnet library or a
	 *         constant from this class.
	 */
	public int getFunctionCode() {
		return functionCode;
	}

	/**
	 * Compute the length of a UTF-8 sequence.
	 * 
	 * @param input
	 *          first byte of the sequence
	 * @return number of bytes in the sequence.
	 */
	public static int runLength(int input) {
		// SEE: http://en.wikipedia.org/wiki/UTF-8#Description
		if ((((byte) input) & 252) == 252)
			return 6;
		if ((((byte) input) & 248) == 248)
			return 5;
		if ((((byte) input) & 240) == 240)
			return 4;
		if ((((byte) input) & 224) == 224)
			return 3;
		if ((((byte) input) & 192) == 192)
			return 2;
		return 1;
	}

	/**
	 * Figure out if the buffer contains a (n in)complete escape sequence
	 * and if so, which one.
	 */
	private void matchEscapeSequence() {
		complete=false;
		functionCode=UNSUPPORTED;
		
		if (index == 0) { // Skip over the initial escape character
			return;					// We abuse its slot for other purposes.
		}
		
		int candidates=0;
		for (int x = 0; x < sequences.length; x++) {
			if (index >=sequences[x].length) {
				continue;
			}
			boolean matched=true;
			for (int y=1;y<=index;y++) { // Start at slot 1!
				if (buffer[y]!=sequences[x][y]) {
					matched=false;
					break;
				}
			}
			if (matched && sequences[x].length-1==index) {
				functionCode=sequences[x][0];
				complete=true;
				return;
			}
			if (matched && sequences[x].length>index) {
				candidates++;
			}
		}
		complete=(candidates==0);
	}
}
