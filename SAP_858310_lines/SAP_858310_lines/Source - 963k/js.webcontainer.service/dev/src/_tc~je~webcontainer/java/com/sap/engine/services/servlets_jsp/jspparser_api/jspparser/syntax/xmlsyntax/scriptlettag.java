/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.DebugInfo;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.TemplateData;

/**
 * @author TODOR-M
 * DEV_tc_je_webcontainer
 * 2005-4-13
 * 
 */
public class ScriptletTag extends CustomJspTag{

  private String script = "";

  public ScriptletTag() {
    super();
    _name = "scriptlet";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    // do nothing
    // this method is invoked only for JSP documents and the content of the tag is in the body of the JSP element (not the start/end tag). 
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars <,%
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
    if (attributes != null && attributes.length > 0) {
      throw new JspParseException(JspParseException.ELEMENT_DOES_NOT_TAKE_ANY_ATTRIBUTES, new Object[]{"scriptlet"}, parser.currentFileName(), debugInfo.start);
    }
  }
  
  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   * @exception   JspParseException  thrown if error occures
   * during verification or parsing
   */
  protected JspTag createEndTag() throws JspParseException {
    return (JspTag) (new ScriptletTag().parse(this.parser));
  }
  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.SCRIPLET);
  }
  
  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected CustomJspTag createThis() {
    return (this.copy) ? new ScriptletTag() : this;
  }
}
