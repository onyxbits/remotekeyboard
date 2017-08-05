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

package net.wimpi.telnetd.net;

import android.util.Log;

import net.wimpi.telnetd.BootException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Class that implements a <tt>PortListener</tt>.<br>
 * If available, it accepts incoming connections and passes them
 * to an associated <tt>ConnectionManager</tt>.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 * @see net.wimpi.telnetd.net.ConnectionManager
 */
public class PortListener
        implements Runnable {


    private static final String TAG = PortListener.class.getSimpleName();
    private static final String logmsg =
            "Listening to Port {0,number,integer} with a connectivity queue size of {1,number,"
                    + "integer}.";
    private String m_Name;
    private int m_Port;                                        //port number running on
    private int m_FloodProtection;                        //flooding protection
    private ServerSocket m_ServerSocket = null; //server socket
    private Thread m_Thread;
    private ConnectionManager connectionManager;    //connection management thread
    private boolean m_Stopping = false;
    private boolean m_Available;                    //Flag for availability

    /**
     * Constructs a PortListener instance.<br>
     *
     * @param port      int that specifies the port number of the server socket.
     * @param floodprot that specifies the server socket queue size.
     */
    public PortListener(String name, int port, int floodprot) {
        m_Name = name;
        m_Available = false;
        m_Port = port;
        m_FloodProtection = floodprot;
    }//constructor

    /**
     * Factory method for a PortListener instance, returns
     * an instance of a PortListener with an associated ConnectionManager.
     *
     * @param settings Properties that contain all configuration information.
     */
    public static PortListener createPortListener(String name, Properties settings)
            throws BootException {

        PortListener pl = null;

        try {
            //1. read settings of the port listener itself
            int port = Integer.parseInt(settings.getProperty(name + ".port"));
            int floodprot = Integer.parseInt(settings.getProperty(name + ".floodprotection"));

            if (new Boolean(settings.getProperty(name + ".secure")).booleanValue()) {
                //do nothing for now, probably set factory in the future
            }
            pl = new PortListener(name, port, floodprot);
        } catch (Exception ex) {
            Log.e(TAG, "createPortListener()", ex);
            throw new BootException("Failure while creating PortListener instance:\n" +
                    ex.getMessage());
        }

        //2. factorize a ConnectionManager, passing the settings, if we do not have one yet
        if (pl.connectionManager == null) {
            pl.connectionManager = ConnectionManager.createConnectionManager(name, settings);
            try {
                pl.connectionManager.start();
            } catch (Exception exc) {
                Log.e(TAG, "createPortListener()", exc);
                throw new BootException(
                        "Failure while starting ConnectionManager watchdog thread:\n" +
                                exc.getMessage());
            }
        }
        return pl;
    }//createPortListener

    /**
     * Returns the name of this <tt>PortListener</tt>.
     *
     * @return the name as <tt>String</tt>.
     */
    public String getName() {
        return m_Name;
    }//getName

    /**
     * Tests if this <tt>PortListener</tt> is available.
     *
     * @return true if available, false otherwise.
     */
    public boolean isAvailable() {
        return m_Available;
    }//isAvailable

    /**
     * Sets the availability flag of this <tt>PortListener</tt>.
     *
     * @param b true if to be available, false otherwise.
     */
    public void setAvailable(boolean b) {
        m_Available = b;
    }//setAvailable

    /**
     * Starts this <tt>PortListener</tt>.
     */
    public void start() {
        Log.d(TAG, "start()");
        m_Thread = new Thread(this);
        m_Thread.start();
        m_Available = true;
    }//start

    /**
     * Stops this <tt>PortListener</tt>, and returns
     * when everything was stopped successfully.
     */
    public void stop() {
        Log.d(TAG, "stop()::" + this.toString());
        //flag stop
        m_Stopping = true;
        m_Available = false;
        //take down all connections
        connectionManager.stop();

        //close server socket
        try {
            m_ServerSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "stop()", ex);
        }

        //wait for thread to die
        try {
            m_Thread.join();
        } catch (InterruptedException iex) {
            Log.e(TAG, "stop()", iex);
        }

        Log.i(TAG, "stop()::Stopped " + this.toString());
    }//stop

    /**
     * Listen constantly to a server socket and handles incoming connections
     * through the associated {a:link ConnectionManager}.
     *
     * @see net.wimpi.telnetd.net.ConnectionManager
     */
    public void run() {
        try {
      /*
          A server socket is opened with a connectivity queue of a size specified
          in int floodProtection.  Concurrent login handling under normal circumstances
          should be handled properly, but denial of service attacks via massive parallel
          program logins should be prevented with this.
      */
            m_ServerSocket = new ServerSocket(m_Port, m_FloodProtection);

            //log entry
            Object[] args = {new Integer(m_Port), new Integer(m_FloodProtection)};
            Log.i(TAG, MessageFormat.format(logmsg, args));

            do {
                try {
                    Socket s = m_ServerSocket.accept();
                    if (m_Available) {
                        connectionManager.makeConnection(s);
                    } else {
                        //just shut down the socket
                        s.close();
                    }
                } catch (SocketException ex) {
                    if (m_Stopping) {
                        //server socket was closed blocked in accept
                        Log.d(TAG, "run(): ServerSocket closed by stop()");
                    } else {
                        Log.e(TAG, "run()", ex);
                    }
                }
            } while (!m_Stopping);

        } catch (IOException e) {
            Log.e(TAG, "run()", e);
        }
        Log.d(TAG, "run(): returning.");
    }//run

    /**
     * Returns reference to ConnectionManager instance associated
     * with the PortListener.
     *
     * @return the associated ConnectionManager.
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }//getConnectionManager

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

}//class PortListener
