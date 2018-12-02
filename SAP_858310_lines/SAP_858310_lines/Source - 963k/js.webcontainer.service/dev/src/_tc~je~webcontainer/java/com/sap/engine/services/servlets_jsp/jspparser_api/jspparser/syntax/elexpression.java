/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax;

/**
 * This class implements a parser for EL expressions
 */
public class ELExpression {
  private static final char ESC = '\\';
  private static final char DOLAR = '$';
  private static final char NUMBER = '#';
  private static final char BRACE_LEFT = '{';
  private static final char BRACE_RIGHT = '}';
  private char[] expression = null;

  /**
   * TRUE if this is deferred EL - #{..}, FALSE if it is not an EL expression or it is ${...}
   */
  boolean isDeffered = false;
  /**
   * TRUE if it is an EL expression , regardless   #{..} or ${...}
   */
  boolean isEL = false;
  /**
   *  True if it is an escaped EL -  \#{ or \${
   */
  boolean isEscaped = false;

  public ELExpression(String expression) {
    if (expression != null) {
      this.expression = expression.trim().toCharArray();
      parse();
    }
  }

  public void parse() {
    if (expression == null) {
      return;
    }
    char prev = 0;
    for (int i = 0; i < expression.length; i++) {
      if (prev == ESC) {
        prev = 0;
        if (expression[i] == ESC) {
          prev = ESC;
        } else if (expression[i] == DOLAR || expression[i] == NUMBER){
          isEscaped = true;
        }
      } else if (prev == DOLAR || prev == NUMBER) {
        if (expression[i] == BRACE_LEFT) {
          this.isEL = true;
          this.isDeffered = (prev == NUMBER);
          break;
        } else {
          prev = expression[i];
        }
      } else {
        prev = expression[i];
      }
    }
//    if (prev != BRACE_RIGHT) {
//      this.isEL = false;
//    }
  }

  /**
   * TRUE if this is deferred EL - #{..}, FALSE if it is ${...}  or String literal
   */
  public boolean isDeffered() {
    return isDeffered;
  }

  /**
   * TRUE if it is an EL expression , regardless   #{..} or ${...}
   * FALSE if it is not recognized as EL
   */
  public boolean isEL() {
    return isEL;
  }

  /**
   *  True if it is an escaped EL -  \#{ or \${
   */
  public boolean isEscaped() {
    return isEscaped;
  }

  /**
   * Used in Template text to check if it contains deferred expressions #{...}
   * @return true im defrered EL found , false otherwise
   */
  public static boolean checkForDeffered(String expr) {
    if (expr == null) {
      return false;
    }
    char[] expression = expr.trim().toCharArray();

    boolean numberFound = false;
    for (int i = 0; i < expression.length; i++) {
      if (!(expression[i] == NUMBER || expression[i] == BRACE_LEFT || expression[i] == BRACE_RIGHT)) {
        continue;
      }
      if (expression[i] == NUMBER && (i > 0 ? expression[i - 1] != ESC : true) && i < (expression.length - 1) && expression[i+1] == BRACE_LEFT){
        numberFound = true;
        continue;
      }
      if (numberFound && expression[i] == BRACE_RIGHT) {
        return true;
      }
    }
    return false;
  }
}
