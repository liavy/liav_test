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

import java.util.Arrays;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class Indentifier extends Element {

  /*
   * denotes that indentifier is quoted
   */
  public static final int QUOTED = 1;
  /*
   * denotes that indentifier is not quoted
   */
  public static final int NOT_QUOTED = 0;
  /*
   * type for this indentifier
   */
  public int indentifierType;
  public String value = null;

  /**
   * Constructs new Indentifier
   *
   */
  public Indentifier() {
    elementType = Element.INDENTIFIER;
  }

  /**
   * Constructs new Indentifier (used from XMLParser)
   *
   */
  public Indentifier(String value) {
    elementType = Element.INDENTIFIER;
    indentifierType = NOT_QUOTED;
    this.value = value;
  }

  /**
   * No action is permited.This method always throws
   * RuntimeException "Not Implemented."
   */
  public void action() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  public void action(StringBuffer writer) throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  /**
   * No prefix is defined for this element.
   * This method always throws RuntimeException
   * "Not Implemented."
   *
   * @return nothing
   */
  public char[] indent() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  /**
   * Parses jap tag attribute name or value and returns
   * newly constructed element
   *
   * @param   parser  parser to use
   * @return  parsed indentifier
   * @exception   JspParseException  thrown if error occures
   * during parsing or verification
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    this.parser = parser;
    char quote = parser.currentChar();
    Position p = parser.currentDebugPos();

    if (quote == '\"' || quote == '\'') {
      indentifierType = QUOTED;
      parser.nextChar();
      startIndex = parser.currentPos();
      char currentChar;

      while (true) {
        currentChar = parser.currentChar();

        if (currentChar == quote) {
          int pos = parser.currentPos();

          if (parser.charAt(pos - 1) == '\\') {
            parser.nextChar();

            if (parser.endReached()) {
              throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{quote + ""},
                      parser.currentFileName(), p);
            }

            continue;
          } else {
            break;
          }
        } else {
          parser.nextChar();

          if (parser.endReached()) {
            throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE, new Object[]{quote + ""},
                    parser.currentFileName(), p);
          }
        }
      }

      endIndex = parser.currentPos();
      parser.nextChar();
    } else {
      if (quote == '&') {
        p = parser.currentDebugPos();
        char[] quoteArray = new char[5];

        for (int i = 0; i < quoteArray.length; i++) {
          parser.nextChar();

          if (parser.endReached()) {
            throw new JspParseException(JspParseException.EXPECTING_CHAR_OR_CHAR_WHILE_READ_END_OF_FILE, new Object[]{"&quote;", "&apos;"},
                    parser.currentFileName(), p);
          }

          quoteArray[i] = parser.currentChar();
        }

        indentifierType = QUOTED;

        if (Arrays.equals(quoteArray, "quot;".toCharArray()) || Arrays.equals(quoteArray, "apos;".toCharArray())) {
          parser.nextChar();
          startIndex = parser.currentPos();

          while (true) {
            if (parser.currentChar() == quote) {
              parser.nextChar();

              if (parser.compare(parser.currentPos(), parser.currentPos() + 5, quoteArray)) {
                endIndex = parser.currentPos() - 1;
                parser.nextChar();
                parser.nextChar();
                parser.nextChar();
                parser.nextChar();
                parser.nextChar();
                break;
              } else {
                continue;
              }
            } else {
              parser.nextChar();

              if (parser.endReached()) {
                throw new JspParseException(JspParseException.EXPECTING_CHAR_WHILE_READ_END_OF_FILE , new Object[]{"'&amp;" + String.copyValueOf(quoteArray) + "'"},
                        parser.currentFileName(), p);
              }
            }
          }
        } else {
          throw new JspParseException(JspParseException.EXPECTING_CHAR_OR_CHAR, new Object[]{"&quote;", "&apos;"},
                  parser.currentFileName(), p);
        }
      } else {
        indentifierType = NOT_QUOTED;
        startIndex = parser.currentPos();
        parser.skipIndentifier();
        endIndex = parser.currentPos();
      }
    }

    return this;
  }

  /**
   * Compares this indentifier to the specified name
   *
   * @param   name  string to compare with
   * @return  true if name is equal to this indentifier
   */
  public boolean equals(String name) throws JspParseException {
    if (value != null) {
      return value.equals(name);
    }
    if (parser == null) {
      throw new JspParseException(JspParseException.CANNOT_OBTAIN_PARSER);
    }

    return parser.compare(startIndex, endIndex, name.toCharArray());
  }

  /**
   * Compares this indentifier to the specified name
   *
   * @param   name  string chars to compare with
   * @return  true if name is equal to this indentifier
   */
  public boolean equals(char[] name) throws JspParseException {
    if (value != null) {
      return value.equals(new String(name));
    }
    if (parser == null) {
      throw new JspParseException(JspParseException.CANNOT_OBTAIN_PARSER);
    }

    return parser.compare(startIndex, endIndex, name);
  }

  public boolean equalsIgnoreCase(String name) throws JspParseException {
    if (value != null) {
      return value.equalsIgnoreCase(name);
    }
    if (parser == null) {
      throw new JspParseException(JspParseException.CANNOT_OBTAIN_PARSER);
    }

    return parser.compareIgnoreCase(startIndex, endIndex, name.toCharArray());
  }

  public boolean equalsIgnoreCase(char[] name) throws JspParseException {
    if (value != null) {
      return value.equalsIgnoreCase(new String(name));
    }
    if (parser == null) {
      throw new JspParseException(JspParseException.CANNOT_OBTAIN_PARSER);
    }

    return parser.compareIgnoreCase(startIndex, endIndex, name);
  }
  /**
   * Constructs string representation of this indentifier
   *
   * @return  constructed string with all escaped tokens
   * replaced with normal tokens
   */
  public String toString() {
    if (value != null) {
      return value;
    }

    if (indentifierType != QUOTED) {
      return parser.getChars(startIndex, endIndex);
    } else {
      return parser.replaceAttributeQuotes(parser.getChars(startIndex, endIndex));
    }
  }

  public String toStringUnquoted() {
    if (value != null) {
      return value;
    }
    return parser.getChars(startIndex, endIndex);
  }

  public String getString(IDCounter id) {
    return null;
  }

}

