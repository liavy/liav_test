/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;


/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class DebugInfo {

  /*
   * parts from the debug string
   */
  public static final char[] END_TAG = "// end\r\n".toCharArray();
  public static final char[] HTML = "// HTML ".toCharArray();
  public static final char[] BEGIN = "// begin [file=\"".toCharArray();
  public static final char[] FROM = "\";from=".toCharArray();
  public static final char[] TO = ";to=".toCharArray();
  public static final char[] END_ = "]\r\n".toCharArray();
  /*
   * Denotes the start position this info refers to
   */
  public Position start;
  /*
   * Denotes the end position this info refers to
   */
  public Position end;
  /*
   * Denotes the file name this info refers to
   */
  public String fileName;
  
  /**
   * The value of the web container's property - JSPDebugSupport.  
   */
  private boolean jspDebugSuppport = true;

  /**
   * Constructs new debug info
   *
   * @param   start start position of this info
   * @param   end  end position of this info
   * @param   fileName file name of this info
   */
  public DebugInfo(Position start, Position end, String fileName, boolean jspDebugSuppport) {
    this.start = start;
    this.end = end;
    this.fileName = fileName;
    this.jspDebugSuppport = jspDebugSuppport;
  }

  /**
   * Creates and writes specific java comment info in the specified
   * string buffer
   *
   * @param   sb  String buffer to write in
   * @param   html whether this info refers to html or not
   */
  public void writeStart(StringBuffer sb, boolean html) {
    if( !jspDebugSuppport ){
      return;
    }
    if (html) {
      sb.append(HTML);
    }

    sb.append(BEGIN);
    sb.append(fileName);
    sb.append(FROM);
    sb.append(parseStartPosition());
    sb.append(TO);
    sb.append(parseEndPosition());
    sb.append(END_);
  }

  /**
   * Writes specific java comment info in the specified
   * string buffer
   *
   * @param   sb  String buffer to write in
   */
  public void writeEnd(StringBuffer sb) {
    if( jspDebugSuppport ){
      sb.append(END_TAG);
    }
  }

  /**
   * Creates debug representation of the start position
   *
   * @return  position in the format
   *          (<line>,<position in line>)
   */
  private String parseStartPosition() {
    StringBuffer sb = new StringBuffer();
    sb.append("(").append(start.line).append(",").append(start.linePos).append(")");
    String ret = sb.toString();
    return ret;
  }

  /**
   * Creates debug representation of the end position
   *
   * @return  position in the format
   *          (<line>,<position in line>)
   */
  private String parseEndPosition() {
    StringBuffer sb = new StringBuffer();
    sb.append("(").append(end.line).append(",").append(end.linePos).append(")");
    String ret = sb.toString();
    return ret;
  }

  public boolean isJspDebugSuppport() {
    return jspDebugSuppport;
  }

}

