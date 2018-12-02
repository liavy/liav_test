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
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.DebugInfo;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;

/*
 * Represents <?xml tag in XML style. It is not mandatory element
 * in XML view of jsp page.
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class XMLTag extends JspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final char[][] attributeNames = {"version".toCharArray(), "encoding".toCharArray(), "standalone".toCharArray(), };
  // <?xml version="1.0"    encoding="xxx" standalone="yes" ili "no" ?>
  private char[] START_TAG_INDENT = "<?xml".toCharArray();

  /**
   * Creates new XMLTag
   *
   */
  public XMLTag() {

  }

  /**
   * Takes specific action corresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    debugInfo.writeStart(buffer, true);
    if (!parser.isXml()) {
      buffer.append("\t\t\tout.print(\"");
      String s = parser.getChars(startIndex, endIndex);
      char c;
      for (int i = 0; i < s.length(); i++) {
        switch (c = s.charAt(i)) {
          case '\'': {
            buffer.append("\\\'");
            break;
          }
          case '\"': {
            buffer.append("\\\"");
            break;
          }
          case '\\': {
            buffer.append("\\\\");
            break;
          }
          default: {
            buffer.append(c);
            break;
          }
        }
      }

      buffer.append("\");\r\n");

    }
    debugInfo.writeEnd(buffer);
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Returns the identification for this tag
   *
   * @return  array containing chars
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
    if (attributes == null || attributes.length < 1) {
      throw new JspParseException(JspParseException.ACTION_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE, new Object[]{"<?xml"},
              parser.currentFileName(), debugInfo.start);
    }

    boolean[] flags = new boolean[attributeNames.length];
    Arrays.fill(flags, true);
    Element e = null;

    for (int i = 0; i < attributes.length; i++) {
      e = attributes[i].name;

      if (parser.compare(e.startIndex, e.endIndex, attributeNames[0])) {
        if (flags[0]) {
          flags[0] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"version", "<?xml ..?>"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (parser.compare(e.startIndex, e.endIndex, attributeNames[1])) {

      } else if (parser.compare(e.startIndex, e.endIndex, attributeNames[2])) {

      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ACTION, new Object[]{e, "<?xml ..?>"},
                parser.currentFileName(), debugInfo.start);
      }
    } 

    if (flags[0]) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_ACTION, new Object[]{"version", "<?xml ..?>"},
              parser.currentFileName(), debugInfo.start);
    }
  }

  public Element parse(JspPageInterface parser) throws JspParseException {
    this.parser = parser;
    startIndex = parser.currentPos();
    Position p = parser.currentDebugPos();

    for (int i = 0; i < START_TAG_INDENT.length; i++) {
      parser.nextChar(); // "skipping <?xml
    } 

    parser.nextChar();
    parser.skipWhiteSpace();
    readAttributes("?>");

    if (parser.currentChar() != '?') {
      throw new JspParseException(JspParseException.IMPLICIT_PARSING_EXCEPTION_MISSING, new Object[]{"?"});
    } else if (parser.nextChar() != '>') {
      throw new JspParseException(JspParseException.IMPLICIT_PARSING_EXCEPTION_MISSING, new Object[]{">"});
    }

    debugInfo = new DebugInfo(p, parser.currentDebugPos(), parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
    parser.nextChar();
    endIndex = parser.currentPos();
    verifyAttributes();
    return this;
  }

}

