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

/**
 * Reresents directive staring with <%@
 * @author Ivo Simeonov
 * @version 4.0
 */
public abstract class JspDirective extends JspTag {
  /**
   * This must be returned from taglib directive for directive name. 
   */
  public static final String DIRECTIVE_NAME_TAGLIB = "taglib";
  
  /**
   * This must be returned from include directive for directive name. 
   */
  public static final String DIRECTIVE_NAME_INCLUDE = "include";

  /**
   * This is used as constant name for include directive in XML sytnax
   */
  public static final String DIRECTIVE_NAME_INCLUDE_XML = "directive.include";

  /*
   * denotes start of this type element
   */
  public static final char[] START_TAG_INDENT = new char[] {'<', '%', '@'};
  /*
   * denotes end of this type element
   */
  public static final char[] END_TAG_INDENT = new char[] {'%', '>'};
  /*
   * denotes all different names that may correspond
   * to this element
   */
  public static final char[][] directives = {"page".toCharArray(), "include".toCharArray(), "taglib".toCharArray(), "tag".toCharArray(), "attribute".toCharArray(), "variable".toCharArray()};
  /*
   * the name of this element
   */
  public char[] name;

  /**
   * Constructs new JspDirective
   *
   */
  public JspDirective() {
    tagType = JspTag.SINGLE_TAG;
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars <,%,@
   * in that order
   */
  public char[] indent() {
    return START_TAG_INDENT;
  }


  /**
   * Parses next tokens.
   *
   * @param   parser  parser to use
   * @return  newly created element
   * @exception   JspParseException  thrown if error occures
   * during parsing or verification
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    this.parser = parser;
    startIndex = parser.currentPos();
    Position p = parser.currentDebugPos();
    parser.nextChar();
    parser.nextChar();
    parser.nextChar(); // "skipping <%@"
    parser.skipWhiteSpace();
    parser.skipIndentifier();
    readAttributes("%");

    if (parser.currentChar() != '%') {
      throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
    } else if (parser.nextChar() != '>') {
      throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
    }

    endIndex = parser.currentPos();
    debugInfo = new DebugInfo(p, parser.currentDebugPos(), parser.currentFileName(), parser.getWebContainerParameters().isJspDebugSupport());
    parser.nextChar();
    verifyAttributes();
    return this;
  }

  /**
   * No verification is permited.This method always throws
   * RuntimeException "Not Implemented."
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  public void checkAttributes(String[] dirAttributes) throws JspParseException {
    if (attributes == null || attributes.length != 1) {
      //todo exception for actual dirAttributes.length
      throw new JspParseException(JspParseException.
        DIRECTIVE_MUST_HAVE_EXCACTY_ONE_SPECIFIED_ATTRIBUTE,
        new Object[]{"include"}, parser.currentFileName(),
        debugInfo.start);
    }

    if (!dirAttributes[0].equals(attributes[0].name.toString())) {
      throw new JspParseException(JspParseException.
        MISSING_ATTRIBUTE_IN_DIRECTIVE,
        new Object[]{"file", "include"},
        parser.currentFileName(), debugInfo.start);
    }
  }
  /**
   * The directive name denoted by "directive" in the following syntax - <%@ directive { attr='value' }* %>
   * @return - String with one of the directive names - "page", "include", "taglib", "tag", "attribute", "variable" or some custom type
   */
  public abstract String getDirectiveName();
  
  /**
   * Subclass this method if you want to perform some validation of the usage of this directive.
   * Note the tree of element is still not created.
   * @param parser JspPageInterface implementation
   * @throws JspParseException - if for example page directive is used in tag file or tag directive in JSP page 
   */
  public abstract void verifyDirective(JspPageInterface parser) throws JspParseException;

}

