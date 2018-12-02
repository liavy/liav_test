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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagData;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.PluginGenerator;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.*;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagBeginGenerator;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagEndGenerator;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.FunctionMapperImpl;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;

/**
 * Represent jsp tags with body
 * Consists of :
 * JspTag startTag
 * Element body
 * JspTag endTag
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspElement extends Element {

  /*
   * jsp element types
   */
  public static final int CUSTOM = 0;

  // new
  public static final int ROOT          = 1001;
  public static final int TEXT          = 1002;
  public static final int DECLARATION   = 1003;
  public static final int SCRIPLET      = 1004;
  public static final int ATTRIBUTE     = 1016;
  public static final int BODY          = 1017;
  public static final int INVOKE        = 1018;
  public static final int DO_BODY       = 1019;
  public static final int ELEMENT       = 1020;
  public static final int OUTPUT        = 1021;

  public static final int DIRECTIVE_PAGE        = 1022;
  public static final int DIRECTIVE_ATTRIBUTE   = 1023;
  public static final int DIRECTIVE_INCLUDE     = 1024;
  public static final int DIRECTIVE_TAG         = 1025;
  public static final int DIRECTIVE_VARIABLE    = 1026;


  public static final int USE_BEAN = 1;
  public static final int FORWARD = 2;
  public static final int GET_PROPERTY = 3;
  public static final int SET_PROPERTY = 4;
  public static final int INCLUDE = 5;
  public static final int PARAM = 6;
  public static final int PARAMS = 7;
  public static final int FALLBACK = 8;
  public static final int PLUGIN = 9;
  public static final int EXPRESSION = 10;

  /*
   * start tag of this element
   */
  JspTag startTag;
  /*
   * body of this element
   */
  Element body;
  /*
   * all found parameters in this element body
   */
  Map<String, String> params;

  /*
   * fallback string if specified
   */
  String fallback;
  /*
   * end tag of this element
   */
  JspTag endTag;

  private JspElement parentJspElement;

  /*
   * type of this element
   */
  public int type;

  /**
   * Current iddent append in the beginning of every line
   */
  private String indent = "\t\t\t";

  /**
   * Constructs new JspElement
   *
   * @param   type  type for this jsp element
   */
  public JspElement(int type) {
    this.type = type;
    params = new HashMap<String, String>();
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Takes specific action coresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
 public void action(StringBuffer buffer) throws JspParseException {

    switch (type) {
      case FALLBACK: {
        throw new JspParseException(JspParseException.ACTION_CAN_BE_USED_ONLY_PLUGIN_ACTION, new Object[]{_default_prefix_tag_start + "fallback"} ,
                parser.currentFileName(), debugInfo.start);
      }

      case ROOT: {
        if (body != null) {
          body.action(buffer);
        }
        break;
      }

      case BODY: {
        if (debugInfo != null) {
          debugInfo.writeStart(buffer, false);
        }
        if (body != null) {
          checkForJspAttribute(buffer);
          body.action(buffer);
        }
        if (debugInfo != null) {
          debugInfo.writeEnd(buffer);
        }
        break;
      }

      case TEXT: {
        if (body != null) {
          if (body instanceof TemplateData) {
            ((TemplateData)body).isTextTag = true;
						body.action(buffer);
          } else {
            if( body.elements != null ){
              throw new JspParseException(JspParseException.NEITHER_SCRIPTING_NOR_SUBELEMENTS_ARE_ALOWED,new Object[]{"<jsp:text>"});
            }
          }
        }
        break;
      }

      case USE_BEAN: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        useBeanAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case FORWARD: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        forwardAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case GET_PROPERTY: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        getPropertyAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case SET_PROPERTY: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        setPropertyAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case INCLUDE: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        includeAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case PARAM: {
        throw new JspParseException(JspParseException.ELEMENT_MUST_BE_USED_ONLY_IN, new String[]{"jsp:param", "jsp:include, jsp:forward and jsp:params"});
      }
      case PARAMS: {
        throw new JspParseException(JspParseException.ACTION_CAN_BE_USED_ONLY_PLUGIN_ACTION, new Object[]{_default_prefix_tag_start + "params"} ,
                parser.currentFileName(), debugInfo.start);
      }
      case PLUGIN: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        pluginAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }
      case CUSTOM: {
        debugInfo.writeStart(buffer, false);
        customTagAction(buffer);
        
        break;
      }
      case EXPRESSION: {
        if (parser.getJspProperty() != null && parser.getJspProperty().isScriptingInvalid().equalsIgnoreCase("true")) {
          throw new  JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE,
                  parser.currentFileName(), parser.currentDebugPos());
        }
				//assert body instanceof TemplateData;
        if ( body  instanceof TemplateData){
          debugInfo.writeStart(buffer, false);
          buffer.append("\t\t\tout.print(").append(((TemplateData)body).getValue()).append(");\r\n");
          debugInfo.writeEnd(buffer);
        }
        break;
      }
      case DECLARATION: {
        if( parser.getJspProperty() != null && parser.getJspProperty().isScriptingInvalid().equalsIgnoreCase("true")){
          throw new  JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE,
                  parser.currentFileName(), parser.currentDebugPos());
        }
        if ( body instanceof TemplateData ) {
          debugInfo.writeStart(parser.getDeclarationsCode(), false);
          String declaration = ((TemplateData)body).getValue();
          if( parser.getWebContainerParameters().isJspDebugSupport() ){
            declaration = StringUtils.escapeEndComment(declaration);
          }
          parser.setDeclarationsCode(parser.getDeclarationsCode().append("\t\t\t").append(declaration).append("\r\n"));
          debugInfo.writeEnd(parser.getDeclarationsCode());
        }
      }
      break;
      case SCRIPLET: {
        if ( parser.getJspProperty() != null && parser.getJspProperty().isScriptingInvalid().equalsIgnoreCase("true") ){
          throw new JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_HERE,
                  parser.currentFileName(), parser.currentDebugPos());
        }
        if ( body instanceof TemplateData ){
          debugInfo.writeStart(buffer, false);
          String scriptlet = ((TemplateData)body).getValue();
          if( parser.getWebContainerParameters().isJspDebugSupport() ){
            scriptlet = StringUtils.escapeEndComment(scriptlet);
          }
          buffer.append("\t\t\t").append(scriptlet).append("\r\n");
          debugInfo.writeEnd(buffer);
        }
      }
      break;
      //NEW actions
      case ELEMENT: {
        debugInfo.writeStart(buffer, false);
        elementTagAction(buffer);
        debugInfo.writeEnd(buffer);
        break;
      }

      case INVOKE: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        startTag.action(buffer);
        debugInfo.writeEnd(buffer);
      }

      case DO_BODY: {
        debugInfo.writeStart(buffer, false);
        checkForJspAttribute(buffer);
        startTag.action(buffer);
        debugInfo.writeEnd(buffer);
      }
      case DIRECTIVE_INCLUDE:
      case DIRECTIVE_VARIABLE:
      case DIRECTIVE_PAGE:
      case DIRECTIVE_ATTRIBUTE:
      case DIRECTIVE_TAG:{
        startTag.action();
      }

      
    }
  }

  /**
   * No indent for this element
   *
   * @return null
   */
  public char[] indent() {
    return null;
  }

  /**
   * Parses next tokens
   *
   * @param   parser  parser to use
   * @return    newly created element
   */
  public Element parse(JspPageInterface parser) {
    return this;
  }

  /**
   * Returns JspTag as the end of this element
   *
   * @return     this element end tag
   */
  public JspTag getEndTag() {
    return this.endTag;
  }

  /**
   * Sets JspTag as the end of this element
   *
   * @param   endTag  end tag to set for this element
   */
  public void setEndTag(JspTag endTag) {
    this.endTag = endTag;
  }

  /**
   * Returns JspTag as the begin of this element
   *
   * @return   this element start tag
   */
  public JspTag getStartTag() {
    return this.startTag;
  }

  /**
   * Sets JspTag as the begin of this element
   *
   * @param   startTag  start tag to set for this element
   */
  public void setStartTag(JspTag startTag) {
    this.startTag = startTag;
  }

  /**
   * Element containing all nested bodey elements
   *
   * @return   this element body
   */
  public Element getBody() {
    return this.body;
  }

  /**
   * Sets the Element containing all nested bodey elements
   *
   * @param   body  body to set
   */
  public void setBody(Element body) {
    this.body = body;
  }

  /**
   * Setter for element's type
   *
   * @param   type  type to set
   */
  public void setType(int type) {
    this.elementType = type;
  }

  /**
   * Predefines the toString() method.
   *
   * @return   string representation of this object
   */
  public String toString() {
    String ret = "\n{ ";
    ret += "\n} ";
    return ret;
  }

  /**
   * Verifies this jsp element
   *
   * @param   type  type of the element to check
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  public void verify(int type) throws JspParseException {

    if (body == null) {
      return;
    }

    switch (type) {
      case ROOT: {
        startTag.verifyAttributes();
        break;
      }
      case FALLBACK: {
        startTag.verifyAttributes();
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }
            if (doBody(all[i])) {
              continue;
            }  
            throw new JspParseException(JspParseException.NEITHER_SCRIPTING_NOR_SUBELEMENTS_ARE_ALOWED,new Object[]{"fallback"}, 
                parser.currentFileName(), debugInfo.start);
          }
        }
        break;
      }
      case USE_BEAN: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
//            if (checkWhiteSpacesElement(all[i])) {
//              continue;
//            }
            if (doAttribute(all[i])) {
              continue;
            }
//            if (hasBody(all[i])) {
//              continue;
//            }
//            if (hasSetProperty(all[i])) {
//              continue;
//            }
//            if (hasGetProperty(all[i])) {
//              continue;
//            }
//            if (hasTextTag(all[i])) {
//              continue;
//            }
//
//            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "useBean"}, parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case FORWARD: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }

            if (doParam(all[i])) {
              continue;
            }

            if (doAttribute(all[i])) {
              continue;
            }

            if (hasBody(all[i])) {
              continue;
            }
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "forward"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case GET_PROPERTY: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }

            if (doParam(all[i])) {
              continue;
            }

            if (doAttribute(all[i])) {
              continue;
            }

            if (hasBody(all[i])) {
              continue;
            }

            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "getProperty"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case SET_PROPERTY: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }

            if (doAttribute(all[i])) {
              continue;
            }

            if (hasBody(all[i])) {
              continue;
            }

            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "setProperty"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case INCLUDE: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }

            if (doParam(all[i])) {
              continue;
            }

            if (doAttribute(all[i])) {
              continue;
            }

            if (hasBody(all[i])) {
              continue;
            }

            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "include"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case PARAM: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (checkWhiteSpacesElement(all[i])) {
              continue;
            }
            if (doAttribute(all[i])) {
              continue;
            }

            if (hasBody(all[i])) {
              continue;
            }
            throw new JspParseException(JspParseException.ELEMENT_HAS_NONE_EMPTY_BODY, new Object[]{_default_prefix_tag_start + "param"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case PARAMS: {

        Element[] all = body.getElements();
        if (all == null) {
          /*
          If the jsp:params element is nested within the jsp:plugin element,
          there must be at least one jsp:param element or a translation-time
          error will occur.
          */
          throw new JspParseException(JspParseException.ELEMENT_NOT_FOUND, new Object[]{"jsp:param"}, parser.currentFileName(), debugInfo.start);
        }
        boolean hasParam = false;
        for (int i = 0; i < all.length; i++) {
          if (checkWhiteSpacesElement(all[i])) {
            continue;
          }

          if (doParam(all[i])) {
            hasParam = true;
            continue;
          }

          if ((all[i] instanceof JspElement) && doBody(all[i])) {
            if( checkBodyForParams((JspElement)all[i]) ){
              hasParam = true;
            }
            continue;
          }

          throw new JspParseException(JspParseException.ELEMENT_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{"params"},
                  parser.currentFileName(), debugInfo.start);
        }
        if( !hasParam ){
          /*
          If the jsp:params element is nested within the jsp:plugin element,
          there must be at least one jsp:param element or a translation-time
          error will occur.
          */
          throw new JspParseException(JspParseException.ELEMENT_NOT_FOUND, new Object[]{"jsp:param"}, parser.currentFileName(), debugInfo.start);
        }
        startTag.verifyAttributes();
        break;
      }
      case PLUGIN: {

        Element[] all = body.getElements();
        if (all == null) {
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (checkWhiteSpacesElement(all[i])) {
            continue;
          }

          if (doParams(all[i])) {
            continue;
          }

          if (doFallback(all[i])) {
            continue;
          }

          if (doAttribute(all[i])) {
            continue;
          }

          if (hasBody(all[i])) {
            Element[] bodyEl = ((JspElement)all[i]).body.getElements();
            if (bodyEl != null) {
              for (int j = 0; j < bodyEl.length; j++) {
                if (doFallback(bodyEl[j])) {
                  continue;
                }
                if (doParams(bodyEl[j])) {
                  continue;
                }
              }
            }
            continue;
          }
          throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "plugin"},
                  parser.currentFileName(), debugInfo.start);
        }
        startTag.verifyAttributes();
        break;
      }
      case CUSTOM: {
        Element[] all = body.getElements();
        if (all != null) {
          for (int i = 0; i < all.length; i++) {
            if (doAttribute(all[i])) {
              continue;
            }
            if (hasBody(all[i])) {
              continue;
            }
          }

        }
        startTag.verifyAttributes();
        break;
      }
      case EXPRESSION: {
        startTag.verifyAttributes();
        break;
      }
      case ELEMENT: {
        startTag.verifyAttributes();
        Element[] all = body.getElements();
        if (all == null) {
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (checkWhiteSpacesElement(all[i])) {
            continue;
          }

          if (hasAttribute(all[i])) {
            startTag.hasJspAttribute = true;
            continue;
          }

          if (hasBody(all[i])) {
            continue;
          }
          throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "element"},
                  parser.currentFileName(), debugInfo.start);
        }
        break;
      }
      case BODY: { //samo templates
        startTag.verifyAttributes();
        Element[] all = body.getElements();
        if (all == null) {
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (checkWhiteSpacesElement(all[i])) {
            continue;
          }
          if (all[i] instanceof AttributeTag || all[i] instanceof BodyActionTag) {
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "body"},
                    parser.currentFileName(), debugInfo.start);
          }
          if (all[i] instanceof JspElement && (((JspElement)all[i]).type == BODY || ((JspElement)all[i]).type == ATTRIBUTE)){
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "body"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        break;
      }
      case ATTRIBUTE: {
        startTag.verifyAttributes();
        Element[] all = body.getElements();
        if (all == null) {
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (checkWhiteSpacesElement(all[i])) {
            continue;
          }
          if (all[i] instanceof AttributeTag || all[i] instanceof BodyActionTag) {
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "attribute"},
                    parser.currentFileName(), debugInfo.start);
          }
          if (all[i] instanceof JspElement && (((JspElement)all[i]).type == BODY || ((JspElement)all[i]).type == ATTRIBUTE)){
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "attribute"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        break;
      }

      case INVOKE: {
        Element[] all = body.getElements();
        if (all == null) {
          if (body instanceof TemplateData && ((TemplateData)body).getValue().trim().length() > 0) {
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "invoke"},
                    parser.currentFileName(), debugInfo.start);
          }
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (all[i] instanceof TemplateData) {
            if (((TemplateData)all[i]).getValue().trim().length() > 0) {
              throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "invoke"},
                      parser.currentFileName(), debugInfo.start);
            }
            continue;
          }
          if (doAttribute(all[i])) {
            continue;
          }
          if (all[i] instanceof BodyActionTag) {
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "invoke"},
                    parser.currentFileName(), debugInfo.start);
          }
        }
        startTag.verifyAttributes();
        break;
      }
      case DO_BODY:{
        Element[] all = body.getElements();
        if (all == null) {
          if (body instanceof TemplateData && ((TemplateData)body).getValue().trim().length() > 0) {
            throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "doBody"},
                    parser.currentFileName(), debugInfo.start);
          }
          break;
        }
        for (int i = 0; i < all.length; i++) {
          if (all[i] instanceof TemplateData) {
            if (((TemplateData)all[i]).getValue().trim().length() > 0) {
              throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "doBody"},
                      parser.currentFileName(), debugInfo.start);
            }
            continue;
          }
          if (doAttribute(all[i])) {
            continue;
          }
          throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "doBody"},
                  parser.currentFileName(), debugInfo.start);
        }
        startTag.verifyAttributes();
        break;
      }
      case DIRECTIVE_INCLUDE: 
      case DIRECTIVE_ATTRIBUTE:
      case DIRECTIVE_PAGE:
      case DIRECTIVE_TAG:
      case DIRECTIVE_VARIABLE:
      {
        checkForDirectiveEmptyBody(startTag._name);
        startTag.verifyAttributes();
        break;
      }
      default:
    }
  }
  
  /**
   * This method will check if the body of this element is empty as per
   * XMLDirectiveBody production in JSP EBNF. XML directtive body can contain no elements or only
   * body with whitespaces: 
   * XMLDirectiveBody ::= XMLJSPDirectiveBody | XMLTagDefDirectiveBody 
   * [ vc: TagFileSpecificXMLDirectives ]
   * 
   * XMLTagDefDirectiveBody::= ( ( 'tag'; S TagDirectiveAttrList S? ( '/>' | ( '>' S? ETag ) ) ) 
   * | ( 'include' S IncludeDirectiveAttrList S?      ( '/>' | ( '>' S? ETag ) ) ) 
   * | ( 'attribute' S AttributeDirectiveAttrList S?  ( '/>' | ( '>' S? ETag ) ) ) 
   * | ( 'variable' S VariableDirectiveAttrList S?    ( '/>' | ( '>' S? ETag ) ) ) ) 
   * | <TRANSLATION_ERROR>
   * 
   * XMLJSPDirectiveBody::= S? 
   * ( ( 'page' S PageDirectiveAttrList S? ( '/>' | ( '>' S? ETag ) ) )
   * | ( 'include' S IncludeDirectiveAttrList S? ( '/>' | ( '>' S? ETag ) ) ) ) 
   * | <TRANSLATION_ERROR> 
   * @param elementName
   * @throws JspParseException
   */
  private void checkForDirectiveEmptyBody(String elementName) throws JspParseException {
    if (body instanceof TemplateData ) {
      if( ((TemplateData) body).getValue().trim().length() > 0) {
        throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[] { _default_prefix_tag_start + elementName }, parser.currentFileName(), debugInfo.start);  
      }
      return; // the body is template data either empty or with whitespaces
    }
    boolean emptyBody = true;
    for (int i = 0; i < body.getElements().length; i++) {
      if (body.getElements()[i] instanceof TemplateData) {
        if (((TemplateData) body.getElements()[i]).getValue().trim().length() > 0) {
          emptyBody = false;
          break;
        }
      }
      emptyBody = false;
      break;
    }
    if (!emptyBody) {
      throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[] { _default_prefix_tag_start + elementName }, parser.currentFileName(), debugInfo.start);
    }

  }

  /**
   * Takes specific action coresponding to custom tags actions
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  private void customTagAction(StringBuffer buffer) throws JspParseException {
    CustomJspTag tag = (CustomJspTag) startTag;
    TagBeginGenerator beginTag = null;
    boolean hasBody = checkForAttributesWithoutBody();
    boolean hasRTExpressionAttribute = false;
    if (tag.attributes != null) {
      Attribute[] attr = tag.attributes;
      for (int i = 0; i < attr.length; i++) {
        if (attr[i].isNamed) {
          hasRTExpressionAttribute = true;
          if (isFragment(tag.tagInfo.getAttributes(), attr[i].name.toString())) {
            if (hasJspElementInBody(this, JspElement.SCRIPT)) {
              throw new JspParseException(JspParseException.SCRIPTING_ELEMENTS_ARE_NOT_ALLOWED_IN_JSPATTRIBUTE,
                      parser.currentFileName(), parser.currentDebugPos());
            }
          }
        } else if (!hasRTExpressionAttribute && isRuntimeExpr(attr[i].value.toString())) {
          hasRTExpressionAttribute = true;
        }
      }
    }
    if (tag.tagInfo != null) {
      if (hasBody && tag.tagInfo.getBodyContent() != null && tag.tagInfo.getBodyContent().equals(TagInfo.BODY_CONTENT_EMPTY)) {
        throw new JspParseException(JspParseException.TAG_REQUIRE_EMPTY_BODY,  new Object[]{tag.prefix + ":" + tag.shortName},
                parser.currentFileName(), parser.currentDebugPos());
      }
    }
    //check body for scripting elements
    //if found cannot be created separated method for this tag
    if (!hasRTExpressionAttribute && hasBody) {
      hasRTExpressionAttribute = checkForScriptingElements(body.elements);
    }
    StringBuffer tmpBuffer = null;
    boolean isScriptless = false;
    String parent = null;

    try {
      beginTag = new TagBeginGenerator(tag.prefix, tag.shortName, tag.attributes, tag.tagInfo, parser);
      beginTag.init(parser.getApplicationClassLoader());
      isScriptless = (tag.tagInfo.getVariableInfo(beginTag.getTagData()) == null || tag.tagInfo.getVariableInfo(beginTag.getTagData()).length == 0) &&
                                (tag.tagInfo.getTagVariableInfos().length == 0) &&
                                !hasRTExpressionAttribute;
      boolean isParentSimple = false;
      if (beginTag.topTag() != null) {
        if (beginTag.topTag().isSimpleTag) {
          isParentSimple = true;
          if (!beginTag.isSimpleTag()) {
            parent = "new javax.servlet.jsp.tagext.TagAdapter((javax.servlet.jsp.tagext.SimpleTag) _jspx_parent)";
          } else {
            parent = "_jspx_parent";
          }
        } else {
          parent = beginTag.topTag().tagHandlerInstanceName;
        }
      }

      if (isScriptless) {
        String methodName = "_jspx_method_" + beginTag.getBaseVarName();
        tmpBuffer = new StringBuffer();

        buffer.append(beginTag.getIdent() + "if (" + methodName + "(_jspx_pageContext, " + parent + ")) {\n");
        if (tag.isInMethod() && !isParentSimple){
            buffer.append(beginTag.getIdent() + "\treturn true;\r\n"+ beginTag.getIdent() + "}\r\n");
        } else if (parser.getJspBodyVariable() != null) {
          buffer.append(beginTag.getIdent() + "\r\n\tString result_" + parser.getJspBodyVariable() + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();");
          buffer.append(beginTag.getIdent() + "\r\n\tout = _jspx_pageContext.popBody();");
          buffer.append(beginTag.getIdent() + "\r\n\treturn result_" + parser.getJspBodyVariable() + ";");
          buffer.append(beginTag.getIdent() + "\r\n}\r\n");

        } else {
          buffer.append(beginTag.getIdent() + "\treturn;\r\n" + beginTag.getIdent() + "}\r\n");
        }

        tmpBuffer.append("\t// CustomJspTag.action() - start").append("\n");

        // Declaration of the Tag method, goes into the parser.getTagFragmentMethodsCode() vector
        String parentClass = beginTag.isSimpleTag() ? "javax.servlet.jsp.tagext.JspTag" : "javax.servlet.jsp.tagext.Tag";
        tmpBuffer.append("\tprivate boolean " + methodName + "(com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext, " +
          parentClass + " _jspx_parent) throws Throwable {\r\n");
        tmpBuffer.append("\t\tJspWriter out = _jspx_pageContext.getOut();\r\n");
        beginTag.generate(tmpBuffer, parser.getTagFragmentMethodsCode(), true, hasBody, false);
      } else {
        beginTag.generate(buffer, parser.getTagFragmentMethodsCode(), false, hasBody, false);
      }
      debugInfo.writeEnd(buffer);
      parser.setFragmentsCount(beginTag.getFragmentsCount());
    } catch (JspParseException e) {
      throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{tag.shortName}, e);
    } catch (NoClassDefFoundError t) {
      throw new JspParseException(JspParseException.CANNOT_PARSE_CUSTOM_TAG, new Object[]{tag.shortName}, t);
    }


    StringBuffer fragmentBody = null;
    if (hasBody) {
      if (isScriptless) {    // || tag.isInMethod()
        setScriptlessBody(body.elements, true);
      }
      // sets parent tag
      body.parentCustomTag = beginTag.topTag()== null ? null : beginTag.topTag().tagHandlerInstanceName;
      if (beginTag.isSimpleTag()) {
        fragmentBody = (StringBuffer)parser.getTagFragmentMethodsCode().lastElement();
      }
      if (tag.hasJspAttribute) {
        Element[] el = body.elements;
        for (int i = 0; i < el.length; i++) {
          if (el[i] instanceof AttributeTag) {
            continue;
          }
          el[i].action(fragmentBody == null ? (isScriptless ? tmpBuffer : buffer) : fragmentBody);
        }
      } else {
        body.action(fragmentBody == null ? (isScriptless ? tmpBuffer : buffer) : fragmentBody);
      }
    }

    TagEndGenerator endTag = new TagEndGenerator(tag.prefix, tag.shortName, tag.attributes, tag.tagInfo, parser);
    if (isScriptless) {
      endTag.generate(tmpBuffer, fragmentBody, null, isScriptless, hasBody, false, parser.isTagFile());
      parser.getTagMethodsCode().add(tmpBuffer);
    } else {
      endTag.generate(buffer, fragmentBody, null, tag.isInMethod(), hasBody, false, parser.isTagFile());
    }
  }

  /**
   * Takes specific action coresponding to plugin action
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  private void pluginAction(StringBuffer buffer) throws JspParseException {
    Hashtable attrs = new Hashtable();
    Hashtable param = new Hashtable();
    String value;
    String key;

    for (int i = 0; i < PluginTag.attributeNames.length; i++) {
      key = String.copyValueOf(PluginTag.attributeNames[i]);
      value = startTag.getAttributeValue(key);

      if (value != null) {
        attrs.put(key, value);
      }
    }

    param.putAll(params);
    try {
      PluginGenerator plugin = new PluginGenerator(attrs, param, fallback, this);
      plugin.init(null);
      plugin.generate(buffer);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
//      throw new ParseException(Error_parsing_plugin + parser.getException(ex));
      throw new JspParseException(JspParseException.ERROR_PARSING_PLUGIN, e);
    }
  }

  /**
   * Takes specific action coresponding to forward action
   *
   * verification
   */
  private void forwardAction(StringBuffer buffer) throws JspParseException {
    boolean hasParam = (params.size() != 0);
    String page = startTag.getAttributeValue("page");
    parser.getScriptletsCode().append("\t\t\tif (true) {").append("\n");
    boolean isExpression = isRuntimeExpr(page.trim());

    if (isExpression) {
      page = evaluateExpression(page);
    }

    if (hasParam) {
      if (!isExpression) {
        page = "\"" + page + "\"";
      }

      page = page + "+" + "\"" + ((page.indexOf("?") > 0) ? "" : "?");
      Iterator i = params.entrySet().iterator();
      Map.Entry em;
      String key;
      String value;

      for (; i.hasNext();) {
        em = (Map.Entry) i.next();
        key = (String) em.getKey();
        value = (String) em.getValue();

        if (isRuntimeExpr(value.trim())) {
          value = evaluateExpression(value);
        } else {
          value = "\"" + value + "\"";
        }

        if ('?' == page.charAt(page.length() - 1)) {
          page = page + key + "=\"" + "+" + value;
        } else {
          page = page + "+" + "\"" + "&" + key + "=\"" + "+" + value;
        }
      }

     buffer.append("\t\t\t\t_jspx_pageContext.forward(").append(page).append(");").append("\r\n");
    } else {
      if (isExpression) {
        buffer.append("\t\t\t\t_jspx_pageContext.forward(").append(page).append(");").append("\n");
      } else {
        buffer.append("\t\t\t\t_jspx_pageContext.forward(\"").append(page).append("\");").append("\n");
      }
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
  }

  /**
   * Takes specific action coresponding to useBean action
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  private void useBeanAction(StringBuffer buffer) throws JspParseException {
    String id = startTag.getAttributeValue("id");
    String typeName = startTag.getAttributeValue("type");
    String className = startTag.getAttributeValue("class");
    String beanName = startTag.getAttributeValue("beanName");
    boolean isExpression = false;
    if (beanName != null) {
      isExpression = isRuntimeExpr(beanName);
      if (isExpression) {
        beanName = evaluateExpression(beanName);
      }
    }
    String scope = startTag.getAttributeValue("scope");
    String tempId = (String) parser.getBeanId().get(id);

    if (tempId == null) {
      parser.getBeanId().put(id, id);
    } else {
      throw new JspParseException(JspParseException.JSP_USEBEAN_DUPLICATED_ID_ATTRIBUTE);
    }

    if (typeName == null) {
      typeName = className;
    }

    if (typeName == null) {
      typeName = beanName;
    }

    if (typeName == null) {
      throw new JspParseException(JspParseException.JSP_USEBEAN_NEEDS_A_TYPE_ATTRIBUTE);
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
    parser.setInner(true);
    body.action();
    parser.setInner(false);
    buffer.append("\t\t}").append("\r\n");
  }

  /**
   * Takes specific action coresponding to getProperty action
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  private void getPropertyAction(StringBuffer buffer) throws JspParseException {
    String property = startTag.getAttributeValue("property");
    String name = startTag.getAttributeValue("name");
    Class beanClass;
    String beanClassName = "";
    try {
      beanClassName = (String) parser.getAllBeanNames().get(name);
      beanClass = parser.getApplicationClassLoader().loadClass(beanClassName);
    } catch (Exception cnfex) {
      buffer.append("\t\t\tout.print(com.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.getProp(_jspx_pageContext.findAttribute(\"").append(name).append("\"), \"").append(property).append("\"));\r\n");
      return;
    }
    try {
      Method m = beanClass.getDeclaredMethod("is" + parser.firstCap(property), new Class[0]);

      if (m.getReturnType().getName().equals("boolean")) {
        buffer.append("\t\t\tout.print(").append(name).append(".is").append(parser.firstCap(property)).append("());\r\n");
      } else {
        throw new JspParseException(JspParseException.METHOD_MUST_RETURN_BOOLEAN, new Object[]{"is" + parser.firstCap(property)});
      }
    } catch (JspParseException ex) {
      throw ex;
    } catch (Exception nsfex) {
      buffer.append("\t\t\tout.print(").append(name).append(".get").append(parser.firstCap(property)).append("());\r\n");
    }
  }

  /**
   * Takes specific action coresponding to setProperty action
   *
   * @exception   JspParseException  thrown if error occures during
   * verification
   */
  private void setPropertyAction(StringBuffer buffer) throws JspParseException {
    String property = startTag.getAttributeValue("property");
    String name = startTag.getAttributeValue("name");

    if (property.equalsIgnoreCase("*")) {
      if (parser.isInner()) {
        debugInfo.writeStart(buffer, false);
        buffer.append("\t\t\ttry{\r\n");
        buffer.append("\t\t\t\tcom.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.introspect(_jspx_pageContext.findAttribute(\"").append(name).append("\"),request);\r\n");
        buffer.append("\t\t\t} catch (OutOfMemoryError o) {\r\n");
        buffer.append("\t\t\t\tthrow o;\r\n");
        buffer.append("\t\t\t} catch (ThreadDeath tde) { throw tde;\r\n");
        buffer.append("\t\t\t} catch (Throwable t) {\r\n");
        buffer.append("\t\t\t\ttry {\r\n");
        buffer.append("\t\t\t\t\tout.clear();\r\n");
        buffer.append("\t\t\t\t} catch(java.io.IOException _jspioex) {}\r\n");
        buffer.append("\t\t\t\trequest.setAttribute(\"javax.servlet.jsp.jspException\",t );\r\n");
        buffer.append("\t\t\t\tthrow new ServletException(\"ID018218: Unknown exception: \"+t.toString());\r\n");
        buffer.append("\t\t\t}\r\n");
      } else {
        debugInfo.writeStart(buffer, false);
        buffer.append("\t\t\t\tcom.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.introspect(_jspx_pageContext.findAttribute(\"").append(name).append("\"),request);\r\n");
      }
    } else {
      String param = startTag.getAttributeValue("param");
      String value;

      if (!(param == null || param.equals("") || param.equalsIgnoreCase("null"))) {

      } else {
        param = property;
      }

      value = startTag.getAttributeValue("value");
      StringBuffer temp = new StringBuffer();

      if (param != property) {
        temp.append("\t\t\t\t");
        temp.append("com.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.introspecthelper(_jspx_pageContext." +
          "findAttribute(\"" + name + "\"), \"" + property +
          "\", request.getParameterValues(\"" + param + "\"));");
        temp.append("\r\n");
      } else if (value != null) {
        if (!isRuntimeExpr(value) && !startTag.getAttribute("value").isNamed) {
          buffer.append("\t\t\tcom.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.setProp(").append(name).append(", \"").append(param).append("\", \"").append(value).append("\");\r\n");
        } else {
          if (value.startsWith("${")) {
            String funcMapper = FunctionMapperImpl.getFunctionMapper(value, parser);
            if (funcMapper == null) {
              funcMapper = "null";
            }
            buffer.append("\t\t\tcom.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.setELProp(").append(name).append(", \"").append(param).append("\", \"").append(value).append("\", _jspx_pageContext, ").append(funcMapper).append(");\r\n");
          } else {
            temp.append("\t\t").append(name).append(".set").append(parser.firstCap(property)).append("(");
            temp.append(evaluateExpression(value));
            temp.append(");\r\n");
          }
        }
      } else {
        temp.append("\t\t\t\t");
        temp.append("com.sap.engine.services.servlets_jsp.lib.jspruntime.RunLibrary.introspecthelper(_jspx_pageContext." +
          "findAttribute(\"" + name + "\"), \"" + property +
          "\", request.getParameterValues(\"" + param + "\"));");
        temp.append("\r\n");
      }

      if (parser.isInner()) {
        buffer.append("\t\t\ttry{\r\n");
        buffer.append(temp);
        buffer.append("\t\t\t} catch (ThreadDeath tde) { throw tde;\r\n");
        buffer.append("\t\t\t} catch (OutOfMemoryError o) {\r\n");
        buffer.append("\t\t\t\tthrow o;\r\n");
        buffer.append("\t\t\t} catch (Throwable t) {\r\n");
        buffer.append("\t\t\t\ttry {\r\n");
        buffer.append("\t\t\t\t\tout.clear();\r\n");
        buffer.append("\t\t\t\t} catch(java.io.IOException _jspioex) {}\r\n");
        buffer.append("\t\t\t\trequest.setAttribute(\"javax.servlet.jsp.jspException\",t );\r\n");
        buffer.append("\t\t\t\tthrow new ServletException(\"ID018220: Unknown exception: \"+t);\r\n");
        buffer.append("\t\t\t}\r\n");
        debugInfo.writeEnd(buffer);
      } else {
        buffer.append(temp);
        debugInfo.writeEnd(buffer);
      }
    }
  }

  /**
   * Takes specific action coresponding to include action
   * @param buffer - the StringBuffer to be used for generated code
   */
  private void includeAction(StringBuffer buffer) throws JspParseException {
    boolean hasParam = (params.size() != 0);
    String page = startTag.getAttributeValue("page");
    String flush = startTag.getAttributeValue("flush");
    boolean isExpression = isRuntimeExpr(page.trim());
    if (flush == null) {
      flush = "false";
    }
    if (isExpression) {
      page = evaluateExpression(page);
    }
    if (hasParam) {
      if (!isExpression) {
        page = "\"" + page + "\"";
      }

      page = page + "+" + "\"" + ((page.indexOf("?") > 0) ? "" : "?");
      Iterator i = params.entrySet().iterator();
      Map.Entry em;
      String key;
      String value;

      for (; i.hasNext();) {
        em = (Map.Entry) i.next();
        key = (String) em.getKey();
        value = (String) em.getValue();

        if (isRuntimeExpr(value.trim())) {
          value = evaluateExpression(value);
        } else {
          value = "\"" + value + "\"";
        }
        if ('?' == page.charAt(page.length() - 1)) {
          page = page + key + "=\"" + "+" + value;
        } else {
          page = page + "+" + "\"" + "&" + key + "=\"" + "+" + value;
        }
      }

      buffer.append("\t\t\t_jspx_pageContext.include(").append(page).append(",").append(flush).append(");").append("\r\n");
    } else {
      if (isExpression) {
        buffer.append("\t\t\t_jspx_pageContext.include(").append(page).append(",").append(flush).append(");").append("\n");
      } else {
        buffer.append("\t\t\t_jspx_pageContext.include(\"").append(page).append("\",").append(flush).append(");").append("\n");
      }
    }
  }

  /**
   * Verifies specified jsp element and stores all its params
   *
   * @param   el  element to verify
   * @return  true if is verified
   * @exception   JspParseException thrown if verification failed
   */
  private boolean doParams(Element el) throws JspParseException {

    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == PARAMS) {
        jspelement.verify(PARAMS);
        params.putAll(jspelement.params);
      } else {
        return false;
      }

      return true;
    }

    return false;
  }

  private boolean doParam(Element el) throws JspParseException {
    JspTag jsptag = null;
    if (el instanceof ParamTag) {
      jsptag = (JspTag) el;
    } else if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;
      if (jspelement.type == PARAM) {
        jsptag = jspelement.startTag;
      }
    }
    
    if( jsptag != null ) {
      String name = jsptag.getAttributeValue("name");
      if (isRuntimeExpr(name)) {
        throw new JspParseException(JspParseException.ATTRIBUTE_CANNOT_ACCEPT_RUNTIME_EXPRESSIONS, new Object[] {"name", "param"});
      }
      String value = jsptag.getAttributeValue("value");
      params.put(name, parser.replaceAttributeQuotes(value));
      return true;
    }

    return false; 
  }

  /**
   * Verifies specified jsp element
   *
   * @param   el  element to verify
   * @return  true if is verified
   * @exception   JspParseException thrown if verification failed
   */
  private boolean doFallback(Element el) throws JspParseException {
    if (el instanceof FallbackTag) {
      fallback = "";
      return true;
    }

    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == FALLBACK) {
        Element e = jspelement.body;
        TemplateData td;

        if (e.elements == null) {
          td = (TemplateData) e;
        } else if (e.elements.length == 1) {
          td = (TemplateData) e.elements[0];
        } else {
          throw new JspParseException(JspParseException.UNEXPECTED_ELEMENT_IN_THE_BODY_OF_ELEMENT, new Object[]{"fallback"},
                  parser.currentFileName(), debugInfo.start);
        }
        if (td.getValue() == null) {
          fallback = parser.getChars(td.startIndex, td.endIndex);
        } else {
          fallback = td.getValue();
        }
        fallback = parser.replaceTemplateTextQuotes(fallback);
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  /**
   * Verifies specified jsp element
   *
   * @param   el  element to verify
   * @return  true if is verified
   */
  private boolean checkWhiteSpacesElement(Element el) {
    return (el instanceof TemplateData) || (el instanceof OutputCommentElement) || (el instanceof JspCommentElement);
  }

  /**
   * Returns the String for XML view
   * @param id IDCounter to be used
   * @return String for XML view
   * @throws JspParseException if an error occured
   */
  public String getString(IDCounter id) throws JspParseException {
    StringBuilder ret = new StringBuilder();
    // get string only from the main RootTag - PageDataImpl.doXmlStyle/doJspStyle
    // attributes are already merged
    if( !(startTag instanceof RootTag) ){
      ret.append(startTag.getString(id));
    }


    if (body instanceof TemplateData && 
          (startTag instanceof ScriptletTag || 
           startTag instanceof ExpressionTag ||
           startTag instanceof DeclarationTag ||
           startTag instanceof TextTag)) {
        ret.append("\n").append(((TemplateData)body).getEscapedOnlyString());
      }else {
        String bodyString = body.getString(id);
        ret.append("\n").append(bodyString);  
      }      

    if( !(startTag instanceof RootTag) ){
      ret.append(endTag.getString(id));
    }
    return ret.toString();
  }

  private void elementTagAction(StringBuffer outBuffer) throws JspParseException {
    String name = startTag.getAttributeValue("name");
    boolean isExpression = isRuntimeExpr(name);
    StringBuffer buffer = new StringBuffer();
    StringBuffer bufferAttrBodyCode = new StringBuffer();
    if (isExpression) {
      name = evaluateExpression(name);
      buffer.append("\t\t\tout.print(\"<\" + ").append(name).append(" + \"");
    } else {
      buffer.append("\t\t\tout.print(\"<").append(name.trim());
    }
    Attribute[] attr = startTag.attributes;
    for (int i = 0; i < attr.length; i++) {
      if ("name".equals(attr[i].name.toString())){
        continue;
      }
//      if (isExpression) {
//        buffer.append(" + \" \" + \""+attr[i].name.toString()+"=\\\""+attr[i].value.toString()+"\\\"");
//      } else {
        buffer.append(" "+attr[i].name.toString()+"=\\\""+attr[i].value.toString()+"\\\"");
//      }
    }
    if (body != null){
      if (body instanceof TemplateData) {
        if (isExpression) {
//          buffer.append(" + \">"+ ((TemplateData)body).getValue()+"</" + name + ">\");\r\n");
            buffer.append(">"+ StringUtils.escapeJavaCharacters(((TemplateData)body).getValue())+"</\" + "+name+" + \">\");\r\n");
        } else {
          buffer.append(">"+ StringUtils.escapeJavaCharacters(((TemplateData)body).getValue())+"</"+name+">\");\r\n");
        }
      } else if (body instanceof ElementCollection) {
        String attrFromBody = null;
        String attrFromBodyVar = null;
        String bodyTagValue = null;
        String bodyTagValueVar = null;
        Element[] el = body.elements;
        checkForAttributesWithoutBody();
        for (int i = 0; i < el.length; i++) {
          if (checkWhiteSpacesElement(el[i])) {
            continue;
          }
          if (el[i] instanceof AttributeTag) {
            AttributeTag at = (AttributeTag)el[i];
            if (attrFromBody == null) {
              attrFromBody = "";
            }
//            if (isExpression) {
//              attrFromBody +=" + \" \" + \""+ at.getAttributeValue("name") + "=\\\"\\\"\"";
//            } else {
              attrFromBody +=" "+ at.getAttributeValue("name") + "=\\\"\\\"";
//            }
            continue;
          }
          if (el[i] instanceof JspElement) {
            JspElement jspelement = (JspElement) el[i];
            if (jspelement.type == ATTRIBUTE) {
              JspTag jsptag = jspelement.startTag;
              if (attrFromBody == null) {
                attrFromBody = "";
              }

              String attrName = jsptag.getAttributeValue("name");
              String trim = jsptag.getAttributeValue("trim");
              String value = "";
              if (jspelement.body != null) {
                boolean toTrim = true; //default value
                if (trim != null && "false".equals(trim)) {
                  toTrim = false;
                }
                attrFromBodyVar = evaluateJspAttributeSimple(jspelement.body, toTrim);
                value = "\\\"\" + " + attrFromBodyVar + " + \"\\\"";
                String jspAttrCode = (String) parser.getJspAttributesCode().get(attrFromBodyVar);
                if (jspAttrCode != null) {
                  bufferAttrBodyCode.append(jspAttrCode);
                  parser.getJspAttributesCode().remove(attrFromBodyVar);
                }
              }
               attrFromBody +=" "+ attrName + "=" + value;
            }
            //body
            if (jspelement.type == BODY) {
              bodyTagValueVar = evaluateJspBodySimple(jspelement.body);
              bodyTagValue = "\" + " + bodyTagValueVar + " + \"";
            }
          }
        }
        if (attrFromBody != null){
          buffer.append(attrFromBody);
          startTag.hasJspAttribute = true;
        }
        if (bodyTagValue == null) {
          bodyTagValue = "";
          startTag.hasJspBody = true;
        } else {
          String jspBodyCode = (String) parser.getJspBodyCode().get(bodyTagValueVar);
          if (jspBodyCode != null) {
            bufferAttrBodyCode.append(jspBodyCode);
            parser.getJspBodyCode().remove(bodyTagValueVar);
          }
        }

        if (isExpression) {
          buffer.append(">" + bodyTagValue + "</\" + " + name + "+ \">\");\r\n");
        } else {
          buffer.append(">" + bodyTagValue + "</" + name + ">\");\r\n");
        }
      } else {
        throw new JspParseException(JspParseException.ACTION_DOES_NOT_HAVE_CORRECT_BODY, new Object[]{_default_prefix_tag_start + "element"},
                parser.currentFileName(), debugInfo.start);
      }

    } else {
      if (isExpression) {
        buffer.append(" + \"/>\");\r\n");
      } else {
        buffer.append("/>\");\r\n");
      }
    }
    outBuffer.append(bufferAttrBodyCode);
    outBuffer.append(buffer);
  }

  private boolean hasAttribute(Element el) {
    if (el instanceof AttributeTag) {
      return true;
    }

    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == ATTRIBUTE) {
        return true;
      }
    }
    return false;
  }

  private boolean hasBody(Element el) {
    if (el instanceof BodyActionTag) {
      startTag.hasJspBody = true;
      return true;
    }

    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == BODY) {
        startTag.hasJspBody = true;
        return true;
      }
    }
    return false;
  }

  private boolean doBody(Element el) {
    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == BODY) {
        if (jspelement.body != null) {
          body = jspelement.body;
        }
        startTag.hasJspBody = true;
        return true;
      }
    }
    return false;
  }
  
  /**
   * Processes <jsp:attribute> action
   * @param el
   * @return
   * @throws JspParseException
   */
  private boolean doAttribute(Element el) throws JspParseException {
    if (el instanceof AttributeTag) {
      JspTag jsptag = (JspTag) el;
      String name = jsptag.getAttributeValue("name");
      Attribute[] attr = null;
      if (startTag.attributes == null) {
        attr = new Attribute[1];
        attr[0] = new Attribute(new Indentifier(name), new Indentifier(""), false);
      } else {
        attr = new Attribute[startTag.attributes.length + 1];
        for (int i = 0; i < startTag.attributes.length; i++) {
         attr[i] = startTag.attributes[i];
        }
        attr[attr.length - 1] = new Attribute(new Indentifier(name), new Indentifier(""), false);
      }
      startTag.setAttributes(attr);
      startTag.hasJspAttribute = true;
      return true;
    }

    if (el instanceof JspElement) {
      JspElement jspelement = (JspElement) el;

      if (jspelement.type == ATTRIBUTE) {
        JspTag jsptag = jspelement.startTag;
        String name = jsptag.getAttributeValue("name");
        String trim = jsptag.getAttributeValue("trim"); //default true
        String value = "";
        boolean named = false;
        if (jspelement.body != null) {

          boolean toTrim = true; //default value
          if (trim != null && "false".equals(trim)) {
            toTrim = false;
          }
          if (jspelement.body instanceof TemplateData) {
            value = ((TemplateData)jspelement.body).getValue();
            boolean foundEL = false;
            List<String> innerExpressions = InnerExpression.getInnerExpressions(value);
            for (int i=0; i < innerExpressions.size(); i++) {
              String iex = (String)innerExpressions.get(i);
              if (innerExpressions.size() == 1 && !iex.startsWith("${") ) {
                break;
              }
              if (iex.startsWith("${")) {
                foundEL = true;
              }
            }
            if (foundEL) {
              //value = evaluateJspAttributeExtended(jspelement.body, toTrim, parser.getScriptletsCode(), this.parentCustomTag);
              value = evaluateJspAttributeSimple(jspelement.body, toTrim);
              named = true;
            } else {
              if (toTrim) {
                value = value.trim();
              }
              named = false;
            }
          } else {
            //value = evaluateJspAttributeExtended(jspelement.body, toTrim, parser.getScriptletsCode(), this.parentCustomTag);
            value = evaluateJspAttributeSimple(jspelement.body, toTrim);
            named = true;
          }
        }
        if(named){
          //JSP.5.10 <jsp:attribute>
          //For standard or custom action attributes that do not accept a request-time expression
          //value, the Container must use the body of the <jsp:attribute> action as
          //the value of the attribute. A translation error must result if the body of the
          //<jsp:attribute> action contains anything but template text.
          if ((type == INCLUDE || type == FORWARD) && !name.equals("page") ||
            (type == USE_BEAN && !name.equals("beanName")) ||
            ((type == SET_PROPERTY || type == PARAM) && !name.equals("value")) ||
            (type == PLUGIN && !(name.equals("height")|| name.equals("width"))) )
          {
            throw new JspParseException(JspParseException.ATTRIBUTE_CAN_ACCEPT_ONLY_STATIC_VALUES, new Object[]{name, String.copyValueOf(jsptag.indent())});
          }
        }
        value = evaluateExpression(value);
        Attribute[] attr = null;
        if (startTag.attributes == null) {
          attr = new Attribute[1];
          attr[0] = new Attribute(new Indentifier(name), new Indentifier(value), named);
        } else {
          attr = new Attribute[startTag.attributes.length + 1];
          for (int i = 0; i < startTag.attributes.length; i++) {
            attr[i] = startTag.attributes[i];
            if (attr[i].isName(name)) {  //Except for when used with <jsp:element>,a translation error will result if both an XML element
                                         //attribute and a <jsp:attribute> element are used to specify the value for the same attribute.
              throw new JspParseException(JspParseException.ATTRIBUTE_ALREADY_SPECIFED_IN_ACTION, new Object[]{"name", _default_prefix_tag_start + "attribute"},
                      parser.currentFileName(), debugInfo.start);
            }
          }
          attr[attr.length - 1] = new Attribute(new Indentifier(name), new Indentifier(value), named);
        }
        startTag.setAttributes(attr);
        startTag.hasJspAttribute = true;
        return true;
      }
    }
    return false;
  }

  private String evaluateJspAttributeExtended(Element body, boolean trim, StringBuffer outBuffer, String parent) throws JspParseException {
    // name for String variable and for the method is the same
    String varName = "_jspx_jspAttr" + parser.getJspAttributeVarCount();
    //buffer for simple evaluation in a String
    StringBuffer buffer = new StringBuffer();
    //buffer for generate separated method for evaluation
    StringBuffer methodBuffer = new StringBuffer();
    methodBuffer.append("\r\nprivate String " + varName +  "(javax.servlet.jsp.JspWriter out, com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext" +
       (parent == null ? "" : ", javax.servlet.jsp.tagext.Tag "+parent)+") throws Throwable {\r\n");
    
    buffer.append(indent+"out = _jspx_pageContext.pushBody();// Start "+varName+"\r\n");
    methodBuffer.append(indent+"out = _jspx_pageContext.pushBody();// Start "+varName+"\r\n");
    //setScriptlessBody(body.elements, false);
    parser.setJspBodyVariable(varName);
    StringBuffer bodyBuffer = new StringBuffer();
    body.action(bodyBuffer);
    parser.setJspBodyVariable(null);
    if (bodyBuffer.toString().indexOf("_jspx_pageContext.evaluateInternal(") > -1) {   // ako ima EL
      trim = false;
    }
    buffer.append(bodyBuffer);
    methodBuffer.append(bodyBuffer);

    buffer.append(indent+"String " + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();\r\n");
    if(trim) {
      buffer.append(indent+varName + " = " + varName + ".trim();\r\n");
    }
    buffer.append(indent+"out = _jspx_pageContext.popBody();// END "+varName+"\r\n");
    parser.getJspAttributesCode().put(varName, buffer.toString());

    methodBuffer.append("\r\n\tlabelResult: {");
    methodBuffer.append("\r\n\tString result_" + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();");
    methodBuffer.append("\r\n\tout = _jspx_pageContext.popBody();");
    methodBuffer.append("\r\n\treturn result_" + varName + (trim ? ".trim()" : "") + ";");
    methodBuffer.append("\r\n\t}");
    methodBuffer.append("\r\n} // END " + varName + "\r\n");
    parser.getJspAttributesCode().put(varName + "_method", methodBuffer.toString());
    
    return varName;
  }

  /**
   * simple evaluation of jsp:attribute used in jsp:element.
   * does not generates separated method, returns only the name of temp variable - _jspx_jspAttr0
   */
   private String evaluateJspAttributeSimple(Element body, boolean trim) throws JspParseException {
    // name for String variable and for the method is the same
    String varName = "_jspx_jspAttr" + parser.getJspAttributeVarCount();
    //buffer for simple evaluation in a String
    StringBuffer buffer = new StringBuffer();
    buffer.append(indent+"out = _jspx_pageContext.pushBody();// Start "+varName+"\r\n");
    setScriptlessBody(body.elements, false);
    body.action(buffer);
    if (buffer.toString().indexOf("_jspx_pageContext.evaluateInternal(") > -1) {   // ako ima EL
      trim = false;
    }
    buffer.append(indent+"String " + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();\r\n");
    if(trim) {
      buffer.append(indent+varName + " = " + varName + ".trim();\r\n");
    }
    buffer.append(indent+"out = _jspx_pageContext.popBody();// END "+varName+"\r\n");
    parser.getJspAttributesCode().put(varName, buffer.toString());
    return varName;
  }

  private String evaluateJspBodySimple(Element body) throws JspParseException {
    String varName = "_jspx_jspBody" + parser.getJspBodyVarCount();
    StringBuffer buffer = new StringBuffer();
    buffer.append(indent+"out = _jspx_pageContext.pushBody();// Start "+varName+"\r\n");
    setScriptlessBody(body.elements, false);
    body.action(buffer);
    buffer.append(indent+"String " + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();\r\n");
    buffer.append(indent+"out = _jspx_pageContext.popBody();// END "+varName+"\r\n");
    parser.getJspBodyCode().put(varName, buffer.toString());
    return varName;
  }

  private String evaluateJspBody(Element body, StringBuffer outBuffer, String parent) throws JspParseException {
    String varName = "_jspx_jspBody" + parser.getJspBodyVarCount();
    StringBuffer buffer = new StringBuffer();
    buffer.append(indent+"out = _jspx_pageContext.pushBody();// Start "+varName+"\r\n");
    setScriptlessBody(body.elements, false);
    body.action(buffer);
    buffer.append(indent+"String " + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();\r\n");
    buffer.append(indent+"out = _jspx_pageContext.popBody();// END "+varName+"\r\n");
    parser.getJspBodyCode().put(varName, buffer.toString());
    return varName;
  }

  /**
    * generates code for evaluation in a separated method
    */
   private String evaluateJspAttributeMethod(Element body, boolean trim, StringBuffer outBuffer, String parent) throws JspParseException {
     String varName = "_jspx_jspAttr" + parser.getJspAttributeVarCount();
     StringBuffer buffer = new StringBuffer();
     buffer.append("\r\nprivate String " + varName +  "(javax.servlet.jsp.JspWriter out, com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext" +
       (parent == null ? "" : ", javax.servlet.jsp.tagext.Tag "+parent)+") throws Throwable {\r\n");
     buffer.append("\r\n\tout = _jspx_pageContext.pushBody();");

     parser.setJspBodyVariable(varName);
     setScriptlessBody(body.elements, false);
     body.action(buffer);
     parser.setJspBodyVariable(null);
     if (buffer.toString().indexOf("_jspx_pageContext.evaluateInternal(") > -1) {   // ako ima EL
       trim = false;
     }
     buffer.append("\r\n\tlabelResult: {");
     buffer.append("\r\n\tString result_" + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();");
     buffer.append("\r\n\tout = _jspx_pageContext.popBody();");
     buffer.append("\r\n\treturn result_" + varName + (trim ? ".trim()" : "") + ";");
     buffer.append("\r\n\t}");
     buffer.append("\r\n} // END "+varName+"\r\n");
     parser.getJspAttributesCode().put(varName, buffer.toString());
     //outBuffer.append("\t\t\tString " + varName + " = " + varName + "(out, _jspx_pageContext);\r\n"); //add method invocation
     return varName + "(out, _jspx_pageContext" + (parent == null ? "" : ", " + parent) + ")";
   }


  /**
   * generates code for evaluation in a separated method
   */
  private String evaluateJspBodyMethod(Element body, StringBuffer outBuffer, String parent) throws JspParseException {
      String varName = "_jspx_jspBody" + parser.getJspBodyVarCount();
      StringBuffer buffer = new StringBuffer();
      buffer.append("\r\nprivate String " + varName +  "(javax.servlet.jsp.JspWriter out, com.sap.engine.services.servlets_jsp.lib.jspruntime.PageContextImpl _jspx_pageContext" +
        (parent == null ? "" : ", javax.servlet.jsp.tagext.Tag "+parent)+") throws Throwable {\r\n");
      buffer.append("\r\n\tout = _jspx_pageContext.pushBody();");
      parser.setJspBodyVariable(varName);
      setScriptlessBody(body.elements, false);
      body.action(buffer);
      parser.setJspBodyVariable(null);
      buffer.append("\r\n\tlabelResult: {");
      buffer.append("\r\n\tString result_" + varName + " = ((javax.servlet.jsp.tagext.BodyContent)out).getString();");
      buffer.append("\r\n\tout = _jspx_pageContext.popBody();");
      buffer.append("\r\n\treturn result_" + varName + ";");
      buffer.append("\r\n\t}");
      buffer.append("\r\n} // END "+varName+"\r\n");
      parser.getJspBodyCode().put(varName, buffer.toString());
      //outBuffer.append("\t\t\tString " + varName + " = " + varName + "(out, _jspx_pageContext);\r\n"); //add method invocation
      return varName + "(out, _jspx_pageContext" + (parent == null ? "" : ", " + parent) + ")";
    }

  /**
   * Checks for <jsp:element
   * @param el  JspElement to inspect
   * @param typeOfElement Element type constant
   * @return true if there is <jsp:element in the body
   */
  public boolean hasJspElementInBody(JspElement el, int typeOfElement) {
    if (el == null) {
      return false;
    }
    if (el.type == typeOfElement) {
      return true;
    }
    if (el.body instanceof ElementCollection) {
      Element[] elBody = el.body.elements;
      for (int i = 0; i < elBody.length; i++) {
        if (elBody[i].elementType == typeOfElement) {
          return true;
        }
        if (elBody[i] instanceof JspElement) {
          JspElement elTemp = (JspElement)elBody[i];
          if (elTemp.type == typeOfElement) {
            return true;
          }
          if(hasJspElementInBody(elTemp, typeOfElement)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isFragment(TagAttributeInfo[] infoes, String tag) {
    if (infoes == null) {
      return false;
    }
    for (int i = 0; i < infoes.length; i++) {
      if (infoes[i].getName().endsWith(tag)) {
        return infoes[i].isFragment();
      }
    }
    return false;
  }

   /**
   * JSP2.1 JSP.1.3.4 Empty Elements
   * 1. JSP comment is allowed for empty body tags
   * 2. jsp:attribute and jsp:body considered as transparent when evaluating the body of the tag
   * 3. Tempate Data is skipped if there are jsp:attribute or jsp:body
   */
  private boolean isEmptyBody(boolean hasJspAttribute, boolean hasJspBody) {
    if (hasJspAttribute && !hasJspBody){
      return true;
    }
    if (body.elements == null) {
      if (body instanceof TemplateData) {
        return false;
      } else {
        return true;
      }
    }
    boolean result = true;
    Element[] el = body.elements;
    for (int i = 0; i < el.length; i++) {
      if (el[i] instanceof TemplateData) {
        if (hasJspAttribute || hasJspBody) {
          continue;
        } else {
          result = false;
        }
      }
      if (el[i] instanceof BodyActionTag || el[i] instanceof AttributeTag || el[i] instanceof JspCommentElement) {
        continue;
      }
      if (el[i] instanceof JspElement) {
        JspElement jspEl = (JspElement)el[i];
        if (jspEl.type == JspElement.ATTRIBUTE) {
          continue;
        } else if (jspEl.type == JspElement.BODY) {
          if (jspEl.body == null || (!(jspEl.body instanceof TemplateData) && (jspEl.body.getElements() == null || jspEl.body.getElements().length == 0))) {
            continue;
          }else if(jspEl.body instanceof ElementCollection){
            // check if all the elements inside the <jsp:body> are comments
            // if jsp:body contains only JspCommentElements it is considered empty
            boolean tmpResult = true;
            Element[] bodyElements = jspEl.body.getElements();
            for (int j = 0; j < bodyElements.length; j++) {
              if( !(bodyElements[j] instanceof JspCommentElement) ){
                tmpResult = false;
                break;
              }
            }
            if( tmpResult ){
              continue;
            }
          }
        }
      }
      result = false;
    }
    return result;
  }

  private boolean hasNonEmptyTemplateData() {

    Element[] el = body.elements;
    for (int i = 0; i < el.length; i++) {
      if (el[i] instanceof TemplateData) {
        String value = ((TemplateData)el[i]).getValue();
        if (!(value != null && value.trim().length() == 0)) {
          return true;
        }
      }
    }
    return false;
  }
  /**
   *	Checks if the body of this JspElement contains ParamTag
   * @param jspelement
   * @return false - if jsp:param not found
   */
  private boolean checkBodyForParams(JspElement jspelement) {
    boolean hasParam = false;

    if( jspelement.body != null && jspelement.body.elements != null ){
	    for (int i = 0; i < jspelement.body.elements.length; i++) {
	      if( jspelement.body.elements[i] instanceof ParamTag){
	        hasParam = true;
	      }
	    }
    }
    return hasParam;
  }
  
  /**
   * Throws exception if the tag contains jsp:attribute and the body(not whitespaces) is not defined inside jsp:body
   * @return - true - if the tag contains non-whitespace body
   * @throws JspParseException
   */
  private boolean checkForAttributesWithoutBody() throws JspParseException{
    CustomJspTag tag = (CustomJspTag) startTag;
    boolean hasBody = body != null;
    if (hasBody) {
      if (body instanceof ElementCollection && body.getElements() != null && body.getElements().length == 0 || isEmptyBody(tag.hasJspAttribute, tag.hasJspBody)) {
        hasBody = false;
      }
    }
    if( startTag.hasJspAttribute && !hasBody){
      if (tag.tagInfo != null && tag.tagInfo.getBodyContent() != null && tag.tagInfo.getBodyContent().equals(TagInfo.BODY_CONTENT_TAG_DEPENDENT)) {
        //skip check for hasNonEmptyTemplateData, when it is tagdependent tag
        return hasBody;
      }
      if ( hasNonEmptyTemplateData()) {
        throw new JspParseException(JspParseException.ELEMENT_CONTAINS_JSP_ATTRIBUTE_WITHOUT_JSP_BODY,
                parser.currentFileName(), debugInfo.start);
      }
    }
    
    return hasBody;
  }

  /**
   * Returns the parent element of this JspElement
   * @return JspElement
   */
  public JspElement getParentJspElement(){
    return  parentJspElement;
  }

  /**
   * Sets the parent element for this JspElement
   * @param parentJspElement
   */

  public void setParentJspElement(JspElement parentJspElement) {
    this.parentJspElement = parentJspElement;
  }

  /**
   * Chechs the body of JSPElement for scripting elements - scriptlets, tags with RT expression attributes
   * @param elements body of the tag
   * @return true if scripting element is found, otherwise - false
   */
  private boolean checkForScriptingElements(Element[] elements) {
    if (elements == null) {
      return false;
    }
    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof TemplateData){
        continue;
      } else if(checkScriptingElement(elements[i])) {
        return true;
      } else if(elements[i] instanceof JspTag && checkJspTag((JspTag) elements[i])) {
        return true;
      } else if (elements[i] instanceof JspElement) {
        JspElement jspEl = (JspElement) elements[i];
        if (checkScriptingElement(jspEl.startTag)) {
          return true;
        }
        if (jspEl.startTag instanceof JspTag && checkJspTag(jspEl.startTag)) {
          return true;
        }
        if (jspEl.getBody() != null && jspEl.getBody() instanceof ElementCollection && checkForScriptingElements(((JspElement)elements[i]).getBody().elements)){
          return true;
        }
      }
    }
    return false;
  }

  private boolean checkScriptingElement(Element element) {
    if (element == null) {
      return false;
    }
    if(element instanceof JspScriptlet || element instanceof ScriptletTag || element instanceof BodyActionTag ||
                element instanceof AttributeTag || element instanceof JspScriptElement || element instanceof DeclarationTag
                || element instanceof JspExpression || element instanceof ExpressionTag
                //|| element instanceof IncludeActionTag - ?
                //excluding content from other jsp's as cannot determine nested tags
                || element instanceof JspIncludeDirective || element instanceof JspIncludeDirectiveTag
                //portal parser registers its own implementation of JspIncludeDirective
                || (element instanceof JspDirective && JspDirective.DIRECTIVE_NAME_INCLUDE.equals(((JspDirective)element).getDirectiveName()))
                || JspDirective.DIRECTIVE_NAME_INCLUDE_XML.equals(element._name)
        ) {
        return true;
    }
    return false;
  }
  /**
   * Checks the JspTag for RT attrubutes and variable infos
   * @param tag
   * @return
   */
  private boolean checkJspTag(JspTag tag) {
    if (tag == null) {
      return false;
    }
    TagData tagData = new TagData((Object[][])null);
    if (tag.attributes != null) {
      for (Attribute attr : tag.attributes) {
        if (attr.isNamed || isRuntimeExpr(attr.value.toString())) {
          return true;
        } else {
          tagData.setAttribute(attr.name.toString(), attr.value.toString());
        }
      }
    }
    if (tag instanceof CustomJspTag) {
      CustomJspTag cjt = (CustomJspTag)tag;
      if (cjt.tagInfo != null) { // some CustomJspTag implementation (like BodyTag) are not real taglib tags
        return !((cjt.tagInfo.getVariableInfo(tagData) == null || cjt.tagInfo.getVariableInfo(tagData).length == 0) &&
                                cjt.tagInfo.getTagVariableInfos().length == 0);
      }
    }
    return false;
  }

  /**
   * Sets flag that parent tag is separated in to a single method
   * @param elements
   */
  private void setScriptlessBody(Element[] elements, boolean flag) {
    if (elements == null) {
      return;
    }
    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof JspTag && ((JspTag)elements[i]).getBody() != null) {
        Element body = ((JspTag)elements[i]).getBody();
        if (body instanceof ElementCollection) {
          setScriptlessBody(body.elements, flag);
        } else if (body instanceof JspElement && ((JspElement)body).getBody() != null && ((JspElement)body).getBody().elements != null) {
          setScriptlessBody(((JspElement)body).getBody().elements, flag);
        }
      } else if (elements[i] instanceof CustomJspTag) {
        ((CustomJspTag) elements[i]).setInMethod(flag);
      } else if (elements[i] instanceof JspElement) {
        JspElement jspEl = (JspElement) elements[i];
        if(jspEl.startTag instanceof CustomJspTag) {
          ((CustomJspTag) jspEl.startTag).setInMethod(flag);
        }
        if(jspEl.getBody() != null && jspEl.getBody() instanceof ElementCollection) {
          setScriptlessBody(jspEl.getBody().elements, flag);
        }
      }
      
    }
  }

  private void checkForJspAttribute(StringBuffer buffer) {
    if (this.startTag == null || !startTag.hasJspAttribute) {
      return;
    }

    if (this.startTag.attributes != null) {
      for (Attribute attr : this.startTag.attributes) {
        if (attr.isNamed) {
          String attrValueVariable = attr.value.toStringUnquoted();
          String jspAttrCode = (String) parser.getJspAttributesCode().get(attrValueVariable);
          if (jspAttrCode != null) {
            buffer.append(indent + jspAttrCode);
            parser.getJspAttributesCode().remove(attrValueVariable);
          }
        }
      }
    }

  }

}

