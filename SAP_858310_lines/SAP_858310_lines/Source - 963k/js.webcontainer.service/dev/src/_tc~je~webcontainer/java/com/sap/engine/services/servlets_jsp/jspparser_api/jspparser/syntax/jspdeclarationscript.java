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
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;


/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class JspDeclarationScript extends JspScriptElement {

  private String script = null;
  /*
   * denotes start of this type element
   */
  public static final char[] START_TAG_INDENT = new char[] {'<', '%', '!'};

  public JspDeclarationScript() {
    super();
    _name = "declaration";
  }
  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    debugInfo.writeStart(parser.getDeclarationsCode(), false);
    script = parser.getChars(startIndex + 3, endIndex - 2);
    script = parser.replaceScriptQuotes(script);
    if( parser.getWebContainerParameters().isJspDebugSupport() ){
      script = StringUtils.escapeEndComment(script);
    }
    parser.setDeclarationsCode(parser.getDeclarationsCode().append(script));
    debugInfo.writeEnd(parser.getDeclarationsCode());
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars <,%,!
   * in that order
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
   * Verifies this tag element attributes.
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

