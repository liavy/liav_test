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
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.CustomJspTag;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class OutputCommentElement extends CustomJspTag {

  public static final char[] START_TAG_INDENT = new char[] {'<', '!', '-', '-'};
  public static final char[] END_TAG_INDENT = new char[] {'-', '-', '>'};
  private String body = "";


  /**
   * Constructs new OutputCommentElement
   *
   */
  public OutputCommentElement() {
    elementType = Element.OUTPUT_COMMENT;
    tagType = JspTag.SINGLE_TAG;
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer writer) throws JspParseException {
    if (elements != null) {
      // ZARADI tapia CTS !!!
      if (parser != null && parser.isXml()) {
        return;
      }

      for (int i = 0; i < elements.length; i++) {
        if (writer == null) {
          elements[i].action();
        } else {
          elements[i].action(writer);
        }
      }
    }
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars <,!,-,-
   * in that order
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  /**
    * Returnes element to witch will be associated
    * current parsing.
    *
    * @return object that is associated as current element
    * of parsing
    */
   protected CustomJspTag createThis() {
     return (this.copy) ? new OutputCommentElement() : this;
   }

   /**
    * Returnes element to witch will be associated
    * current parsing.
    *
    * @return object that is associated as current element
    * of parsing
    */
   protected JspElement createJspElement() {
     return new JspElement(JspElement.OUTPUT_COMMENT);
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
     return (JspTag) (new OutputCommentElement().parse(this.parser));
   }

  /**
   * Verifies the attributes of this tag
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

  /**
   * Performs action that coresponds to template jsp data
   *
   * @param   start  start index of the tamplate data
   * @param   end  end index of the tamplate data
   */
  private void templateAction(int start, int end) {
    templateAction(parser.getChars(start, end));
  }

  /**
   * Performs action that coresponds to template jsp data
   *
   * @param   s  tamplate data
   */
  private void templateAction(String s) {
    parser.getScriptletsCode().append("\t\t\tout.print(\"").append(StringUtils.escapeJavaCharacters(s)).append("\");").append("\r\n");
  }

  public String getString(IDCounter id) {
    return body;
  }

}

