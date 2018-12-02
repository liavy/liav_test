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
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.IDCounter;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 * Represents jsp:text tag in XML style.
 *
 * @author Boby Kadrev
 * @version 4.0
 */
public class TextTag extends CustomJspTag {


  private String s = null;

  /**
   * Creates new TextTag
   *
   */
  public TextTag() {
    super();
    _name = "text";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  /**
   * TextTag does nothing . It don't have attributes and only can contain TemplateText and expression language 
   * that are handled by the JspElement that forms the whole construction. 
   * <jsp:text> some text and ${el} </jsp:text> - is formed like this
   * JspElement 
   * 	- startTag 	- TextTag - type  START_TAG
   *  - body			- TemplateData that contains "some text and ${el}"
   * 	- endTag		- TextTag	- type SINGLE_TAG 
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {}

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Retuns the indentification for this tag
   *
   * @return  array containing chars
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
	protected JspElement createJspElement() {
		return new JspElement(JspElement.TEXT);
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
		return (JspTag) (new TextTag().parse(this.parser));
	}
	
	/**
	 * Returnes element to witch will be associated
	 * current parsing.
	 *
	 * @return object that is associated as current element
	 * of parsing
	 */
	protected CustomJspTag createThis() {
		return (this.copy) ? new TextTag() : this;
	}
		
  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes != null && attributes.length > 0) {
      throw new JspParseException(JspParseException.ELEMENT_DOES_NOT_TAKE_ANY_ATTRIBUTES, new Object[]{_name}, parser.currentFileName(), debugInfo.start);
    }
  }



}

