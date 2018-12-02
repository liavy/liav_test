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

import java.util.LinkedList;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.AttributeTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.ExpressionTag;

/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public class ElementCollection extends Element {

  /**
   * Constructs new ElementCollection
   *
   */
  public ElementCollection() {

  }

  /**
   * Constructs new ElementCollection linked with the
   * specified parser
   *
   * @param   parser  parse to use
   */
  public ElementCollection(JspPageInterface parser) {
    this.parser = parser;
  }

  /**
   * Loops over all elements that contain this element
   * and takes their action
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer writer) throws JspParseException {
    if (elements != null) {
      for (int i = 0; i < elements.length; i++) {
        elements[i].parentCustomTag = this.parentCustomTag;
        if (writer == null) {          
          elements[i].action();
        } else {
          elements[i].action(writer);
        }
      }
    }
  } 

  public void action() throws JspParseException {
    if (parser == null) {
      action(null);
    } else {
      action(parser.getScriptletsCode());
    }
  }

  /**
   * No indent for this element
   *
   * @return  always null
   */
  public char[] indent() {
    return null;
  }

  //
  /**
   * During parsing this element stores all elements that
   * are found as next elements in the jsp file
   *
   * @param   parser  parser to use
   * @return  Element containig all elements in the jsp file
   * coresponding to the specified parser
   * @exception   JspParseException  thrown if error occures during
   * parsing or verification
   */
  public Element parse(JspPageInterface parser) throws JspParseException {
    ElementCollection ec = new ElementCollection(parser);
    ec.startIndex = parser.currentPos();
    LinkedList l = new LinkedList();
    Element e;
    Element ee;
    TemplateData temp = null;

    while (true) {
      ee = parser.getRootElement().getNextElement(parser);

      if (ee != null) {
        e = ee.parse(parser);

        if (e == null) {
          throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
        }

        if (e instanceof TemplateData) {
          if (temp != null) {
            temp.merge((TemplateData) e);
          } else {
            temp = (TemplateData) e;
          }
        } else {
          if (temp != null) {
            l.add(temp);
            l.add(e);
            temp = null;
          } else {
            l.add(e);
          }
        }

        //        l.add(e);
      } else {
        if (!parser.endReached()) {
          throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
        }

        if (temp != null) {
          l.add(temp);
          temp = null;
        }

        break;
      }
    }

    ec.endIndex = parser.currentPos();
    ec.elements = new Element[l.size()];
    ec.elements = (Element[]) l.toArray(ec.elements);
    return ec;
  }

  /**
   * During parsing this element stores all elements to the
   * specified token that are found as next elements in the
   * jsp file
   *
   * @param   parser  parser to use
   * @param   endIndent  prefix for the end token on witch
   * this element stops parsing
   * @return  element containing all elements to the specified
   * token in the jsp file,coresponding to the specified
   * parser
   * @exception   JspParseException  thrown if error occures during
   * parsing or verification
   */
  public Element parseTo(JspPageInterface parser, char[] endIndent) throws JspParseException {
    ElementCollection ec = new ElementCollection(parser);
    ec.startIndex = parser.currentPos();
    LinkedList l = new LinkedList();
    Element e;
    TemplateData temp = null;

    while (true) {
      e = parser.getRootElement().getNextElement(parser, endIndent);

      if (e != null) {
        e = e.parse(parser);

        if (e instanceof TemplateData) {
          if (temp != null) {
            temp.merge((TemplateData) e);
          } else {
            temp = (TemplateData) e;
          }
        } else {
          if (temp != null) {
            l.add(temp);
            l.add(e);
            temp = null;
          } else {
            l.add(e);
          }
        }
      } else {
        //check if found;
        if (temp != null) {
          l.add(temp);
          temp = null;
        }

        break;
      }
    }

    ec.endIndex = parser.currentPos();
    ec.elements = new Element[l.size()];
    ec.elements = (Element[]) l.toArray(ec.elements);
    return ec;
  }

  /**
   * Sets a parser for this element
   *
   * @param   parser  parser to use with this element
   */
  public void setParser(JspPageInterface parser) {
    this.parser = parser;
  }

  public String getString(IDCounter id) throws JspParseException {
    StringBuilder ret = new StringBuilder();
    if (elements != null) {
      for (int i = 0; i < elements.length; i++) {
        if (elements[i] instanceof TemplateData) {
          if (((TemplateData) elements[i]).isEmpty()) {
            continue;
          }
        }

        ret.append("\n").append(elements[i].getString(id));
      }
    }
    return ret.toString();
  }

}

