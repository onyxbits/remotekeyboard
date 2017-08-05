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

import net.wimpi.telnetd.io.BasicTerminalIO;

import java.io.IOException;

/**
 * Class that implements an Editline
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
class Editline {

    //Aggregations (inner class!)
    private Buffer m_Buffer;
    //Members
    private BasicTerminalIO m_IO;
    private int m_Cursor = 0;
    private boolean m_InsertMode = true;
    private int m_LastSize = 0;
    private boolean m_HardWrapped = false;
    private char m_LastRead;
    private int m_LastCursPos = 0;

    /**
     * Constructs an Editline.
     */
    public Editline(BasicTerminalIO io) {
        m_IO = io;
        //allways full length
        m_Buffer = new Buffer(m_IO.getColumns() - 1);
        m_Cursor = 0;
        m_InsertMode = true;
    }//constructor

    /**
     * Accessor method for line buffer size.
     *
     * @ return int that represents the number of chars in the fields buffer.
     */
    public int size() {
        return m_Buffer.size();
    }//getSize

    public String getValue() {
        return m_Buffer.toString();
    }//getValue

    public void setValue(String str)
            throws BufferOverflowException, IOException {

        storeSize();
        //buffer
        m_Buffer.clear();
        //cursor
        m_Cursor = 0;

        //screen
        m_IO.moveLeft(m_LastSize);
        m_IO.eraseToEndOfLine();
        append(str);
    }//setValue

    public void clear() throws IOException {

        storeSize();
        //Buffer
        m_Buffer.clear();
        //Cursor
        m_Cursor = 0;
        //Screen
        draw();
    }//clear

    public String getSoftwrap()
            throws IndexOutOfBoundsException, IOException {

        //Wrap from Buffer
        String content = m_Buffer.toString();
        int idx = content.lastIndexOf(" ");
        if (idx == -1) {
            content = "";
        } else {
            //System.out.println("Line:softwrap:lastspace:"+idx);
            content = content.substring(idx + 1, content.length());

            //System.out.println("Line:softwrap:wraplength:"+content.length());

            //Cursor
            //remeber relative cursor pos
            m_Cursor = size();
            m_Cursor = m_Cursor - content.length();

            //buffer
            for (int i = 0; i < content.length(); i++) {
                m_Buffer.removeCharAt(m_Cursor);
            }

            //screen
            m_IO.moveLeft(content.length());
            m_IO.eraseToEndOfLine();

            //System.out.println("Line:softwrap:buffercontent:"+buf.toString());
        }
        return content + getLastRead();
    }//getSoftWrap


    public String getHardwrap()
            throws IndexOutOfBoundsException, IOException {

        //Buffer
        String content = m_Buffer.toString();
        content = content.substring(m_Cursor, content.length());
        //System.out.println("buffer:tostring:"+buf.toString()+":");
        //System.out.println("buffer:size:"+buf.size());
        int lastsize = m_Buffer.size();
        for (int i = m_Cursor; i < lastsize; i++) {
            m_Buffer.removeCharAt(m_Cursor);
            //System.out.println("buffer:removing char #"+i);
        }
        //System.out.println("buffer:tostring:"+buf.toString()+":");
        //cursor stays
        //screen
        m_IO.eraseToEndOfLine();
        return content;
    }//getHardWrap

    private void setCharAt(int pos, char ch)
            throws IndexOutOfBoundsException, IOException {

        //buffer
        m_Buffer.setCharAt(pos, ch);
        //cursor
        //implements overwrite mode no change
        //screen
        draw();
    }//setCharAt

    private void insertCharAt(int pos, char ch)
            throws BufferOverflowException, IndexOutOfBoundsException, IOException {

        storeSize();
        //buffer
        m_Buffer.ensureSpace(1);
        m_Buffer.insertCharAt(pos, ch);
        //cursor adjustment (so that it stays in "same" pos)
        if (m_Cursor >= pos) {
            m_Cursor++;
        }
        //screen
        draw();
    }//insertCharAt

    private void removeCharAt(int pos)
            throws IndexOutOfBoundsException, IOException {

        storeSize();
        //buffer
        m_Buffer.removeCharAt(pos);

        //cursor
        if (m_Cursor > pos) {
            m_Cursor--;
        }
        //screen
        draw();

    }//removeChatAt

    private void insertStringAt(int pos, String str)
            throws BufferOverflowException, IndexOutOfBoundsException, IOException {

        storeSize();
        //buffer
        m_Buffer.ensureSpace(str.length());
        for (int i = 0; i < str.length(); i++) {
            m_Buffer.insertCharAt(pos, str.charAt(i));
            //Cursor
            m_Cursor++;
        }
        //screen
        draw();

    }//insertStringAt

    public void append(char ch)
            throws BufferOverflowException, IOException {

        storeSize();
        //buffer
        m_Buffer.ensureSpace(1);
        m_Buffer.append(ch);
        //cursor
        m_Cursor++;
        //screen
        m_IO.write(ch);
    }//append(char)

    public void append(String str)
            throws BufferOverflowException, IOException {

        storeSize();
        //buffer
        m_Buffer.ensureSpace(str.length());
        for (int i = 0; i < str.length(); i++) {
            m_Buffer.append(str.charAt(i));
            //Cursor
            m_Cursor++;
        }
        //screen
        m_IO.write(str);
    }//append(String)

    public int getCursorPosition() {
        return m_Cursor;
    }//getCursorPosition

    public void setCursorPosition(int pos) {

        if (m_Buffer.size() < pos) {
            m_Cursor = m_Buffer.size();
        } else {
            m_Cursor = pos;
        }
        //System.out.println("Editline:cursor:"+Cursor);
    }//setCursorPosition

    private char getLastRead() {
        return m_LastRead;
    }//getLastRead

    private void setLastRead(char ch) {
        m_LastRead = ch;
    }//setLastRead


    public boolean isInInsertMode() {
        return m_InsertMode;
    }//isInInsertMode

    public void setInsertMode(boolean b) {
        m_InsertMode = b;
    }//setInsertMode

    public boolean isHardwrapped() {
        return m_HardWrapped;
    }//isHardwrapped

    public void setHardwrapped(boolean b) {
        m_HardWrapped = b;
    }//setHardwrapped

    /**
     * Method that will be
     * reading and processing input.
     */
    public int run() throws IOException {
        int in = 0;
        //draw();
        //myIO.flush();
        do {
            //get next key
            in = m_IO.read();
            //store cursorpos
            m_LastCursPos = m_Cursor;

            switch (in) {
                case BasicTerminalIO.LEFT:
                    if (!moveLeft()) {
                        return in;
                    }
                    break;
                case BasicTerminalIO.RIGHT:
                    if (!moveRight()) {
                        return in;
                    }
                    break;
                case BasicTerminalIO.BACKSPACE:
                    try {
                        if (m_Cursor == 0) {
                            return in;
                        } else {
                            removeCharAt(m_Cursor - 1);
                        }
                    } catch (IndexOutOfBoundsException ioobex) {
                        m_IO.bell();
                    }
                    break;
                case BasicTerminalIO.DELETE:
                    try {
                        removeCharAt(m_Cursor);
                    } catch (IndexOutOfBoundsException ioobex) {
                        m_IO.bell();
                    }
                    break;
                case BasicTerminalIO.ENTER:
                case BasicTerminalIO.UP:
                case BasicTerminalIO.DOWN:
                case BasicTerminalIO.TABULATOR:
                    return in;
                default:
                    try {
                        handleCharInput(in);
                    } catch (BufferOverflowException boex) {
                        setLastRead((char) in);
                        return in;
                    }
            }
            m_IO.flush();
        } while (true);
    }//run


    public void draw() throws IOException {
        //System.out.println("DEBUG: Buffer="+buf.toString());
        //System.out.println("DEBUG: Cursor="+Cursor);
        //System.out.println("DEBUG: lastcurspos="+lastcurspos);


    /*
    int diff=lastSize-buf.size();
    StringBuffer output=new StringBuffer(buf.toString());
    if(diff>0){
            for (int i=0;i<diff;i++) {
                output.append(" ");
            }
    }
    */

        m_IO.moveLeft(m_LastCursPos);
        m_IO.eraseToEndOfLine();
        m_IO.write(m_Buffer.toString());
        //adjust screen cursor hmm
        if (m_Cursor < m_Buffer.size()) {
            m_IO.moveLeft(m_Buffer.size() - m_Cursor);
        }
    }//draw

    private boolean moveRight() throws IOException {
        //cursor
        if (m_Cursor < m_Buffer.size()) {
            m_Cursor++;
            //screen
            m_IO.moveRight(1);
            return true;
        } else {
            return false;
        }
    }//moveRight

    private boolean moveLeft() throws IOException {
        //cursor
        if (m_Cursor > 0) {
            m_Cursor--;
            //screen
            m_IO.moveLeft(1);
            return true;
        } else {
            return false;
        }
    }//moveLeft

    private boolean isCursorAtEnd() {
        return (m_Cursor == m_Buffer.size());
    }//isCursorAtEnd

    private void handleCharInput(int ch)
            throws BufferOverflowException, IOException {

        if (isCursorAtEnd()) {
            append((char) ch);
        } else {
            if (isInInsertMode()) {
                try {
                    insertCharAt(m_Cursor, (char) ch);
                } catch (BufferOverflowException ex) {
                    //ignore buffer overflow on insert
                    m_IO.bell();
                }
            } else {
                setCharAt(m_Cursor, (char) ch);
            }
        }
    }//handleCharInput

    private void storeSize() {
        m_LastSize = m_Buffer.size();
    }//storeSize


    //inner class Buffer
    class Buffer extends CharBuffer {

        public Buffer(int size) {
            super(size);
        }//constructor

    }//class Buffer

}//class Editfield