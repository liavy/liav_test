/*
 * Created on 2004-6-28
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.*;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.*;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.io.FileReader;
import java.io.CharArrayWriter;

/**
 * Utility class for creating elements of the jsp tree.
 *
 * @author ivo-s
 */
public class SAXJspElementFactory {

  /* logging location */
  static Location location = LogContext.getLocationJspParser();

  /*
   * Creates object for the <jsp:root syntax element
   * and its start tag
   */
  static Element createRootElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) throws JspParseException{
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createRootElement");
    }
    JspElement rootElement = new JspElement(JspElement.ROOT);
    rootElement.parser = parser;
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ROOT);//no new tag is created.
    setPrefix(startTag, parser);
    startTag.parser = parser;
    startTag.debugInfo = debugInfo;

    Attribute[] newAtrributes = convertRootAttributes(attributes);
    Attribute[] merged = mergeRootAttributes(startTag.getAttributes(), newAtrributes);
    startTag.setAttributes(merged);
    rootElement.setStartTag(startTag);
    rootElement.setBody(new ElementCollection(parser));

    return rootElement;
  }

  /**
   * Merges the attributes of jsp:root elements of including and included JSP.
   * The version attribute of including attribute is valid.
   * @param mainAttributes - the attributes from the including JSP
   * @param includedAttributes - the attributes from the included JSP.
   * @return Attribute[] - merged attributes
   * @throws JspParseException - If the including attribute tries to redefine the value for a prefix.
   */
  private static Attribute[] mergeRootAttributes(Attribute[] mainAttributes, Attribute[] includedAttributes) throws JspParseException{
    if( mainAttributes == null || mainAttributes.length == 0 ){
      return includedAttributes;
    }
    List<Attribute> merged = new ArrayList<Attribute>(5);                    // most of the root tags include max 5 namespace definitions
    for (int i = 0; i < mainAttributes.length; i++) {
      merged.add(mainAttributes[i]);
    }
    for (int i = 0; i < includedAttributes.length; i++) {
      Attribute newAttribute = includedAttributes[i];
      String newName = newAttribute.name.toString();
      if( !newName.equals("version") ){ // merge only namespace attributes
        boolean found = false;
        for (int j = 0; j < mainAttributes.length; j++) {
          Attribute oldAttribute = mainAttributes[j];
          if( newAttribute.name.toString().equals(oldAttribute.name.toString()) ) {
           if( !newAttribute.value.toString().equals(oldAttribute.value.toString()) ){
             // JSP 2.1 spec 1.10.5
             // If a taglib directive is encountered in a standard syntax page that attempts to
             // redefine a prefix that is already defined in the current scope (by a JSP segment
             // in either syntax), a translation error must occur unless that prefix is being redefined
             // to the same namespace URI.
             throw new JspParseException(JspParseException.JSP_DOCUMENT_CANNOT_REDEFINE_PREFIX, new Object[]{oldAttribute.value.toString(), newName, newAttribute.value.toString()});
           }
           found = true;
           break;
          }
        }// end for
        if ( !found ) {
          merged.add(newAttribute);
        }
      }
    }
    return (Attribute[])merged.toArray(new Attribute[0]);
  }

  /*
   * Creates object for the <jsp:text syntax element
   * and its start tag
   */
  static Element createTextElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createTextElement");
    }
    JspElement rootElement = new JspElement(JspElement.TEXT);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_TEXT);//new TextTag();
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:declaration syntax element
   * and its start tag
   */
  static Element createDeclarationElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDeclarationElement");
    }
    JspElement rootElement = new JspElement(JspElement.DECLARATION);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DECLARATION);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement, startTag, attributes);
  }

  /*
   * Creates object for the <jsp:scriptlet syntax element
   * and its start tag
   */
  static Element createScriptletElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createScripletElement");
    }
    JspElement rootElement = new JspElement(JspElement.SCRIPLET);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_SCRIPTLET);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement, startTag, attributes);
  }

  /*
   * Creates object for the <jsp:expression syntax element
   * and its start tag
   */
  static Element createExpressionElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createExpressionElement");
    }
    JspElement rootElement = new JspElement(JspElement.EXPRESSION);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_EXPRESSION);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:useBean syntax element
   * and its start tag
   */
  static JspElement createUseBeanElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createUseBeanElement");
    }
    JspElement rootElement = new JspElement(JspElement.USE_BEAN);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_USE_BEAN);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:setProperty syntax element
   * and its start tag
   */
  static Element createSetPropertyElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createSetPropertyElement");
    }
    JspElement rootElement = new JspElement(JspElement.SET_PROPERTY);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_SET_PROPERTY);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:getProperty syntax element
   * and its start tag
   */
  static Element createGetPropertyElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createGetPropertyElement");
    }
    JspElement rootElement = new JspElement(JspElement.GET_PROPERTY);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_GET_PROPERTY);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:include syntax element
   * and its start tag
   */
  static Element createIncludeElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createIncludeElement");
    }
    JspElement rootElement = new JspElement(JspElement.INCLUDE);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_INCLUDE);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:forward syntax element
   * and its start tag
   */
  static Element createForwardElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createForwardElement");
    }
    JspElement rootElement = new JspElement(JspElement.FORWARD);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_FORWARD);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:params syntax element
   * and its start tag
   */
  static Element createParamElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createParamElement");
    }
    JspElement rootElement = new JspElement(JspElement.PARAM);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PARAM);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:plugin syntax element
   * and its start tag
   */
  static Element createPluginElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createPluginElement");
    }
    JspElement rootElement = new JspElement(JspElement.PLUGIN);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PLUGIN);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:params syntax element
   * and its start tag
   */
  static Element createParamsElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createParamsElement");
    }
    JspElement rootElement = new JspElement(JspElement.PARAMS);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PARAMS);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:fallback syntax element
   * and its start tag
   */
  static Element createFallbackElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createFallbackElement");
    }
    JspElement rootElement = new JspElement(JspElement.FALLBACK);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_FALLBACK);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:attribute syntax element
   * and its start tag
   */
  static Element createAttributeElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createAttributeElement");
    }
    JspElement rootElement = new JspElement(JspElement.ATTRIBUTE);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ATTRIBUTE);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:body syntax element
   * and its start tag
   */
  static Element createBodyElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createBodyElement");
    }
    JspElement rootElement = new JspElement(JspElement.BODY);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_BODY);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:invoke syntax element
   * and its start tag
   */
  static Element createInvokeElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createInvokeElement");
    }
    JspElement rootElement = new JspElement(JspElement.INVOKE);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_INVOKE);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:doBody syntax element
   * and its start tag
   */
  static Element createDoBodyElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDoBodyElement");
    }
    JspElement rootElement = new JspElement(JspElement.DO_BODY);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DO_BODY);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:element syntax element
   * and its start tag
   */
  static Element createElementElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createElementElement");
    }
    JspElement rootElement = new JspElement(JspElement.ELEMENT);
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ELEMENT);
    setPrefix(startTag, parser);
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*
   * Creates object for the <jsp:output syntax element
   * and its start tag
   */
  static Element createOutputElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createOutputElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_OUTPUT);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for the <jsp:directive.page syntax element
   * and its start tag
   */
  static Element createDirectivePageElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDirectivePageElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DIRECTIVE_PAGE);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for the <jsp:directive.include syntax element
   * and its start tag
   */
  static Element createDirectiveIncludeElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDirectiveIncludeElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DIRECTIVE_INCLUDE);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for the <jsp:directive.tag syntax element
   * and its start tag
   */
  static Element createDirectiveTagElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDirectiveTagElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DIRECTIVE_TAG);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for the <jsp:directive.attribute syntax element
   * and its start tag
   */
  static Element createDirectiveAttributeElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDirectiveAttributeElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DIRECTIVE_ATTRIBUTE);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for the <jsp:directive.variable syntax element
   * and its start tag
   */
  static Element createDirectiveVariableElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createDirectiveVariableElement");
    }
    JspTag rootElement = (JspTag)parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DIRECTIVE_VARIABLE);
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    rootElement.setAttributes(convertAttributes(attributes));
    rootElement.setBody(new ElementCollection(parser));
    return rootElement;
  }

  /*
   * Creates object for characters
   */
  static Element createTemplateDataElement(JspPageInterface parser,String qName,DebugInfo debugInfo,Attributes attributes,boolean starttag, boolean isTagdependent) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createTemplateDataElement");
    }
    StringBuffer sb = new StringBuffer();
    if (starttag) {
      sb.append("<").append(qName);
      // TODO namespace attributes
      for (int i=0;i<attributes.getLength();i++) {
        if ("xmlns:jsp".equals(attributes.getQName(i))) {
          continue; //skip only this attribute
        }
        sb.append(" ").append(attributes.getQName(i)).append("=");
        String quote = "\"";
        /* TODO quote
        String attributeValue=attributes.getValue(i);
        if (attributeValue.indexOf(quote != -1)) {
           quote = "'";
        }*/

        sb.append(quote).append(attributes.getValue(i)).append(quote);
      }
    } else {
      sb.append("</").append(qName);
    }
    sb.append(">");
    TemplateData rootElement = new TemplateData(sb.toString(), true);
    rootElement.isTagDependent = isTagdependent;
    setPrefix(rootElement, parser);
    rootElement.debugInfo = debugInfo;
    rootElement.parser = parser;
    return rootElement;
  }

  /*
   * Creates object for the a custom syntax element
   * and its start tag
   */
  static Element createCustomElement(JspPageInterface parser,DebugInfo debugInfo,Attributes attributes,String startIndent, String endIndent) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.createCustomElement");
    }
    JspElement rootElement = new JspElement(JspElement.CUSTOM);
    //CustomJspTag startTag = new CustomJspTag(startIndent.toCharArray(),endIndent.toCharArray());
    CustomJspTag startTag = (CustomJspTag)parser.getRootElement().getRegisteredElement(startIndent);
    if (startTag == null ) {// only debug message for TagsTest
      LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000605",
        "CustomTag not found. Indent:{0}", new String[] {startIndent}, null, null);
      if( parser.getTagLibDescriptors() != null && parser.getTagLibDescriptors().keys() != null ) {
        Enumeration<String> keys = parser.getTagLibDescriptors().keys();
        while( keys.hasMoreElements() ) {
          String key = keys.nextElement();
          LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000606",
            "Found TLD. URI:{0}", new String[] {key}, null, null);
        }
      }
      try {
        FileReader fr = new FileReader(parser.currentFileName());
        CharArrayWriter cw  = new CharArrayWriter();
        char[] chars = new char[2048];
        int r;
        while ((r = fr.read(chars)) > 0) {
           cw.write(chars, 0, r);
        }
        LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000610",
          "The parsing page is {0}:{1}", new String[] {parser.currentFileName(), cw.toString()}, null, null);

      } catch (Exception e) {
        LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).traceError("ASJ.web.000611",
          "ERROR: The parsing page is {0}. the err msg is {1}", new String[] {parser.currentFileName(), e.getMessage()}, null, null);
      }
    }
    startTag.setStartTagIndent(startIndent.toCharArray());
    startTag.setEndTagIndent(endIndent.toCharArray());
    startTag.JSP_ID = parser.getRootElement().JSP_ID = " " + parser.getRootElement()._default_prefix.peek() + ":id";
    //setPrefix(startTag, parser); CustomTags do not need _default_prefix
    startTag.debugInfo = debugInfo;
    rootElement.parser = parser;
    startTag.parser = parser;
    return initJspElementWithStartTag(rootElement,startTag,attributes);
  }

  /*********************************************************************************/

  /*
   * Finish construction of the object that represent <jsp:root
   * syntax element
   */
  static void closeRootElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeRootElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ROOT);//new RootTag();
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:text
   * syntax element
   */
  static void closeTextElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeTextElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_TEXT);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:declaration
   * syntax element
   */
  static void closeDeclarationElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDeclarationElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DECLARATION);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element, endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:scriptlet
   * syntax element
   */
  static void closeScriptletElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeScripletElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_SCRIPTLET);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element, endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:expression
   * syntax element
   */
  static void closeExpressionElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeExpressionElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_EXPRESSION);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:useBean
   * syntax element
   */
  static void closeUseBeanElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeUseBeanElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_USE_BEAN);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:setProperty
   * syntax element
   */
  static void closeSetPropertyElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeSetPropertyElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_SET_PROPERTY);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:getProperty
   * syntax element
   */
  static void closeGetPropertyElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeGetPropertyElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_GET_PROPERTY);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:include
   * syntax element
   */
  static void closeIncludeElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeIncludeElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_INCLUDE);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:forward
   * syntax element
   */
  static void closeForwardElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeForwardElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_FORWARD);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:param
   * syntax element
   */
  static void closeParamElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeParamElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PARAM);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:plugin
   * syntax element
   */
  static void closePluginElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closePluginElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PLUGIN);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:params
   * syntax element
   */
  static void closeParamsElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeParamsElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_PARAMS);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:fallback
   * syntax element
   */
  static void closeFallbackElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeFallbackElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_FALLBACK);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:attribute
   * syntax element
   */
  static void closeAttributeElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeAttributeElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ATTRIBUTE);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:body
   * syntax element
   */
  static void closeBodyElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeBodyElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_BODY);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:invoke
   * syntax element
   */
  static void closeInvokeElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeInvokeElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_INVOKE);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:doBody
   * syntax element
   */
  static void closeDoBodyElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDoBodyElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_DO_BODY);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:element
   * syntax element
   */
  static void closeElementElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeElementElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement(SAXTreeBuilder.JSP_ELEMENT);
    setPrefix(endTag, element.parser);
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  /*
   * Finish construction of the object that represent <jsp:output
   * syntax element
   */
  static void closeOutputElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeOutputElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent <jsp:directive.page
   * syntax element
   */
  static void closeDirectivePageElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDirectivePageElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent <jsp:directive.include
   * syntax element
   */
  static void closeDirectiveIncludeElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDirectiveIncludeElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent <jsp:directive.tag
   * syntax element
   */
  static void closeDirectiveTagElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDirectiveTagElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent <jsp:directive.attribute
   * syntax element
   */
  static void closeDirectiveAttributeElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDirectiveAttributeElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent <jsp:directive.variable
   * syntax element
   */
  static void closeDirectiveVariableElement(DebugInfo debugInfo,Element element) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeDirectiveVariableElement");
    }
    ((JspTag)element).tagType = JspTag.SINGLE_TAG;
  }

  /*
   * Finish construction of the object that represent a custom
   * syntax element
   */
  static void closeCustomElement(DebugInfo debugInfo,Element element, String qName) {
    if (location.bePath()) {
      location.pathT("SAXJspElementFactory.closeCustomElement");
    }
    CustomJspTag endTag = (CustomJspTag)element.parser.getRootElement().getRegisteredElement("<"+qName);
    endTag.setStartTagIndent(("<"+qName).toCharArray());
    endTag.setEndTagIndent(("</"+qName+">").toCharArray());
    endTag.JSP_ID = element.parser.getRootElement().JSP_ID = " " + element.parser.getRootElement()._default_prefix.peek() + ":id";
    // setPrefix(endTag, element.parser); CustomTags do not need _default_prefix
    //CustomJspTag endTag = new CustomJspTag(("<"+qName).toCharArray(),("</"+qName+">").toCharArray());
    endTag.debugInfo = debugInfo;
    initJspElementWithEndTag((JspElement)element,endTag);
  }

  private static com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[] convertAttributes(Attributes attributes) {
    if (attributes == null) {
      return new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[0];
    }

    Vector v = new Vector();

    for (int i=0;i<attributes.getLength();i++) {
      if (SAXTreeBuilder.URI_XMLNS.equals(attributes.getURI(i))              // JDK5.0 SAXParser return empty string for Attributes.getURI()
        || attributes.getQName(i).startsWith(SAXTreeBuilder.XMLNS_PREFIX)  // The fix is to check if attribute name starts with "xmlns:"
        || attributes.getQName(i).startsWith(SAXTreeBuilder.XMLNS)) { // The fix is to check if attribute name starts with "xmlns"
        continue; // this is not real attribute for the tag
      }
      Indentifier name = null;
      if (SAXTreeBuilder.URI_EMPTY.equals(attributes.getURI(i))) {
        name = new Indentifier(attributes.getQName(i));
      } else {
        name = new Indentifier(attributes.getLocalName(i));
      }
      Indentifier value = new Indentifier(attributes.getValue(i));
      name.indentifierType = Indentifier.NOT_QUOTED;
      value.indentifierType = Indentifier.QUOTED;
      com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute attribute =
        new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute(name,value, false);
      v.add(attribute);
    }
    com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[] result =
      new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[v.size()];
    v.toArray(result);
    return result;
  }

  private static com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[] convertRootAttributes(Attributes attributes) {
    if (attributes == null) {
      return new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[0];
    }

    Vector v = new Vector();

    for (int i=0;i<attributes.getLength();i++) {
      if (SAXTreeBuilder.URI_XMLNS.equals(attributes.getValue(i)) || SAXTreeBuilder.URI_XHTML.equals(attributes.getValue(i))) {
        continue;
      }
      Indentifier name = new Indentifier(attributes.getQName(i));
      Indentifier value = new Indentifier(attributes.getValue(i));
      name.indentifierType = Indentifier.NOT_QUOTED;
      value.indentifierType = Indentifier.QUOTED;
      com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute attribute =
        new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute(name,value, false);
      v.add(attribute);
    }
    com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[] result =
      new com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Attribute[v.size()];
    v.toArray(result);
    return result;
  }

  private static JspElement initJspElementWithStartTag(JspElement element, CustomJspTag tag, Attributes attributes) {
    tag.setAttributes(convertAttributes(attributes));
    element.setStartTag(tag);
    element.setBody(new ElementCollection(element.parser));
    return element;
  }



  private static void initJspElementWithEndTag(JspElement element, CustomJspTag tag) {
    tag.setIsEndTag();
    tag.parser = element.parser;
    element.setEndTag(tag);
    Position start = element.getStartTag().debugInfo.start;
    Position end = tag.debugInfo.end;
    String fileName =  tag.debugInfo.fileName;

    DebugInfo debugInfo = new DebugInfo(start,end,fileName, element.parser.getWebContainerParameters().isJspDebugSupport());
    element.debugInfo = debugInfo;
  }


  static Element[] mergeElements(Element[] elements, Element[] addElements) {
    if (elements == null || elements.length <= 0 ) {
      return addElements;
    } else {
      if (addElements == null || addElements.length <=0) {
        return elements;
      }
      Element last = elements[elements.length-1];
      Element first = addElements[0];
      if (last instanceof TemplateData &&
          first instanceof TemplateData) {
        String lastValue = ((TemplateData)last).value;
        String firstValue = ((TemplateData)first).value;
        StringBuffer sb = new StringBuffer(lastValue.length() + firstValue.length());
        sb.append(lastValue);
        sb.append(firstValue);
        TemplateData element = new TemplateData(sb.toString(), (((TemplateData)first).isRootTag));
        ((TemplateData) element).isTagDependent = ((TemplateData)first).isTagDependent;
        setPrefix(element, first.parser);
        // debug info merge
        DebugInfo debugInfo = new DebugInfo(((TemplateData)last).debugInfo.start,
                                            ((TemplateData)first).debugInfo.end,
                                            ((TemplateData)first).debugInfo.fileName,
                                            ((TemplateData)first).debugInfo.isJspDebugSuppport());
        element.debugInfo = debugInfo;
        element.parser = last.parser;

        Element[] newElements = new Element[elements.length+addElements.length-1];
        System.arraycopy(elements,0,newElements,0,elements.length-1);
        newElements[elements.length-1] = element;
        System.arraycopy(addElements,1,newElements,elements.length,addElements.length-1);
        return newElements;
      } else {
        Element[] newElements = new Element[elements.length+addElements.length];
        System.arraycopy(elements,0,newElements,0,elements.length);
        System.arraycopy(addElements,0,newElements,elements.length,addElements.length);
        return newElements;
      }
    }
  }


  static Element[] addElement(Element[] elements, Element element, boolean isTagDependent) {
    if (elements == null || elements.length <= 0 ) {
      return new Element[]{element};
    } else {
      Element last = elements[elements.length-1];
      if (last instanceof TemplateData &&
          element instanceof TemplateData) {
        String lastValue = ((TemplateData)last).value;
        String elementValue = ((TemplateData)element).value;
        StringBuffer sb = new StringBuffer(lastValue.length() + elementValue.length());
        sb.append(lastValue);
        sb.append(elementValue);
        TemplateData templateData = new TemplateData(sb.toString(), (((TemplateData)last).isRootTag) || (((TemplateData)element).isRootTag));
        setPrefix(templateData, element.parser);
        // debug info merge
        DebugInfo debugInfo = new DebugInfo(((TemplateData)last).debugInfo.start,
                                            ((TemplateData)element).debugInfo.end,
                                            ((TemplateData)element).debugInfo.fileName,
                                            ((TemplateData)element).debugInfo.isJspDebugSuppport());
        templateData.parser = last.parser;
        templateData.debugInfo = debugInfo;
        templateData.isTagDependent = isTagDependent;
        elements[elements.length-1] = templateData;
        return elements;
      } else {
        Element[] newElements = new Element[elements.length+1];
        System.arraycopy(elements,0,newElements,0,elements.length);
        newElements[elements.length] = element;
        return newElements;
      }
    }
  }


  static Element[] addTemplateData(JspPageInterface parser,Element[] elements, DebugInfo debugInfo,char[] ch , int start,int length, boolean isTagDependent) {
    if (elements == null || elements.length <= 0 ||
         !(elements[elements.length-1] instanceof TemplateData)) {
      String stringData = new String(ch,start,length);
      TemplateData newData = new TemplateData(stringData, false);
      newData.isTagDependent = isTagDependent;
      setPrefix(newData, parser);
      newData.debugInfo = debugInfo;
      newData.parser = parser;
      return addElement(elements,newData, isTagDependent);
    } else {
      int index = elements.length - 1;
      Element lastEl = elements[index];
      StringBuffer stringData = new StringBuffer(((TemplateData)lastEl).value);
      stringData.append(ch,start,length);
      elements[index] = mergeTemplateData((TemplateData)lastEl,debugInfo,ch,start,length, isTagDependent);
      return elements;
    }
  }

  static TemplateData mergeTemplateData(TemplateData element, DebugInfo debugInfo,char[] ch , int start,int length, boolean isTagDependent) {
    StringBuffer stringData = new StringBuffer((element).value);
    stringData.append(ch,start,length);

    TemplateData templateData = new TemplateData(stringData.toString(), element.isRootTag);
    setPrefix(templateData, element.parser);
    // debug info merge
    DebugInfo debugInfo1 = new DebugInfo((element).debugInfo.start,
                                           debugInfo.end,
                                           debugInfo.fileName,
                                           debugInfo.isJspDebugSuppport());
    templateData.debugInfo = debugInfo1;
    templateData.parser = element.parser;
    templateData.isTagDependent = isTagDependent;
    return templateData;
  }

  private static void setPrefix(Element el, JspPageInterface parser) {
    el._default_prefix.push(parser.getRootElement()._default_prefix.peek());
    el._default_prefix_tag_end = parser.getRootElement()._default_prefix_tag_end;
    el._default_prefix_tag_start = parser.getRootElement()._default_prefix_tag_start;
    el.JSP_ID = parser.getRootElement().JSP_ID = " " + parser.getRootElement()._default_prefix.peek() + ":id";
    if( el instanceof JspTag ) {
      CustomJspTag currentCustomTag = (CustomJspTag) el;
      currentCustomTag.setEndTagIndent((el._default_prefix_tag_end + currentCustomTag._name + ">").toCharArray());
      currentCustomTag.setStartTagIndent((el._default_prefix_tag_start + currentCustomTag._name ).toCharArray());
    }

  }

}
