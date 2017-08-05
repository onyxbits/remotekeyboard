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

import net.wimpi.telnetd.io.BasicTerminalIO;
import net.wimpi.telnetd.io.TerminalIO;
import net.wimpi.telnetd.shell.Shell;
import net.wimpi.telnetd.shell.ShellManager;
import android.util.Log;

import java.util.Vector;

/**
 * Class that implements a connection with this telnet daemon.<br>
 * It is derived from java.lang.Thread, which reflects the architecture
 * constraint of one thread per connection. This might seem a waste of
 * resources, but as a matter of fact sharing threads would require a
 * far more complex imlementation, due to the fact that telnet is not a
 * stateless protocol (i.e. alive throughout a session of multiple requests
 * and responses).<br>
 * Each Connection instance is created by the listeners ConnectionManager
 * instance, making it part of a threadgroup and passing in an associated
 * ConnectionData instance, that holds vital information about the connection.
 * Be sure to take a look at their documention.<br>
 * <p/>
 * Once the thread has started and is running, it will get a login
 * shell instance from the ShellManager and run passing its own reference.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 * @see net.wimpi.telnetd.net.ConnectionManager
 * @see net.wimpi.telnetd.net.ConnectionData
 * @see net.wimpi.telnetd.shell.ShellManager
 * @see net.wimpi.telnetd.io.TerminalIO
 */
public class Connection
    extends Thread {

  private static final String TAG = Connection.class.getSimpleName();
  private static int m_Number;			//unique number for a thread in the thread group
  private boolean m_Dead;
  private Vector m_Listeners;

  //Associations
  private ConnectionData m_ConnectionData;	//associated information
  private BasicTerminalIO m_TerminalIO;		//associated terminal io
  private Shell m_NextShell = null;				//next shell to be run

  /**
   * Constructs a TelnetConnection by invoking its parent constructor
   * and setting of various members.<br>
   * Subsequently instantiates the whole i/o subsystem, negotiating
   * telnet protocol level options etc.<br>
   *
   * @param tcg ThreadGroup that this instance is running in.
   * @param cd  ConnectionData instance containing all vital information
   *            of this connection.
   * @see net.wimpi.telnetd.net.ConnectionData
   */
  public Connection(ThreadGroup tcg, ConnectionData cd) {
    super(tcg, ("Connection" + (++m_Number)));

    m_ConnectionData = cd;
    //init the connection listeners for events
    //(there should actually be only one or two)
    m_Listeners = new Vector(3);
    m_TerminalIO = new TerminalIO(this);
    m_Dead = false;
  }//constructor

  /**
   * Method overloaded to implement following behaviour:
   * <ol>
   * <li> On first entry, retrieve an instance of the configured
   * login shell from the ShellManager and run it.
   * <li> Handle a shell switch or close down disgracefully when
   * problems (i.e. unhandled unchecked exceptions) occur in the
   * running shell.
   * </ol>
   */
  public void run() {

    boolean done = false;

    try {
      Shell sh = ShellManager.getReference().getShell(m_ConnectionData.getLoginShell());
      do {
        sh.run(this);
        if (m_Dead) {
          done = true;
          break;
        }
        sh = getNextShell();
        if (sh == null) {
          done = true;
        }
      } while (!done || m_Dead);

    } catch (Exception ex) {
      Log.e(TAG, "run()", ex); //Handle properly
    } finally {
      //call close if not dead already
      if (!m_Dead) {
        close();
      }
    }
    Log.d(TAG, "run():: Returning from " + this.toString());
  }//run

  /**
   * Method to access the associated connection data.
   *
   * @return ConnectionData associated with the Connection instance.
   * @see net.wimpi.telnetd.net.ConnectionData
   */
  public ConnectionData getConnectionData() {
    return m_ConnectionData;
  }//getConnectionData

  /**
   * Method to access the associated terminal io.
   *
   * @return BasicTerminalIO associated with the Connection instance.
   * @see net.wimpi.telnetd.io.BasicTerminalIO
   */
  public BasicTerminalIO getTerminalIO() {
    return m_TerminalIO;
  }//getTerminalIO

  /**
   * Method to prepare the Connection for a shell switch.<br>
   * A shell instance will be acquired from the ShellManager
   * according to the given name.<br>
   * In case of a nonexistant name the return will be false,
   * otherwise true.
   *
   * @param name String that should represent a valid shell name.
   * @return boolean flagging if the request could be carried out correctly.
   * @see net.wimpi.telnetd.shell.ShellManager
   */
  public boolean setNextShell(String name) {
    m_NextShell = ShellManager.getReference().getShell(name);
    if (m_NextShell == null) {
      return false;
    } else {
      return true;
    }
  }//setNextShell

  /**
   * Method used internally to retrieve the next shell
   * to be run. Its like a one-slot stack, so that we dont
   * end up in a never ending story.
   */
  private Shell getNextShell() {
    //get shell
    Shell shell = m_NextShell;

    if (shell != null) {
      //empty single queue
      m_NextShell = null;
      //return it
      return shell;
    } else {
      return null;
    }
  }//getNextShell

  /**
   * Closes the connection and its underlying i/o and network
   * resources.<br>
   */
  public synchronized void close() {
    if (m_Dead) {
      return;
    } else {
      try {
        //connection dead
        m_Dead = true;
        //close i/o
        m_TerminalIO.close();
      } catch (Exception ex) {
        Log.e(TAG, "close()", ex);
        //handle
      }
      try {
        //close socket
        m_ConnectionData.getSocket().close();
      } catch (Exception ex) {
        Log.e(TAG, "close()", ex);
        //handle
      }
     try {
        //register closed connection in ConnectionManager
        m_ConnectionData.getManager().registerClosedConnection(this);
      } catch (Exception ex) {
        Log.e(TAG, "close()", ex);
        //handle
      }
     try {
        //try to interrupt it
        interrupt();
      } catch (Exception ex) {
        Log.e(TAG, "close()", ex);
        //handle
      }


      Log.d(TAG, "Closed " + this.toString() + " and inactive.");
    }
  }//close

  /**
   * Returns if a connection has been closed.<br>
   *
   * @return the state of the connection.
   */
  public boolean isActive() {
    return !m_Dead;
  }//isClosed

  /****** Event handling ****************/

  /**
   * Method that registers a ConnectionListener with the
   * Connection instance.
   *
   * @param cl ConnectionListener to be registered.
   * @see net.wimpi.telnetd.net.ConnectionListener
   */
  public void addConnectionListener(ConnectionListener cl) {
    m_Listeners.addElement(cl);
  }//addConnectionListener

  /**
   * Method that removes a ConnectionListener from the
   * Connection instance.
   *
   * @param cl ConnectionListener to be removed.
   * @see net.wimpi.telnetd.net.ConnectionListener
   */
  public void removeConnectionListener(ConnectionListener cl) {
    m_Listeners.removeElement(cl);
  }//removeConnectionListener


  /**
   * Method called by the io subsystem to pass on a
   * "low-level" event. It will be properly delegated to
   * all registered listeners.
   *
   * @param ce ConnectionEvent to be processed.
   * @see net.wimpi.telnetd.net.ConnectionEvent
   */
  public void processConnectionEvent(ConnectionEvent ce) {
    for (int i = 0; i < m_Listeners.size(); i++) {
      ConnectionListener cl = (ConnectionListener) m_Listeners.elementAt(i);
      if (ce.isType(ConnectionEvent.CONNECTION_IDLE)) {
        cl.connectionIdle(ce);
      } else if (ce.isType(ConnectionEvent.CONNECTION_TIMEDOUT)) {
        cl.connectionTimedOut(ce);
      } else if (ce.isType(ConnectionEvent.CONNECTION_LOGOUTREQUEST)) {
        cl.connectionLogoutRequest(ce);
      //}
      //else if (ce.isType(ConnectionEvent.CONNECTION_BROKEN)) {
      //  cl.connectionBroken(ce);
      } else if (ce.isType(ConnectionEvent.CONNECTION_BREAK)) {
        cl.connectionSentBreak(ce);
      }
    }
  }//processConnectionEvent

}//class Connection
