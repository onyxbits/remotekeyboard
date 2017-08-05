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

package net.wimpi.telnetd.io.toolkit;

import java.util.Vector;

/**
 * Class implementing a character buffer.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
class CharBuffer {

    //Members
    private Vector m_Buffer;
    private int m_Size;

    public CharBuffer(int size) {
        m_Buffer = new Vector(size);
        m_Size = size;
    }//constructor

    public char getCharAt(int pos)
            throws IndexOutOfBoundsException {

        return ((Character) m_Buffer.elementAt(pos)).charValue();
    }//getCharAt

    public void setCharAt(int pos, char ch)
            throws IndexOutOfBoundsException {

        m_Buffer.setElementAt(new Character(ch), pos);
    }//setCharAt

    public void insertCharAt(int pos, char ch)
            throws BufferOverflowException, IndexOutOfBoundsException {

        m_Buffer.insertElementAt(new Character(ch), pos);
    }//insertCharAt

    public void append(char aChar)
            throws BufferOverflowException {

        m_Buffer.addElement(new Character(aChar));
    }//append

    public void append(String str)
            throws BufferOverflowException {
        for (int i = 0; i < str.length(); i++) {
            append(str.charAt(i));
        }
    }//append

    public void removeCharAt(int pos)
            throws IndexOutOfBoundsException {

        m_Buffer.removeElementAt(pos);
    }//removeCharAt

    public void clear() {
        m_Buffer.removeAllElements();
    }//clear

    public int size() {
        return m_Buffer.size();
    }//size

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < m_Buffer.size(); i++) {
            sbuf.append(((Character) m_Buffer.elementAt(i)).charValue());
        }
        return sbuf.toString();
    }//toString

    public void ensureSpace(int chars)
            throws BufferOverflowException {

        if (chars > (m_Size - m_Buffer.size())) {
            throw new BufferOverflowException();
        }
    }//ensureSpace

}//class CharBuffer