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

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class ForwardTag extends CustomJspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"page"};

  /**
   * Creates new ForwardTag
   *
   */
  public ForwardTag() {
    super();
    _name = "forward";
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
        debugInfo.writeStart(buffer, false);
        String page = getAttributeValue("page");
        buffer.append("\t\t\tif (true) {").append("\n");

        if (isRuntimeExpr(page)) {
          buffer.append("\t\t\t\t_jspx_pageContext.forward(").append(evaluateExpression(page)).append(");").append("\n");
        } else {
          buffer.append("\t\t\t\t_jspx_pageContext.forward(\"").append(page).append("\");").append("\n");
        }
        //JSP8.3
        //It is legal for a tag file to forward to a page via the <jsp:forward> standard
        //action. Just as for JSP pages, the forward is handled through the request
        //dispatcher. Upon return from the RequestDispatcher.forward method, the
        //generated tag handler must stop processing of the tag file and throw javax.servlet.jsp.SkipPageException.
        if (parser.isTagFile()) {
          buffer.append("\t\t\t\tthrow new javax.servlet.jsp.SkipPageException();").append("\n");
        } else {
          buffer.append("\t\t\t\treturn;").append("\n");
        }
        buffer.append("\t\t\t};").append("\n");
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
   * @return  array containing chars
   * "<jsp:forward"
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
    return (this.copy) ? new ForwardTag() : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.FORWARD);
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
    return (JspTag) (new ForwardTag().parse(this.parser));
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length != 1) {
      throw new JspParseException(JspParseException.ACTRION_MUST_HAVE_EXACTLY_ONE_SPECIFIED_ATTRIBUTE, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }

    String name = attributes[0].name.toString();
    if (!attributeNames[0].equals(name)) {
      throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ACTION, new Object[]{name, _name}, parser.currentFileName(), debugInfo.start);
    }
  }

}

