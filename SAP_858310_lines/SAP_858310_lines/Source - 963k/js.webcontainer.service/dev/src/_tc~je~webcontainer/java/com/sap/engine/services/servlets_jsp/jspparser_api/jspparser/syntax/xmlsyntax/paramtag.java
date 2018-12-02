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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax;

import java.util.Arrays;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Indentifier;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class ParamTag extends CustomJspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final char[][] attributeNames = {"name".toCharArray(), "value".toCharArray(), };

  /**
   * Creates new ParamTag
   *
   */
  public ParamTag() {
    super();
    _name = "param";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  //  /**
  //   *
  //   *
  //   * @param   startTagIndent
  //   * @param   endTagIndent
  //   */
  //  public ParamTag(char[] startTagIndent ,char[] endTagIndent) {
  //    this();
  //    START_TAG_INDENT = startTagIndent;
  //    CMP_0 = endTagIndent;
  //  }
  /**
   * No action is taken
   *
   */
  public void action(StringBuffer buffer) throws JspParseException {
    throw new JspParseException(JspParseException.ELEMENT_MUST_BE_USED_ONLY_IN, new String[]{"jsp:param", "jsp:include, jsp:forward and jsp:params"});
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars
   * "<jsp:param"
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
    return (this.copy) ? new ParamTag() : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.PARAM);
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
    return (JspTag) (new ParamTag().parse(this.parser));
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length != 2) {
      throw new JspParseException(JspParseException.ELEMENT_MUST_HAVE_EXACTLY_TWO_SPECIFIED_ATTRIBUTE, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }

    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);
    Indentifier e = null;

    for (int i = 0; i < attributes.length; i++) {
      e = attributes[i].name;

      if (e.equals(attributeNames[0])) {
        if (flags[0]) {
          flags[0] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ELEMENT, new Object[]{"name", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.equals(attributeNames[1])) {
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ELEMENT, new Object[]{"value", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ELEMENT, new Object[]{e, _name}, parser.currentFileName(), debugInfo.start);
      }
    } 
  }

}

