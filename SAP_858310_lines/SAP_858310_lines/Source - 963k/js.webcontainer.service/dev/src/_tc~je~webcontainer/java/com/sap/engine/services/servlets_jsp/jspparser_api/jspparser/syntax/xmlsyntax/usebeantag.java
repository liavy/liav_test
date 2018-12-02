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

import java.util.*;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ELExpression;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.InnerExpression;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspElement;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.JspTag;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class UseBeanTag extends CustomJspTag {

  /*
   * all attributes that this element can
   * accept
   */
  private static final String[] attributeNames = {"id", "scope", "class", "type", "beanName"};

  /**
   * Creates new UseBeanTag
   *
   */
  public UseBeanTag() {
    super();
    _name = "useBean";
    START_TAG_INDENT = (_default_prefix_tag_start + _name).toCharArray();
    CMP_0 = (_default_prefix_tag_end + _name + END).toCharArray();
  }

  //  /**
  //   *
  //   *
  //   * @param   startTagIndent
  //   * @param   endTagIndent
  //   */
  //  public UseBeanTag(char[] startTagIndent ,char[] endTagIndent) {
  //    this();
  //    START_TAG_INDENT = startTagIndent;
  //    CMP_0 = endTagIndent;
  //  }
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
        String id = getAttributeValue("id");
        String typeName = getAttributeValue("type");
        String className = getAttributeValue("class");
        String beanName = getAttributeValue("beanName");
        boolean isExpression = false;
        if (beanName != null) {
          isExpression = isRuntimeExpr(beanName);
          if (isExpression) {
            ELExpression expr = new ELExpression(beanName);
            if( expr.isEL() ) {
              beanName = InnerExpression.getExpresionEvaluation(beanName, String.class, parser);
            } else {
              beanName = evaluateExpression(beanName);
            }
          }
        }
        String scope = getAttributeValue("scope");
        String tempId = (String) parser.getBeanId().get(id);

        if (tempId == null) {
          parser.getBeanId().put(id, id);
        } else {
          throw new JspParseException(JspParseException.DUBLICATED_ATTRIBUTE_VALUE_IN_ACTION, new Object[]{"id", id, _name},
                  parser.currentFileName(), debugInfo.start);
        }
        
        if( className != null ) {
          Class classClass = null;
          try{
            classClass = Class.forName(className,false,parser.getParserParameters().getAppClassLoader());
          }catch (ClassNotFoundException e) {
            throw new JspParseException(JspParseException.CANNOT_RESOLVE_BEAN_CLASS, new Object[]{className},e );
          }
          
          // JSP JSP 5.1
          //If type and class are present, class must be assignable to type (in the Java platform sense). 
          //For it not to be assignable is a translationtime error.
          if ( typeName != null ){
              Class typeClass = null;
              try{
                  typeClass = Class.forName(typeName,false,parser.getParserParameters().getAppClassLoader());
                  
              }catch (ClassNotFoundException e) {
                  throw new JspParseException(JspParseException.CANNOT_RESOLVE_BEAN_CLASS, new Object[]{typeClass},e );
              }              
              if ( !typeClass.isAssignableFrom(classClass) ){
                  throw new JspParseException(JspParseException.USE_BEAN_CLASS_IS_NOT_ASSIGNABLE_TO_TYPE,new Object[]{id,className, typeName});
              }
          }
        }


            
        if (typeName == null) {
          typeName = className;
        }

        if (typeName == null) {
          typeName = beanName;
        }

        if (typeName == null) {
          throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR_ACTION_NOT_VERIFIED, new Object[]{_name});
        }

        String throwExName = "_jspx_throw_Ex_" + id;
        parser.getAllBeanNames().put(id, typeName);

        if (className == null && beanName == null) {
          buffer.append("\t\tboolean ").append(throwExName).append(" = true;").append("\r\n");
        } else {
          buffer.append("\t\tboolean ").append(throwExName).append(" = false;").append("\r\n");
        }

        buffer.append("\t\t").append(typeName).append(" ").append(id).append(" = null;").append("\r\n");

        if (scope == null) {
          scope = "page";
        }

        if (scope.equalsIgnoreCase("page")) {
          buffer.append("\t\t\t").append(id).append(" = (").append(typeName).append(")_jspx_pageContext.getAttribute(\"").append(id).append("\", PageContext.PAGE_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("request"))) {
          buffer.append("\t\t\t").append(id).append(" = (").append(typeName).append(")_jspx_pageContext.getAttribute(\"").append(id).append("\", PageContext.REQUEST_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("session"))) {
          buffer.append("\t\t\t").append(id).append(" = (").append(typeName).append(")_jspx_pageContext.getAttribute(\"").append(id).append("\", PageContext.SESSION_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("application"))) {
          buffer.append("\t\t\t").append(id).append(" = (").append(typeName).append(")_jspx_pageContext.getAttribute(\"").append(id).append("\", PageContext.APPLICATION_SCOPE);").append("\r\n");
        }

        buffer.append("\t\tif (").append(id).append(" == null) {").append("\r\n");
        buffer.append("\t\t\tif (").append(throwExName).append(") {").append("\r\n");
        buffer.append("\t\t\t\t\tthrow new java.lang.InstantiationException(\"Not defined beanName or class!\");").append("\r\n");
        buffer.append("\t\t\t}").append("\r\n");
        buffer.append("\t\t\ttry {").append("\r\n");
        if (isExpression) {
          buffer.append("\t\t\t\t").append(id).append(" = (").append(typeName).append(") java.beans.Beans.instantiate(getClass().getClassLoader(), ").append(beanName == null ? typeName : beanName).append(");").append("\r\n");
        } else {
          buffer.append("\t\t\t\t").append(id).append(" = (").append(typeName).append(") java.beans.Beans.instantiate(getClass().getClassLoader(), \"").append(beanName == null ? typeName : beanName).append("\");").append("\r\n");
        }
        buffer.append("\t\t\t} catch (Exception exc) {").append("\r\n");
        buffer.append("\t\t\t\t\tthrow new java.lang.InstantiationException(\"ID018236: Cannot instantiate bean. \" + exc.toString());").append("\r\n");
        buffer.append("\t\t\t}").append("\r\n");

        if (scope.equalsIgnoreCase("page")) {
          buffer.append("\t\t_jspx_pageContext.setAttribute(\"").append(id).append("\", ").append(id).append(", PageContext.PAGE_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("request"))) {
          buffer.append("\t\t_jspx_pageContext.setAttribute(\"").append(id).append("\", ").append(id).append(", PageContext.REQUEST_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("session"))) {
          buffer.append("\t\t_jspx_pageContext.setAttribute(\"").append(id).append("\", ").append(id).append(", PageContext.SESSION_SCOPE);").append("\r\n");
        } else if ((scope.equalsIgnoreCase("application"))) {
          buffer.append("\t\t_jspx_pageContext.setAttribute(\"").append(id).append("\", ").append(id).append(", PageContext.APPLICATION_SCOPE);").append("\r\n");
        }

        buffer.append("\t\t}").append("\r\n");
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
   * "<jsp:useBean"
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
    return (this.copy) ? new UseBeanTag() : this;
  }

  /**
   * Returnes element to witch will be associated
   * current parsing.
   *
   * @return object that is associated as current element
   * of parsing
   */
  protected JspElement createJspElement() {
    return new JspElement(JspElement.USE_BEAN);
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
    return (JspTag) (new UseBeanTag().parse(this.parser));
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occures
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    if (attributes == null || attributes.length < 2) {
      throw new JspParseException(JspParseException.ACTION_MUST_HAVE_AT_LEAST_TWO_SPECIFIED_ATTRIBUTES, new Object[]{_name},
              parser.currentFileName(), debugInfo.start);
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
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"id", _name},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[1].equals(name)) {
        if (flags[1]) {
          flags[1] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"scope", _name},
                  parser.currentFileName(), debugInfo.start);
        }
        if (!("page".equals(value) || "request".equals(value) || "session".equals(value) || "application".equals(value))) {
          throw new JspParseException(JspParseException.INVALID_VALUE_FOR_ATTRIBUTE_IN_ACTION, new Object[]{value, "scope", _name},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[2].equals(name)) {
        if (flags[2]) {
          flags[2] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"class", _name},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[3].equals(name)) {
        if (flags[3]) {
          flags[3] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"type", _name},
                  parser.currentFileName(), debugInfo.start);
        }
      } else if (attributeNames[4].equals(name)) {
        if (flags[4]) {
          flags[4] = false;
        } else {
          throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"beanName", _name},
                  parser.currentFileName(), debugInfo.start);
        }
      } else {
        throw new JspParseException(JspParseException.UNRECOGNIZED_ATTRIBUTE_IN_ACTION, new Object[]{name, _name},
                parser.currentFileName(), debugInfo.start);
      }
    }

    if (flags[0]) {
      throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_IN_ACTION, new Object[]{"id", _name},
              parser.currentFileName(), debugInfo.start);
    }

    if (!flags[2]) {
      if (!flags[4]) {
        throw new JspParseException(JspParseException.ATTRIBUTE_AND_ATTRIBUTE_CANNOT_BE_SPECIFED_TOGETHER_IN_ACTION, new Object[]{"class", "beanName", _name},
                parser.currentFileName(), debugInfo.start);
      }
    } else {
      if (flags[3]) {
        throw new JspParseException(JspParseException.MISSING_ATTRIBUTE_OR_ATTRIBUTE_IN_ACTION, new Object[]{"class", "type", _name},
                parser.currentFileName(), debugInfo.start);
      }
    }
  }

  // can be expression
  private String getBeanName() throws JspParseException {
    String ret = getAttributeValue("beanName");

    if (ret != null) {
      ret = evaluateExpression(ret);
    }

    return ret;
  }

}

