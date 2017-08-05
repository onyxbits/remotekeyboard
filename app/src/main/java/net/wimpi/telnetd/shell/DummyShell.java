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

package net.wimpi.telnetd.shell;

import android.util.Log;

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.io.terminal.ColorHelper;
import net.wimpi.telnetd.io.terminal.TerminalManager;
import net.wimpi.telnetd.io.toolkit.BufferOverflowException;
import net.wimpi.telnetd.io.toolkit.Checkbox;
import net.wimpi.telnetd.io.toolkit.Editarea;
import net.wimpi.telnetd.io.toolkit.Editfield;
import net.wimpi.telnetd.io.toolkit.InputFilter;
import net.wimpi.telnetd.io.toolkit.Label;
import net.wimpi.telnetd.io.toolkit.Pager;
import net.wimpi.telnetd.io.toolkit.Point;
import net.wimpi.telnetd.io.toolkit.Selection;
import net.wimpi.telnetd.io.toolkit.Statusbar;
import net.wimpi.telnetd.io.toolkit.Titlebar;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionData;
import net.wimpi.telnetd.net.ConnectionEvent;

import java.io.IOException;

/**
 * This class is an example implmentation of a Shell.<br>
 * It is used for testing the system.<br>
 * At the moment you can see all io toolkit classes in action,
 * pressing "t" at its prompt (instead of the enter, which is
 * requested for logging out again).
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 */
public class DummyShell
        implements Shell {

    private static final String TAG = DummyShell.class.getSimpleName();
    //Constants
    private static final String logo =
            "/***\n" +
                    "* \n" +
                    "* TelnetD library (embeddable telnet daemon)\n" +
                    "* Copyright (C) 2000-2005 Dieter Wimberger\n" +
                    "***/\n" +
                    "A looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo" +
                    "ng line!";
    private Connection m_Connection;
    private BasicTerminalIO m_IO;
    private Editfield m_EF;

    public static Shell createShell() {
        return new DummyShell();
    }//createShell

    /**
     * Method that runs a shell
     *
     * @param con Connection that runs the shell.
     */
    public void run(Connection con) {
        try {
            m_Connection = con;
            //mycon.setNextShell("nothing");
            m_IO = m_Connection.getTerminalIO();
            //dont forget to register listener
            m_Connection.addConnectionListener(this);

            //clear the screen and start from zero
            m_IO.eraseScreen();
            m_IO.homeCursor();

            //We just read any key
            m_IO.write("Dummy Shell. Please press enter to logout!\r\n");
            m_IO.flush();
            boolean done = false;
            while (!done) {
                int i = m_IO.read();
                if (i == -1 || i == -2) {
                    Log.d(TAG, "Input(Code):" + i);
                    done = true;
                }
                if (i == 10) {
                    done = true;

                } else if (i == 117) {

                    ConnectionData cd = m_Connection.getConnectionData();
                    //output header
                    m_IO.write(BasicTerminalIO.CRLF +
                            "DEBUG: Active Connection" +
                            BasicTerminalIO.CRLF);
                    m_IO.write("------------------------" + BasicTerminalIO.CRLF);

                    //output connection data
                    m_IO.write("Connected from: " + cd.getHostName() +
                            "[" + cd.getHostAddress() + ":" + cd.getPort() + "]"
                            + BasicTerminalIO.CRLF);
                    m_IO.write("Guessed Locale: " + cd.getLocale() +
                            BasicTerminalIO.CRLF);
                    m_IO.write(BasicTerminalIO.CRLF);
                    //output negotiated terminal properties
                    m_IO.write("Negotiated Terminal Type: " +
                            cd.getNegotiatedTerminalType() + BasicTerminalIO.CRLF);
                    m_IO.write("Negotiated Columns: " + cd.getTerminalColumns() +
                            BasicTerminalIO.CRLF);
                    m_IO.write("Negotiated Rows: " + cd.getTerminalRows() +
                            BasicTerminalIO.CRLF);

                    //output of assigned terminal instance (the cast is a hack, please
                    //do not copy for other TCommands, because it would break the
                    //decoupling of interface and implementation!
                    m_IO.write(BasicTerminalIO.CRLF);
                    m_IO.write("Assigned Terminal instance: " +
                            ((TerminalIO) m_IO).getTerminal());
                    m_IO.write(BasicTerminalIO.CRLF);
                    m_IO.write("Environment: " + cd.getEnvironment().toString());
                    m_IO.write(BasicTerminalIO.CRLF);
                    //output footer
                    m_IO.write("-----------------------------------------------" +
                            BasicTerminalIO.CRLF + BasicTerminalIO.CRLF);

                    m_IO.flush();


                } else if (i == 'e') {
                    //run editfield test
                    Label l = new Label(m_IO, "testedit", "TestEdit: ");
                    m_EF = new Editfield(m_IO, "edit", 50);
                    m_EF.registerInputFilter(new InputFilter() {

                        public int filterInput(int key) throws IOException {
                            if (key == 't') {
                                try {
                                    m_EF.setValue("Test");
                                } catch (BufferOverflowException e) {

                                }
                                return InputFilter.INPUT_HANDLED;
                            } else if (key == 'c') {
                                m_EF.clear();
                                return InputFilter.INPUT_HANDLED;
                            } else {

                                return key;
                            }
                        }
                    });
                    l.draw();
                    m_EF.run();
                } else if (i == 116) {
                    //run test sequence

                    Pager pg = new Pager(m_IO);
                    pg.setShowPosition(true);
                    pg.page(logo + logo + logo + logo + logo + logo + logo + logo + logo + logo
                            + logo);

                    Label l = new Label(m_IO, "label1");
                    l.setText("Hello World!");
                    l.setLocation(new Point(1, 5));
                    l.draw();
                    m_IO.flush();

                    m_IO.homeCursor();
                    m_IO.eraseScreen();
                    Titlebar tb = new Titlebar(m_IO, "title 1");
                    tb.setTitleText("MyTitle");
                    tb.setAlignment(Titlebar.ALIGN_CENTER);
                    tb.setBackgroundColor(ColorHelper.BLUE);
                    tb.setForegroundColor(ColorHelper.YELLOW);
                    tb.draw();


                    Statusbar sb = new Statusbar(m_IO, "status 1");
                    sb.setStatusText("MyStatus");
                    sb.setAlignment(Statusbar.ALIGN_LEFT);
                    sb.setBackgroundColor(ColorHelper.BLUE);
                    sb.setForegroundColor(ColorHelper.YELLOW);
                    sb.draw();

                    m_IO.flush();

                    m_IO.setCursor(2, 1);

                    Selection sel = new Selection(m_IO, "selection 1");
                    String[] tn = TerminalManager.getReference().getAvailableTerminals();

                    for (int n = 0; n < tn.length; n++) {
                        sel.addOption(tn[n]);
                    }

                    sel.setLocation(1, 10);
                    sel.run();

                    Checkbox cb = new Checkbox(m_IO, "checkbox 1");
                    cb.setText("Check me !");
                    cb.setLocation(1, 12);
                    cb.run();

                    Editfield ef = new Editfield(m_IO, "editfield 1", 20);
                    ef.setLocation(1, 13);
                    ef.run();
                    try {
                        ef.setValue("SETVALUE!");
                    } catch (Exception ex) {

                    }
                    Editfield ef2 = new Editfield(m_IO, "editfield 2", 8);
                    ef2.setLocation(1, 14);
                    ef2.setPasswordField(true);
                    ef2.run();

                    Log.d(TAG, "Your secret password was:" + ef2.getValue());
                    m_IO.flush();

                    //clear the screen and start from zero
                    m_IO.eraseScreen();
                    m_IO.homeCursor();
                    //myio.flush();
                    Titlebar tb2 = new Titlebar(m_IO, "title 1");
                    tb2.setTitleText("jEditor v0.1");
                    tb2.setAlignment(Titlebar.ALIGN_LEFT);
                    tb2.setBackgroundColor(ColorHelper.BLUE);
                    tb2.setForegroundColor(ColorHelper.YELLOW);
                    tb2.draw();

                    Statusbar sb2 = new Statusbar(m_IO, "status 1");
                    sb2.setStatusText("Status");
                    sb2.setAlignment(Statusbar.ALIGN_LEFT);
                    sb2.setBackgroundColor(ColorHelper.BLUE);
                    sb2.setForegroundColor(ColorHelper.YELLOW);
                    sb2.draw();

                    m_IO.setCursor(2, 1);

                    Editarea ea = new Editarea(m_IO, "area", m_IO.getRows() - 2, 100);
                    m_IO.flush();
                    ea.run();
                    Log.d(TAG, ea.getValue());

                    m_IO.eraseScreen();
                    m_IO.homeCursor();
                    m_IO.write("Dummy Shell. Please press enter to logout!\r\n");
                    m_IO.flush();


                }
                //the next line is for debug reasons
                else {
                    Log.d(TAG, "Input(Code):" + i);
                }
            }
            m_IO.homeCursor();
            m_IO.eraseScreen();
            m_IO.write("Goodbye!.\r\n\r\n");
            m_IO.flush();

        } catch (Exception ex) {
            Log.e(TAG, "run()", ex);
        }
    }//run

    //this implements the ConnectionListener!
    public void connectionTimedOut(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_TIMEDOUT");
            m_IO.flush();
            //close connection
            m_Connection.close();
        } catch (Exception ex) {
            Log.e(TAG, "connectionTimedOut()", ex);
        }
    }//connectionTimedOut

    public void connectionIdle(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_IDLE");
            m_IO.flush();
        } catch (IOException e) {
            Log.e(TAG, "connectionIdle()", e);
        }

    }//connectionIdle

    public void connectionLogoutRequest(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_LOGOUTREQUEST");
            m_IO.flush();
        } catch (Exception ex) {
            Log.e(TAG, "connectionLogoutRequest()", ex);
        }
    }//connectionLogout

    public void connectionSentBreak(ConnectionEvent ce) {
        try {
            m_IO.write("CONNECTION_BREAK");
            m_IO.flush();
        } catch (Exception ex) {
            Log.e(TAG, "connectionSentBreak()", ex);
        }
    }//connectionSentBreak


}//class DummyShell
