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

import java.util.Enumeration;
import java.util.StringTokenizer;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.ParserAttributes;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspPageDirective extends JspDirective {

  /*
   * denotes the name of this type element
   */
  private static final String DIRECTIVE_NAME = "page";
  /*
   * all attributes that this element can
   * accept
   */
  public static final String[] attributeNames = {"language", "extends", "import", "session", "buffer", "autoFlush",
    "isThreadSafe", "info", "errorPage", "isErrorPage", "contentType", "pageEncoding", "isELIgnored",
    "deferredSyntaxAllowedAsLiteral", "trimDirectiveWhitespaces"};
  /*
   * Must save attributes for this directive, to use in XML view of jsp.
   */
  public Attribute[] attributes0 = null;

  /**
   * Constructs new JspPageDirective
   *
   */
  public JspPageDirective() {
    elementType = Element.PAGE_DIRECTIVE;
    name = DIRECTIVE_NAME.toCharArray();
    _name = DIRECTIVE_NAME;
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    // extends
    if (parser.getPageActionTaken()) {
      return;
    } else {
      parser.setPageActionTaken(true);
      attributes = new Attribute[parser.getPageAttributes().size()];
      Enumeration e = parser.getPageAttributes().elements();
      int i = 0;

      while (e.hasMoreElements()) {
        attributes[i] = (Attribute) e.nextElement();
        i++;
      }
    }

    String attributeValue = getAttributeValue("extends");

    if (attributeValue != null) {
      Class cl = null;
      Class httpJsp = null;
      try {
        cl = parser.getApplicationClassLoader().loadClass(attributeValue);
        httpJsp = parser.getApplicationClassLoader().loadClass("javax.servlet.jsp.HttpJspPage");
      } catch (ClassNotFoundException e) {
        throw new JspParseException(JspParseException.ERROR_IN_LOADING_SUPERCLASS_OF_THE_SERVLET_THE_CLASS_IS, new Object[]{attributeValue},
                parser.currentFileName(), debugInfo.start);
      } catch (NoClassDefFoundError e) {
        throw new JspParseException(JspParseException.ERROR_IN_LOADING_SUPERCLASS_OF_THE_SERVLET_THE_CLASS_IS, new Object[]{attributeValue},
                parser.currentFileName(), debugInfo.start);
      }

      if (httpJsp.isAssignableFrom(cl)) {
        parser.getExtendsDirective().append(attributeValue);
      } else {
        throw new JspParseException(JspParseException.JSP_PAGE_MUSR_BE_INSTANCE_OF_HTTPJSPPAGE_HTTPJSPPAGE_IS_NOT_ASSIGNABLE_FROM_CLASS, new Object[]{attributeValue},
                parser.currentFileName(), debugInfo.start);
      }
    }

    // isThreadSafe
    attributeValue = getAttributeValue("isThreadSafe");

    if (attributeValue != null) {
      if (attributeValue.equalsIgnoreCase("false")) {
        parser.getImplemetsDirective().append("javax.servlet.SingleThreadModel");
      }
    }

    // import
    attributeValue = parser.getPageImportAttribute();

    if (attributeValue != null) {
      StringTokenizer toker = new StringTokenizer(attributeValue, ",");

      while (toker.hasMoreTokens()) {
        parser.getImportDirective().append("import ").append(toker.nextToken().trim()).append(';').append("\r\n");
      }
    }

    // session
    attributeValue = getAttributeValue("session");

    if (attributeValue != null) {
      parser.setCreateSession(new Boolean(attributeValue).booleanValue());
    }

    // isErrorPage
    attributeValue = getAttributeValue("isErrorPage");

    if (attributeValue != null) {
      parser.setIsErrorPage(new Boolean(attributeValue).booleanValue());
    }

    // autoFlush
    attributeValue = getAttributeValue("autoFlush");

    if (attributeValue != null) {
      parser.setAutoFlush(new Boolean(attributeValue).booleanValue());
    }

    // buffer
    attributeValue = getAttributeValue("buffer");

    if (attributeValue != null) {
      if (attributeValue.endsWith("kb")) {
        parser.setBufferSize(new Integer(attributeValue.substring(0, attributeValue.length() - 2)).intValue() * 1024);
      } else if (attributeValue.equals("none")) {
        parser.setBufferSize(0);

        if ((!parser.getAutoFlush()) && (parser.getBufferSize() == 0)) {
          throw new JspParseException(JspParseException.INCORRECT_TO_SET_AUTOFLUSH_FALSE_AND_BUFFER_NONE,
                  parser.currentFileName(), debugInfo.start);
        }

        parser.setAutoFlush(true);
      } else {
        parser.setBufferSize(new Integer(attributeValue).intValue() * 1024);
      }
    } else {
      int bufferSize = ServiceContext.getServiceContext().getWebContainerProperties().getServletOutputStreamBufferSize();
      if (bufferSize < ParserAttributes.MIN_BUFFER_SIZE) {
        bufferSize = ParserAttributes.MIN_BUFFER_SIZE;
      }
      parser.setBufferSize(bufferSize);
    }

    // contentType
    attributeValue = getAttributeValue("contentType");

    if (attributeValue != null) {
      parser.setContentType(attributeValue);
    }

    // pageEncoding detected in page preprocessing - see ParserImpl.parse()
    //attributeValue = getAttributeValue("pageEncoding");

    //if (attributeValue != null) {
    //  parser.setPageEncoding(attributeValue);
    //}

    // info
    attributeValue = getAttributeValue("info");

    if (attributeValue != null) {
      parser.setServletInfo(attributeValue);
    }

    // errorPage
    attributeValue = getAttributeValue("errorPage");

    if (attributeValue != null) {
      parser.setErrorPage(attributeValue);
    }
    // isELIgnored
    attributeValue = getAttributeValue("isELIgnored");

    if (attributeValue != null) {
      parser.setIsELIgnored(new Boolean(attributeValue).booleanValue());
    }

    //JSP2.1
    // deferredSyntaxAllowedAsLiteral
    attributeValue = getAttributeValue("deferredSyntaxAllowedAsLiteral");

    if (attributeValue != null) {
      parser.setDeferredSyntaxAllowedAsLiteral(new Boolean(attributeValue).booleanValue());
    }

    // deferredSyntaxAllowedAsLiteral
    attributeValue = getAttributeValue("trimDirectiveWhitespaces");

    if (attributeValue != null) {
      parser.setTrimDirectiveWhitespaces(new Boolean(attributeValue).booleanValue());
    }
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null) {
      throw new JspParseException(JspParseException.DIRECTIVE_MUST_HAVE_AT_LEAST_ONE_SPECIFIED_ATTRIBUTE, new Object[]{"page"},
              parser.currentFileName(), debugInfo.start);
    }
    Object o = null;
    String attributeName0;
    //save attributes
    attributes0 = attributes;

    for (int i = 0; i < attributes.length; i++) {
      attributeName0 = attributes[i].name.toString();
      if (attributeName0.equals("import")) {
        if (parser.getPageImportAttribute() == null) {
          parser.setPageImportAttribute(attributes[i].value.toString());
        } else {
          parser.setPageImportAttribute(parser.getPageImportAttribute() + "," + attributes[i].value.toString());
        }

        continue;
      }
      o = parser.getPageAttributes().get(attributeName0);
      if (o == null) {
        parser.getPageAttributes().put(attributeName0, attributes[i]);
      } else 
      {
      	if ( parser.getJspProperty().isOldApplication() )
      	{
				throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{attributes[i].name, "page"},
                parser.currentFileName(), debugInfo.start);
      	}
      	else
      	{
	      	if (!((Attribute)o).value.equals(attributes[i].value.toString())) {
	        	throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_DIRECTIVE, new Object[]{attributes[i].name, "page"},
                    parser.currentFileName(), debugInfo.start);
      	}
      }
     }
    }
    String name = null;
    String value = null;

    for (int i = 0; i < attributes.length; i++) {
      name = attributes[i].name.toString();
      value = attributes[i].value.toString();
      if (attributeNames[0].equals(name)) { // language
        if (!("java".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "language", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[1].equals(name)) { // extends
      } else if (attributeNames[2].equals(name)) { // import
      } else if (attributeNames[3].equals(name)) { // session
        if (!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "session", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[4].equals(name)) { // buffer
        if (!("none".equals(value))) {
          if (value.indexOf("kb") == -1) {
            throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "buffer", "page"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
      } else if (attributeNames[5].equals(name)) { // autoflush
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "autoFlush", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[6].equals(name)) { // isThreadSafe
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "isThreadSafe", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[7].equals(name)) { // info
      } else if (attributeNames[8].equals(name)) { // errorPage
      } else if (attributeNames[9].equals(name)) { // isErrorPage
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "isErrorPage", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
        // JSP.1.4.3 Using JSPs as Error Pages
        // A JSP container must detect if a JSP error page is self-referencing and throw a translation error.
        if( "true".equals(value) ) {
          String errorPage = getAttributeValue("errorPage");
          if( errorPage != null ) {
            if( parser.currentFileName().equals(parser.getRealPath(errorPage)) ) {
              throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{errorPage, "errorPage", "page"},
                  parser.currentFileName(), debugInfo.start);              
            }
          }
        }
      } else if (attributeNames[10].equals(name)) { // contentType 
      } else if (attributeNames[11].equals(name)) { // pageEncoding
      } else if (attributeNames[12].equals(name)) { // isElIgnored
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "isELIgnored", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[13].equals(name)) { // deferredSyntaxAllowedAsLiteral
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "deferredSyntaxAllowedAsLiteral", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[14].equals(name)) { // trimDirectiveWhitespaces
        if (!("true".equals(value) || "false".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_DIRECTIVE, new Object[]{value, "trimDirectiveWhitespaces", "page"},
                  parser.currentFileName(), debugInfo.start);
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_DIRECTIVE, new Object[]{name, "page"},
                parser.currentFileName(), debugInfo.start);
      }
    } 
    
  }

  public String getString(IDCounter id) {
    String s = _default_prefix_tag_start + "directive." + _name;
    String name = null;
    String value = null;

    if (attributes0 != null) {
      for (int i = 0; i < attributes0.length; i++) {
        s = s + "\n\t";
        name = attributes0[i].name.toString();
        value = attributes0[i].value.toString();
        s = s + name + "=\"" + value + "\"";
      } 
    }
    s = s + getId(id);
    return s.concat("\n/>");
  }
  
  /**
   * The directive name denoted by "directive" in the following syntax - <%@ directive { attr='value' }* %>
   * @return - String with one of the directive names - "page", "include", "taglib", "tag", "attribute", "variable" or some custom type
   */
  public String getDirectiveName(){
    return DIRECTIVE_NAME;
  }
  
  /**
   * Subclass this method if you want to perform some validation of the usage of this directive.
   * Note the tree of element is still not created.
   * @param parser
   * @throws JspParseException - if for example page directive is used in tag file or tag directive in JSP page 
   */
  public void verifyDirective(JspPageInterface parser) throws JspParseException{
    if( parser.isTagFile() ){
      throw new JspParseException(JspParseException.ELEMENT_CANNOT_BE_USED_IN_TAG_FILES, new Object[]{getDirectiveName()}, parser.currentFileName(), parser.currentDebugPos());
    }
  }
}

