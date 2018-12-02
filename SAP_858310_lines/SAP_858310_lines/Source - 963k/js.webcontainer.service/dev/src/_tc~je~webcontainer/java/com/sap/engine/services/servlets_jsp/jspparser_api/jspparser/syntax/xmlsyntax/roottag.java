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

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.IDCounter;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.SAXTreeBuilder;

/*
 * Represents root element at XML view of a jsp page.
 *
 * @author Boby Kadrev
 * @version 4.0
 */

public class RootTag extends CustomJspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"xmlns:jsp", "version", };
  /*
   * all reserved values for attribute 'prefix'
   * accept
   */
  private static final String[] reservedPrefixes = {"xmlns:jsp", "xmlns:jspx", "xmlns:java", "xmlns:javax", "xmlns:servlet", "xmlns:sun", "xmlns:sunw"};


  /**
   * Creates new RootTag
   */
  public RootTag() {
    super();
    _name = "root";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @throws JspParseException thrown if error occures during
   *                           verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    //TO DO add debug info
    switch (tagType) {
      case SINGLE_TAG:
        {
          debugInfo.writeStart(buffer, false);
          debugInfo.writeEnd(buffer);
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
   * @return array containing chars
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }

  public Element parse(JspPageInterface parser) throws JspParseException {
    throw new JspParseException(JspParseException.CANNOT_BE_USED_IN_JSP_SYNTAX, new Object[]{_name}, parser.currentFileName(), parser.currentDebugPos());
  }
  /**
   * Verifies the attributes of this tag
   *
   * @throws JspParseException thrown if error occures
   *                           during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (!parser.isXml()) {
      throw new JspParseException(JspParseException.CANNOT_BE_USED_IN_JSP_SYNTAX, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }
    if (attributes == null || attributes.length < 1) {
      throw new JspParseException(JspParseException.ACTION_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }

    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);
    String e = null;
    String value = null;

    for (int i = 0; i < attributes.length; i++) {
      e = attributes[i].name.toString();
      value = attributes[i].value.toString();

      if (e.equals(attributeNames[1])) {
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"version", _name}, parser.currentFileName(), debugInfo.start);
        }
        if (!("1.2".equals(value) || "2.0".equals(value) || "2.1".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value, "version", _name}, parser.currentFileName(), debugInfo.start);
        }
      } else if (e.toString().startsWith("xmlns:")) {
        if (SAXTreeBuilder.URI_JSP.equals(value)) { //xmlns:jsp=http://java.sun.com/JSP/Page
          if (flags[0]) {
            flags[0] = false;
          } else {
            throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"xmlns:" + _default_prefix.peek(), _name}, parser.currentFileName(), debugInfo.start);
          }
          continue;
        }
        for (int j = 0; j < reservedPrefixes.length; j++) {
          if (e.equals(reservedPrefixes[j])) {
            throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE_VALUE_IS_RESERVED, new Object[]{value, "prefix", "taglib"}, parser.currentFileName(), debugInfo.start);
          }
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{e, _name}, parser.currentFileName(), debugInfo.start);
      }
    }

    if (flags[1]) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_ACTION, new Object[]{"version", _name}, parser.currentFileName(), debugInfo.start);
    }
  }

  public String getString(IDCounter id) {
    String s = _default_prefix_tag_start +_name;
    String name = null;
    String value = null;

    if (attributes != null && attributes.length > 0) {
      for (int i = 0; i < attributes.length; i++) {
        s = s + "\n\t";
        name = attributes[i].name.toString();
        value = attributes[i].value.toString();
        s = s  + name + "=\"" + value + "\"";
      }
    }else{
      // if the JSP document do not have jsp:root , create a valid one
      s = s + "\n\t";
      name = "xmlns:"+_default_prefix.peek();
      value = "http://java.sun.com/JSP/Page";
      s = s  + name + "=\"" + value + "\"";     
      
      s = s + "\n\t";
      name = "version";
      value = "2.1";
      s = s  + name + "=\"" + value + "\"";  
    }

    return s.concat("\n" + parser.getRootElement().JSP_ID + "=\"0\"\n>");
  }

}

