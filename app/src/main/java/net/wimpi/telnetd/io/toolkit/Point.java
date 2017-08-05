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
package net.wimpi.telnetd.io.toolkit;/** * Class that represents a point on the terminal. * Respectively it specifies a character cell, encapsulating * column and row coordinates. * * @author Dieter Wimberger * @version 2.0 (16/07/2006) */public class Point {  //Members  private int m_Row;  private int m_Col;  /**   * Constructs an instance with its coordinates set to the origin (0/0).   */  public Point() {    m_Col = 0;    m_Row = 0;  }//constructor  /**   * Constructs an instance with given coordinates.   *   * @param col Integer that represents a column position.   * @param row Integer that represents a row position   */  public Point(int col, int row) {    m_Col = col;    m_Row = row;  }//constructor  /**   * Mutator method to set the points coordinate at once.   *   * @param col Integer that represents a column position.   * @param row Integer that represents a row position   */  public void setLocation(int col, int row) {    m_Col = col;    m_Row = row;  }//setLocation  /**   * Convenience method to set the points coordinates.   *   * @param col Integer that represents a column position.   * @param row Integer that represents a row position   */  public void move(int col, int row) {    m_Col = col;    m_Row = row;  }//move  /**   * Accessor method for the column coordinate.   *   * @return int that represents the cells column coordinate.   */  public int getColumn() {    return m_Col;  }//getColumn  /**   * Mutator method for the column coordinate of this   * Cell.   *   * @param col Integer that represents a column position.   */  public void setColumn(int col) {    m_Col = col;  }//setColumn  /**   * Accessor method for the row coordinate.   *   * @return int that represents the cells row coordinate.   */  public int getRow() {    return m_Row;  }//getRow  /**   * Mutator method for the row coordinate of this   * Cell.   *   * @param row Integer that represents a row position.   */  public void setRow(int row) {    m_Row = row;  }//setRow}//class Point