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

import java.io.Serializable;

/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class Position implements Serializable {

  /*
   * line in the jsp source
   */
  int line;
  /*
   * position in the line in the jsp source
   */
  int linePos;

  /**
   * Constructs new debug position object
   *
   * @param   line line in the jsp source
   * @param   linePos position in the line in the jsp source
   */
  public Position(int line, int linePos) {
    this.line = line;
    this.linePos = linePos;
  }

  public String toString() {
    return "Position(" + line + ", " + linePos + ")";
  }

  public int getLine() {
    return line;
  }

  public int getLinePos() {
    return linePos;
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    Position p = (Position) obj;
	  return (p.getLine() == line && p.getLinePos() == linePos);
  }

  public int hashCode() {
    return (line << 13) + (linePos & 0xff);
  }
}

