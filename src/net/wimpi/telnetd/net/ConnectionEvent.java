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

/**
 * Class implmenting a ConnectionEvent.<br>
 * These events are used to communicate things that are
 * supposed to be handled within the application context.
 * These events are processed by the Connection instance
 * calling upon its registered listeners.
 *
 * @author Dieter Wimberger
 * @version 2.0 (16/07/2006)
 * @see net.wimpi.telnetd.net.Connection
 * @see net.wimpi.telnetd.net.ConnectionListener
 */
public class ConnectionEvent {

  private int m_Type;
  private Connection m_Source;

  /**
   * Constructs a new instance of a ConnectionEvent
   * with a given source (Connection) and a given type.
   *
   * @param source Connection that represents the source of this event.
   * @param typeid int that contains one of the defined event types.
   */
  public ConnectionEvent(Connection source, int typeid) {
    m_Type = typeid;
    m_Source = source;
  }//constructor

  /**
   * Accessor method returning the source of the
   * ConnectionEvent instance.
   *
   * @return Connection representing the source.
   */
  public Connection getSource() {
    return m_Source;
  }//getSource

  /**
   * @see #getSource()
   * @deprecated for better naming, replaced by getSource
   */
  public Connection getConnection() {
    return m_Source;
  }//getConnection

  /**
   * Method that helps identifying the type.
   *
   * @param typeid int that contains one of the defined event types.
   */
  public boolean isType(int typeid) {
    return (m_Type == typeid);
  }//isType


//Constants

  /**
   * Defines the connection idle event type.<br>
   * It occurs if a connection has been idle exceeding
   * the configured time to warning.
   */
  public static final int CONNECTION_IDLE = 100;

  /**
   * Defines the connection timed out event type.<br>
   * It occurs if a connection has been idle exceeding
   * the configured time to warning and the configured time
   * to timedout.
   */
  public static final int CONNECTION_TIMEDOUT = 101;

  /**
   * Defines the connection requested logout event type.<br>
   * It occurs if a connection requested disgraceful logout by
   * sending a <Ctrl>-<D> key combination.
   */
  public static final int CONNECTION_LOGOUTREQUEST = 102;

  /**
   * Defines the connection broken event type.<br>
   * It occurs if a connection has to be closed by
   * the system due to communication problems (i.e. I/O errors).
   */
  public static final int CONNECTION_BROKEN = 103;

  /**
   * Defines the connection sent break event type.<br>
   * It occurs when the connection sent a NVT BREAK.
   */
  public static final int CONNECTION_BREAK = 104;

}//class ConnectionEvent