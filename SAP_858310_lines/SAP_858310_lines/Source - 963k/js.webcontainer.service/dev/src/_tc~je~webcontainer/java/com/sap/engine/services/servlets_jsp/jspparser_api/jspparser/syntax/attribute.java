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


/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class Attribute extends Element {

  /*
   * name of this attribute
   */
  public Indentifier name;
  /*
   * value of this attribute
   */
  public Indentifier value;

  public boolean isNamed = false;
  /**
   * Constructs new attribute element
   *
   */
  public Attribute() {
    elementType = Element.ATTRIBUTE;
  }

  /**
   * Constructs new attribute element
   *
   */
  public Attribute(Indentifier name, Indentifier value, boolean isNamed) {
    elementType = Element.ATTRIBUTE;
    this.name = name;
    this.value = value;
    this.isNamed = isNamed;
  }

  /**
   * This methos is never invoked
   *
   * @exception   RuntimeException always
   */
  public void action() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  public void action(StringBuffer writer) throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  /**
   * This methos is never invoked
   *
   * @return char array indentification for this
   * parsing element
   * @exception   RuntimeException always
   */
  public char[] indent() throws JspParseException {
    throw new JspParseException(JspParseException.NOT_IMPLEMENTED);
  }

  /**
   * Returns new jsp element reprezenting a part of the jsp file
   *
   * @param   parser the parse for the current jsp file
   * @return  parsed element
   * @exception   JspParseException  thrown if error occures during
   * parsing
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    startIndex = parser.currentPos();
    parser.skipWhiteSpace();
    name = (Indentifier) (new Indentifier().parse(parser));
    parser.skipWhiteSpace();

    if (parser.currentChar() != '=') {
      throw new JspParseException(JspParseException.EXPECTING_INSTEAD_OF, new Object[]{"=", escape(parser.currentChar())},
              parser.currentFileName(), parser.currentDebugPos());
    } else {
      parser.nextChar();
      parser.skipWhiteSpace();
    }
    if( parser.currentChar() != '\'' && parser.currentChar() != '"' ){
      throw new JspParseException(JspParseException.ATTRIBUTE_VALUES_MUST_BE_QUOTED,new Object[]{name.toString()},
              parser.currentFileName(), parser.currentDebugPos());
    }
    value = (Indentifier) (new Indentifier().parse(parser));
    endIndex = parser.currentPos();
    return this;
  }

  /**
   * Checks whether the name of this attribute is the specified
   * name
   *
   * @param   name name to compare with
   * @return  true if the name of this attribute is the specified
   */
  public boolean isName(String name) throws JspParseException {
    return this.name.equals(name);
  }

  /**
   * Checks whether the name of this attribute is the specified
   * name
   *
   * @param   name  name to compare with
   * @return  true if the name of this attribute is the specified
   */
  public boolean isName(char[] name) throws JspParseException {
    return this.name.equals(name);
  }

  public String getString(IDCounter id) {
    return null;
  }

  public String toString() {
    return name.toString()+"="+value.toString();
  }
}

