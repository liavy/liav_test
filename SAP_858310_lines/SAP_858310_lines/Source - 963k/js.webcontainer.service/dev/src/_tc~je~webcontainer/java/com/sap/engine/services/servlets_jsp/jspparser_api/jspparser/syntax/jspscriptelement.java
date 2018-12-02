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

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspScriptElement extends JspTag {

  /*
   * denotes the end token of this type element
   */
  public static final char[] END_TAG_INDENT = new char[] {'%', '>'};

  /**
   * Constructs new JspScriptElement
   *
   */
  public JspScriptElement() {
    elementType = Element.SCRIPT;
    tagType = JspTag.SINGLE_TAG;
  }

  /**
   * No action is permited.This method always throws
   * RuntimeException "Not Implemented."
   *
   * @exception   JspParseException
   */
  public void action(StringBuffer buffer) throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * This element has no indent .This method always throws
   * RuntimeException "Not Implemented."
   *
   * @return  nothing
   */
  public char[] indent() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  /**
   * Parses next token to the end indent of this element
   *
   * @param   parser  parser to use
   * @return   newly created element
   * @exception   JspParseException  thrown if error occures
   * during parsing or verification
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    this.parser = parser;
    if (parser.getJspProperty() != null && parser.getJspProperty().isScriptingInvalid().equalsIgnoreCase("true")) {
      throw new  JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE,parser.currentFileName(), parser.currentDebugPos());
    }
    startIndex = parser.currentPos();
    Position startDebugPos = parser.currentDebugPos();
    parser.nextChar();
    parser.nextChar();
    parser.nextChar(); // "skipping <%!"
    parser.parseTo("%>".toCharArray(), false);

    if (parser.currentChar() != '%') {
      throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN, parser.currentFileName(), parser.currentDebugPos());
    } else {
      if (parser.nextChar() != '>') {
        throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_PARSING_ELEMENT_IS_NOT_VALID_FOR_TOKEN, parser.currentFileName(), parser.currentDebugPos());
      } else {
        parser.nextChar();
      }
    }

    Position endDebugPos = parser.currentDebugPos();
    debugInfo = new DebugInfo(startDebugPos, endDebugPos, parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
    endIndex = parser.currentPos();
    return this;
  }

  /**
   * No verification is permited.This method always throws
   * RuntimeException "Not Implemented."
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

}

