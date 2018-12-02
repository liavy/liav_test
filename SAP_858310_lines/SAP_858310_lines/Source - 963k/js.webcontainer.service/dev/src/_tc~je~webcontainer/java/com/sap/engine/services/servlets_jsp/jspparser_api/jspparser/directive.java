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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

import java.util.*;

/**
 * Parses some String and determines the name of this directive
 * and generates a Hashtable containing the all name - value couples detected into this String.
 *
 */
public class Directive {

  /**
   * Directive name
   */
  private String directiveName;
  /**
   * Hastable containing Directive elements
   */
  private Hashtable elements;
  /**
   * If the current value starts with double quotes or a single quote
   */
  private boolean dQ = true;

  /**
   * Constructs a new Directive. Parses the String given as parameter and determine name of this directive
   * and a Hashtable containing the all namme - value couples detected into this String.
   *
   * @param   directiveSrc  source string
   */
  public Directive(String directiveSrc) throws JspParseException {
    directiveSrc.concat(" ");
    String value = null;
    String key = null;
    elements = new Hashtable();
    int srcPos = 0;

    //skip spaces
    while (Character.isWhitespace(directiveSrc.charAt(srcPos))) {
      if (srcPos >= directiveSrc.length() - 1) {
        return;
      }

      srcPos++;
    }

    //skip tag
    char[] tmpt = new char[directiveSrc.length()];
    int m = 0;

    while (!Character.isWhitespace(directiveSrc.charAt(srcPos))) {
      tmpt[m] = directiveSrc.charAt(srcPos);
      m++;
      srcPos++;
    }

    directiveName = String.valueOf(tmpt, 0, m);

    //skip spaces
    while (Character.isWhitespace(directiveSrc.charAt(srcPos))) {
      if (srcPos >= directiveSrc.length() - 1) {
        return;
      }

      srcPos++;
    }

    while (srcPos < directiveSrc.length()) {
      if (directiveSrc.charAt(srcPos) == '/') {
        return;
      }

      char[] tmp = new char[directiveSrc.length()];
      int i = 0;

      while (isKey(directiveSrc.charAt(srcPos))) {
        tmp[i] = directiveSrc.charAt(srcPos);
        i++;
        srcPos++;
      }

      key = String.valueOf(tmp, 0, i);

      //skip spaces
      while (Character.isWhitespace(directiveSrc.charAt(srcPos))) {
        srcPos++;
      }

      if (!(directiveSrc.charAt(srcPos) == '=')) {
        throw new JspParseException(JspParseException.CHAR_EXCPECTED, new Object[]{"="});
      }

      srcPos++;

      //skip spaces
      while (Character.isWhitespace(directiveSrc.charAt(srcPos))) {
        srcPos++;
      }

      if (directiveSrc.charAt(srcPos) == '"') {
        dQ = true;
      } else if (directiveSrc.charAt(srcPos) == '\'') {
        dQ = false;
      } else {
        throw new JspParseException(JspParseException.CHAR_EXCPECTED, new Object[]{"\""});
      }

      srcPos++;
      char[] tmp2 = new char[directiveSrc.length()];
      int j = 0;

      if (dQ) {
        while ((directiveSrc.charAt(srcPos) != '"') || ((directiveSrc.charAt(srcPos) == '"') && (directiveSrc.charAt(srcPos - 1) == '\\'))) {
          if ((directiveSrc.charAt(srcPos) == '"') && (directiveSrc.charAt(srcPos - 1) == '\\')) {
            tmp2[j - 1] = directiveSrc.charAt(srcPos);
            srcPos++;
          } else {
            tmp2[j] = directiveSrc.charAt(srcPos);
            j++;
            srcPos++;
          }
        }
      } else {
        while (directiveSrc.charAt(srcPos) != '\'') {
          if ((directiveSrc.charAt(srcPos) == '"') && (directiveSrc.charAt(srcPos - 1) == '\\')) {
            tmp2[j - 1] = directiveSrc.charAt(srcPos);
            srcPos++;
          } else {
            tmp2[j] = directiveSrc.charAt(srcPos);
            j++;
            srcPos++;
          }
        }
      }

      srcPos++;
      value = String.valueOf(tmp2, 0, j);
      //      if (value.trim().startsWith("<%="))
      elements.put(key, value);

      if (srcPos >= directiveSrc.length() - 1) {
        return;
      }

      //skip spaces
      while (Character.isWhitespace(directiveSrc.charAt(srcPos))) {
        srcPos++;

        if (srcPos >= directiveSrc.length() - 1) {
          return;
        }
      }

      if (directiveSrc.charAt(srcPos) == '/') {
        return;
      }
    }
  }

  /**
   * Returns the name of the directive.
   *
   * @return     directice name
   */
  public String getName() {
    return directiveName.trim();
  }

  /**
   * Returns the elements of the directive.
   *
   * @return     elements for the directive
   */
  public Hashtable getElements() {
    return elements;
  }

  private boolean isKey(char s) {
    return (s != '"') && (s != '\'') && (s != ' ') && (s != '\n') && (s != '\t') && (s != '=');
  }

}

