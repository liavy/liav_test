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
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 *
 * @author Bojidar Kadrev
 * @version 7.0
 */
public class AttributeTag extends CustomJspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"name", "trim" };

  /**
   * Creates new ExpressionTag
   *
   */
  public AttributeTag() {
    _name = "attribute";
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
    switch (tagType) {
      case SINGLE_TAG: {
        //no action
        break;
      }
      case START_TAG:
      case END_TAG:
      default:
    }
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }


  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars
   * "<jsp:attribute"
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
    return (this.copy) ? new AttributeTag() : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.ATTRIBUTE);
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
    return (JspTag) (new AttributeTag().parse(this.parser));
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length == 0) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_ACTION, new Object[]{"name", _name}, parser.currentFileName(), debugInfo.start);
    }

    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);
    String name = null;
    String value = null;

    for (int i = 0; i < attributes.length; i++) {
      name = attributes[i].name.toString();
      value = attributes[i].value.toString();

      if (attributeNames[0].equals(name)) {
        if (flags[0]) {
          flags[0] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"name", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[1].equals(name)) {
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"trim", _name}, parser.currentFileName(), debugInfo.start);
        }

        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value, "trim", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ACTION, new Object[]{name, _name}, parser.currentFileName(), debugInfo.start);
      }
    }

    if (flags[0]) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_ACTION, new Object[]{"name", _name}, parser.currentFileName(), debugInfo.start);
    }
  }

}

