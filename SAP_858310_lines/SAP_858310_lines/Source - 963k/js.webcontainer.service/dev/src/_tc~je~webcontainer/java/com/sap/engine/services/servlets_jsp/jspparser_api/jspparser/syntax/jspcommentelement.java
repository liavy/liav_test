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
 * @author Ivo Simeonov
 * @version 4.0
 */
public class JspCommentElement extends JspTag {

  /*
   * denotes start of this type element
   */
  public static final char[] START_TAG_INDENT = new char[] {'<', '%', '-', '-'};
  /*
   * denotes end of this type element
   */
  public static final char[] END_TAG_INDENT = new char[] {'-', '-', '%', '>'};

  /**
   * Constructs new JspCommentElement
   *
   */
  public JspCommentElement() {
    elementType = Element.JSP_COMMENT;
    tagType = JspTag.SINGLE_TAG;
  }

  /**
   * No action is taken.
   */
  public void action(StringBuffer buffer) {

  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  always array containing chars <,%,-,-
   * in that order
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
   * Parses all tokens to the end prefix of this element
   *
   * @param   parser  parser to use
   * @return  newly created element
   * @exception   JspParseException  thrown if error occures
   * during parsing
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    this.parser = parser;
    this.startIndex = parser.currentPos();
    parser.parseTo(END_TAG_INDENT, true);
    this.endIndex = parser.currentPos();
    return this;
  }

  /**
   * Verifies this tag element attributes.
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes != null) {
      throw new JspParseException(JspParseException.ELEMENT_DOES_NOT_TAKE_ANY_ATTRIBUTES, new Object[]{"comment"},
              parser.currentFileName(), debugInfo.start);
    }
  }

  public String getString(IDCounter id) {
    return "";
  }

}

