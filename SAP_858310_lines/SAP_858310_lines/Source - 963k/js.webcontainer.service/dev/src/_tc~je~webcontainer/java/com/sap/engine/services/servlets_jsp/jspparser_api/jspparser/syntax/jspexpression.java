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

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspExpression extends JspScriptElement {

  private String script = null;
  /*
   * denotes start of this type element
   */
  public static final char[] START_TAG_INDENT = new char[] {'<', '%', '='};

  public JspExpression() {
    super();
    _name = "expression";
  }
  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   */
  public void action(StringBuffer buffer) {
    debugInfo.writeStart(buffer, false);
    buffer.append("\t\t\tout.print(");
    script = parser.getCharsTrim(startIndex + 3, endIndex - 2);
    script = parser.replaceScriptQuotes(script);
    buffer.append(script);
    buffer.append(");\r\n");
    debugInfo.writeEnd(buffer);
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars <,%,=
   * in that order
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes != null) {
      throw new JspParseException(JspParseException.ELEMENT_DOES_NOT_TAKE_ANY_ATTRIBUTES, new Object[]{_name},
              parser.currentFileName(), debugInfo.start);
    }
  }

  public String getString(IDCounter id) {
    return _default_prefix_tag_start + _name + getId(id) + CDATA_START + script + CDATA_END + _default_prefix_tag_end + _name + END;
  }

}

