//License
/***
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***/

package net.wimpi.telnetd.io.terminal;

/**
 * Utility class that provides methods and constants
 * for creating and handling colored/styled Strings.
 * The systems internal markup (Ctrl-a sequences) is
 * utilized.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public class ColorHelper {

    /**
     * Defines the internal marker string
     * for style/color markups.
     */
    public static final String INTERNAL_MARKER = "\001";
    /**
     * Defines the internal marker character code.
     */
    public static final int MARKER_CODE = 1;
    /**
     * Defines the markup representation of the color
     * black.
     */
    public static final String BLACK = "S";
    /**
     * Defines the markup representation of the color
     * red.
     */
    public static final String RED = "R";
    /**
     * Defines the markup representation of the color
     * green.
     */
    public static final String GREEN = "G";
    /**
     * Defines the markup representation of the color
     * yellow.
     */
    public static final String YELLOW = "Y";
    /**
     * Defines the markup representation of the color
     * blue.
     */
    public static final String BLUE = "B";
    /**
     * Defines the markup representation of the color magenta.
     */
    public static final String MAGENTA = "M";
    /**
     * Defines the markup representation of the color
     * cyan.
     */
    public static final String CYAN = "C";
    /**
     * Defines the markup representation of the color
     * white.
     */
    public static final String WHITE = "W";
    /**
     * Defines the markup representation of the activator
     * for style bold (normally represented by high intensity).
     */
    public static final String BOLD = "f";
    /**
     * Defines the markup representation of the deactivator
     * for style bold.
     */
    public static final String BOLD_OFF = "d"; //normal color or normal intensity
    /**
     * Defines the markup representation of the activator
     * for style italic.
     */
    public static final String ITALIC = "i";
    /**
     * Defines the markup representation of the deactivator
     * for style italic.
     */
    public static final String ITALIC_OFF = "j";
    /**
     * Defines the markup representation of the activator
     * for style underlined.
     */
    public static final String UNDERLINED = "u";
    /**
     * Defines the markup representation of the deactivator
     * for style underlined.
     */
    public static final String UNDERLINED_OFF = "v";
    /**
     * Defines the markup representation of the activator
     * for style blinking.
     */
    public static final String BLINK = "e";
    /**
     * Defines the markup representation of the deactivator
     * for style blinking (i.e. steady)
     */
    public static final String BLINK_OFF = "n";
    /**
     * Defines the markup representation of the graphics rendition
     * reset.<br>
     * It will reset all set colors and styles.
     */
    public static final String RESET_ALL = "a";

    /**
     * Creates a string with the given textcolor.
     *
     * @param str   String to be colorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String colorizeText(String str, String color) {
        return INTERNAL_MARKER + color + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeText

    /**
     * Creates a string with the given textcolor.
     *
     * @param str    String to be colorized.
     * @param color  Constant defined color (see constants).
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.<code>true<code> if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText(String str, String color, boolean autoff) {
        if (autoff) {
            return colorizeText(str, color);
        } else {
            return INTERNAL_MARKER + color + str;
        }
    }//colorizeText

    /**
     * Creates a string with the given backgroundcolor.
     *
     * @param str   String to be colorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String colorizeBackground(String str, String color) {
        return INTERNAL_MARKER + color.toLowerCase()
                + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBackground

    /**
     * Creates a string with the given backgroundcolor.
     *
     * @param str    String to be colorized.
     * @param color  Constant defined color (see constants).
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.<code>true<code> if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeBackground(String str,
            String color, boolean autoff) {
        if (autoff) {
            return colorizeBackground(str, color);
        } else {
            return INTERNAL_MARKER + color.toLowerCase() + str;
        }
    }//colorizeBackground

    /**
     * Creates a string with the given foreground and backgroundcolor.
     *
     * @param str String to be colorized.
     * @param fgc Constant defined color (see constants). Will be textcolor.
     * @param bgc Constant defined color (see constants). Will be backgroundcolor.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText(String str, String fgc, String bgc) {
        return INTERNAL_MARKER + fgc + INTERNAL_MARKER
                + bgc.toLowerCase() + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeText

    /**
     * Creates a string with the given foreground and backgroundcolor.
     *
     * @param str    String to be colorized.
     * @param fgc    Constant defined color (see constants). Will be textcolor.
     * @param bgc    Constant defined color (see constants). Will be backgroundcolor.
     * @param autoff boolean that toggles automatical appending of "attributes off"
     *               sequence.<code>true<code> if "attributes off" should be appended, false
     *               otherwise.
     * @return String with internal markup-sequences.
     */
    public static String colorizeText(String str, String fgc,
            String bgc, boolean autoff) {
        if (autoff) {
            return colorizeText(str, fgc, bgc);
        } else {
            return INTERNAL_MARKER + fgc + INTERNAL_MARKER
                    + bgc.toLowerCase() + str;
        }
    }//colorizeText

    /**
     * Creates a string with high intensity (bold) in the given textcolor.
     *
     * @param str   String to be boldcolorized.
     * @param color Constant defined color (see constants).
     * @return String with internal markup-sequences.
     */
    public static String boldcolorizeText(String str, String color) {
        return INTERNAL_MARKER + BOLD + INTERNAL_MARKER + color + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBoldText

    /**
     * Creates a string with the given high intensity foregroundcolor
     * and the given backgroundcolor.
     *
     * @param str String to be colorized.
     * @param fgc Constant defined color (see constants). Will be bold textcolor.
     * @param bgc Constant defined color (see constants). Will be backgroundcolor.
     * @return String with internal markup-sequences.
     */
    public static String boldcolorizeText(String str, String fgc, String bgc) {
        return INTERNAL_MARKER + BOLD + INTERNAL_MARKER + fgc + INTERNAL_MARKER + bgc.toLowerCase()
                + str + INTERNAL_MARKER + RESET_ALL;
    }//colorizeBoldText

    /**
     * Creates a string with high intensity (bold).
     *
     * @param str String to be styled bold.
     * @return String with internal markup-sequences.
     */
    public static String boldText(String str) {
        return INTERNAL_MARKER + BOLD + str + INTERNAL_MARKER + BOLD_OFF;
    }//boldText

    /**
     * Creates a string with italic style.
     *
     * @param str String to be styled italic.
     * @return String with internal markup-sequences.
     */
    public static String italicText(String str) {
        return INTERNAL_MARKER + ITALIC + str + INTERNAL_MARKER + ITALIC_OFF;
    }//italicText

    /**
     * Creates an underlined string.
     *
     * @param str String to be styled underlined.
     * @return String with internal markup-sequences.
     */
    public static String underlinedText(String str) {
        return INTERNAL_MARKER + UNDERLINED + str + INTERNAL_MARKER + UNDERLINED_OFF;
    }//underlinedText

    /**
     * Creates a blinking string.
     *
     * @param str String to be styled blinking.
     * @return String with internal markup-sequences.
     */
    public static String blinkingText(String str) {
        return INTERNAL_MARKER + BLINK + str + INTERNAL_MARKER + BLINK_OFF;
    }//blinkingText

    /**
     * Returns the length of the visible string calculated
     * from the internal marked-up string passed as parameter.
     *
     * @param str String with internal color/style markups.
     * @return long Representing the length of the visible string..
     */
    public static long getVisibleLength(String str) {
        int counter = 0;
        int parsecursor = 0;
        int foundcursor = 0;

        boolean done = false;

        while (!done) {
            foundcursor = str.indexOf(MARKER_CODE, parsecursor);
            if (foundcursor != -1) {
                //increment counter
                counter++;
                //parseon from the next char
                parsecursor = foundcursor + 1;
            } else {
                done = true;
            }
        }

        return (str.length() - (counter * 2));
    }//getVisibleLength

}//class ColorHelper