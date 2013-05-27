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

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Locale;

/**
 * An utility class that is used to store and allow retrieval
 * of all data associated with a connection.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 * @see net.wimpi.telnetd.net.Connection
 */
public class ConnectionData {

  //Associations
  private ConnectionManager m_CM;		//the connection's ConnectionManager
  private Socket m_Socket;			    //the connection's socket
  private InetAddress m_IP;				  //the connection's IP Address Object
  private HashMap m_Environment;  //the environment

  //Members
  private String m_HostName;						//cache for the hostname
  private String m_HostAddress;						//cache for the host ip
  private int m_Port;								//port of the connection
  private Locale m_Locale;						//locale of the connection
  private long m_LastActivity;						//timestamp for the last activity
  private boolean m_Warned;							//warned flag
  private String m_NegotiatedTerminalType;			//negotiated TerminalType as String
  private int[] m_TerminalGeometry;					//negotiated terminal geometry
  private boolean m_TerminalGeometryChanged = true;	//flag for changes in the terminal geometry
  private String m_LoginShell;       //the login shell
  private boolean m_LineMode = false;
  private String m_EchoMode = "server";

  /**
   * Constructs a ConnectionData instance storing vital
   * information about a connection.
   *
   * @param sock Socket of the inbound connection.
   */
  public ConnectionData(Socket sock, ConnectionManager cm) {
    m_Socket = sock;
    m_CM = cm;
    m_IP = sock.getInetAddress();
    setHostName();
    setHostAddress();
    setLocale();
    m_Port = sock.getPort();
    //this will set a default geometry and terminal type for the terminal
    m_TerminalGeometry = new int[2];
    m_TerminalGeometry[0] = 80;	//width
    m_TerminalGeometry[1] = 25;	//height
    m_NegotiatedTerminalType = "default";
    m_Environment = new HashMap(20);
    //this will stamp the first activity for validity :)
    activity();
  }//ConnectionData


  /**
   * Returns a reference to the ConnectionManager the
   * connection is associated with.
   *
   * @return Reference to the associated ConnectionManager.
   * @see net.wimpi.telnetd.net.ConnectionManager
   */
  public ConnectionManager getManager() {
    return m_CM;
  }//getManager

  /**
   * Returns a reference to the socket the Connection
   * is associated with.
   *
   * @return Reference to the associated Socket.
   * @see java.net.Socket
   */
  public Socket getSocket() {
    return m_Socket;
  }//getSocket

  /**
   * Returns the remote port to which the socket is connected.
   *
   * @return String that contains the remote port number to which the socket is connected.
   */
  public int getPort() {
    return m_Port;
  }//getPort

  /**
   * Returns the fully qualified host name for the connection's IP address.<br>
   * The name is cached on creation for performance reasons. Subsequent calls
   * will not result in resolve queries.
   *
   * @return String that contains the fully qualified host name for this address.
   */
  public String getHostName() {
    return m_HostName;
  }//getHostName

  /**
   * Returns the IP address of the connection.
   *
   * @return String that contains the connection's IP address.<br>
   *         The format "%d.%d.%d.%d" is well known, where %d goes from zero to 255.
   */
  public String getHostAddress() {
    return m_HostAddress;
  }//getHostAddress

  /**
   * Returns the InetAddress object associated with the connection.
   *
   * @return InetAddress associated with the connection.
   */
  public InetAddress getInetAddress() {
    return m_IP;
  }//getInetAddress

  /**
   * Returns the Locale object associated with the connection
   * by carrying out a simple domain match. <br>
   * This can either be effective, if your users are really
   * home in the country they are connecting from,
   * or ineffective if they are on the move getting connected
   * from anywhere in the world.<br>
   * <br>
   * Yet this gives the chance of capturing a default locale
   * and starting from some point. On application context
   * this can be by far better handled, so be aware that
   * it makes sense to spend some thoughts on that thing when you
   * build your application.
   *
   * @return the Locale object "guessed" for the connection based
   *         on its host name.
   */
  public Locale getLocale() {
    return m_Locale;
  }//getLocale


  /**
   * Returns a timestamp of the last activity that happened on
   * the associated connection.
   *
   * @return the timestamp as a long representing the difference,
   *         measured in milliseconds, between the current time and
   *         midnight, January 1, 1970 UTC.
   */
  public long getLastActivity() {
    return m_LastActivity;
  }//getLastActivity


  /**
   * Sets a new timestamp to the actual time in millis
   * retrieved from the System. This will remove an idle warning
   * flag if it has been set. Note that you can use this behaviour
   * to implement your own complex idle timespan policies within
   * the context of your application.<br>
   * The check frequency of the ConnectionManager should just be set
   * according to the lowest time to warning and time to disconnect
   * requirements.
   */
  public void activity() {
    m_Warned = false;
    m_LastActivity = System.currentTimeMillis();
  }//setLastActivity

  /**
   * Sets the state of the idle warning flag.<br>
   * Note that this method will also update the
   * the timestamp if the idle warning flag is removed,
   * which means its kind of a second way to achieve the
   * same thing as with the activity method.
   *
   * @param bool true if a warning is to be issued,
   *             false if to be removed.
   * @see #activity()
   */
  public void setWarned(boolean bool) {
    m_Warned = bool;
    if (!bool) {
      m_LastActivity = System.currentTimeMillis();
    }
  }//setWarned

  /**
   * Returns the state of the idle warning flag, which
   * will be true if a warning has been issued, and false
   * if not.
   *
   * @return the state of the idle warning flag.
   */
  public boolean isWarned() {
    return m_Warned;
  }//isWarned

  /**
   * Sets the terminal geometry data.<br>
   * <em>This method should not be called explicitly
   * by the application (i.e. the its here for the io subsystem).</em><br>
   * A call will set the terminal geometry changed flag.
   *
   * @param width  of the terminal in columns.
   * @param height of the terminal in rows.
   */
  public void setTerminalGeometry(int width, int height) {
    m_TerminalGeometry[0] = width;
    m_TerminalGeometry[1] = height;
    m_TerminalGeometryChanged = true;
  }//setTerminalGeometry

  /**
   * Returns the terminal geometry in an array of two integers.
   * <ul>
   * <li>index 0: Width in columns.
   * <li>index 1: Height in rows.
   * </ul>
   * A call will reset the terminal geometry changed flag.
   *
   * @return integer array containing width and height.
   */
  public int[] getTerminalGeometry() {
    //we toggle the flag because the change should now be known
    if (m_TerminalGeometryChanged) m_TerminalGeometryChanged = false;
    return m_TerminalGeometry;
  }//getTerminalGeometry

  /**
   * Returns the width of the terminal in columns for convenience.
   *
   * @return the number of columns.
   */
  public int getTerminalColumns() {
    return m_TerminalGeometry[0];
  }//getTerminalColumns

  /**
   * Returns the height of the terminal in rows for convenience.
   *
   * @return the number of rows.
   */
  public int getTerminalRows() {
    return m_TerminalGeometry[1];
  }//getTerminalRows

  /**
   * Returns the state of the terminal geometry changed flag,
   * which will be true if it has been set, and false
   * if not.
   *
   * @return the state of the terminal geometry changed flag.
   */
  public boolean isTerminalGeometryChanged() {
    return m_TerminalGeometryChanged;
  }//isTerminalGeometryChanged

  /**
   * Sets the terminal type that has been negotiated
   * between telnet client and telnet server, in form of
   * a String.<br>
   * <p/>
   * <em>This method should not be called explicitly
   * by the application (i.e. the its here for the io subsystem).</em><br>
   *
   * @param termtype the negotiated terminal type as String.
   */
  public void setNegotiatedTerminalType(String termtype) {
    m_NegotiatedTerminalType = termtype;
  }//setNegotiatedTerminalType

  /**
   * Returns the terminal type that has been negotiated
   * between the telnet client and the telnet server, in
   * of a String.<br>
   *
   * @return the negotiated terminal type as String.
   */
  public String getNegotiatedTerminalType() {
    return m_NegotiatedTerminalType;
  }//getNegotiatedTerminalType

  /**
   * Returns the hashmap for storing and
   * retrieving environment variables to be passed
   * between shells.
   *
   * @return a <tt>HashMap</tt> instance.
   */
  public HashMap getEnvironment() {
    return m_Environment;
  }//getEnvironment

  /**
   * Sets the login shell name.
   *
   * @param s the shell name as string.
   */
  public void setLoginShell(String s) {
    m_LoginShell = s;
  }//setLoginShell

  /**
   * Returns the login shell name.
   *
   * @return the shell name as string.
   */
  public String getLoginShell() {
    return m_LoginShell;
  }//getLoginShell

  /**
   * Tests if in line mode.
   *
   * @return true if in line mode, false otherwise
   */
  public boolean isLineMode() {
    return m_LineMode;
  }//isLineMode

  /**
   * Sets the line mode flag for the connection.
   * Note that the setting will only be used at
   * startup at the moment.
   *
   * @param b true if to be initialized in linemode,
   *          false otherwise.
   */
  public void setLineMode(boolean b) {
    m_LineMode = b;
  }//setLineMode

  /**
   * Mutator for HostName cache
   */
  private void setHostName() {
    m_HostName = m_IP.getHostName();
  }//setHostName

  /**
   * Mutator for HostAddress cache
   */
  private void setHostAddress() {
    m_HostAddress = m_IP.getHostAddress();
  }//setHostAddress

  /**
   * Mutator for Locale
   * Sets a Locale derived from the hostname,
   * or the default which is Locale.ENGLISH if something
   * goes wrong.
   * The localhost represents a problem for example :)
   */
  private void setLocale() {
    String country = getHostName();
    try {
      country = country.substring(country.lastIndexOf(".") + 1);
      if (country.equals("at")) {
        m_Locale = new Locale("de", "AT");
        return;
      } else if (country.equals("de")) {
        m_Locale = new Locale("de", "DE");
        return;
      } else if (country.equals("mx")) {
        m_Locale = new Locale("es", "MX");
        return;
      } else if (country.equals("es")) {
        m_Locale = new Locale("es", "ES");
        return;
      } else if (country.equals("it")) {
        m_Locale = Locale.ITALY;
        return;
      } else if (country.equals("fr")) {
        m_Locale = Locale.FRANCE;
        return;
      } else if (country.equals("uk")) {
        m_Locale = new Locale("en", "GB");
        return;
      } else if (country.equals("arpa")) {
        m_Locale = Locale.US;
        return;
      } else if (country.equals("com")) {
        m_Locale = Locale.US;
        return;
      } else if (country.equals("edu")) {
        m_Locale = Locale.US;
        return;
      } else if (country.equals("gov")) {
        m_Locale = Locale.US;
        return;
      } else if (country.equals("org")) {
        m_Locale = Locale.US;
        return;
      } else if (country.equals("mil")) {
        m_Locale = Locale.US;
        return;
      } else {
        //default to english
        m_Locale = Locale.ENGLISH;
      }
    } catch (Exception ex) {
      //default to english
      m_Locale = Locale.ENGLISH;
    }
  }//setLocale

}//class ConnectionData
