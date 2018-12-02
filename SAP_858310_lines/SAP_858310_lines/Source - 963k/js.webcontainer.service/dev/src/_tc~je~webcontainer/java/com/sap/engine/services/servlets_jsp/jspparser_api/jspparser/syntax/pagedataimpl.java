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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.tagext.PageData;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.JspIncludeDirectiveTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.RootTag;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.XMLTag;
import com.sap.engine.services.servlets_jsp.server.LogContext;

/**
 * Class responsible for generating the XML stream representing
 * the JSP translation unit being compiled.
 *
 * @author Boby Kadrev
 */
public class PageDataImpl extends PageData {

  private Element[] elements = null;
  private String body = "";
  private String root = "jsp:root\n\txmlns:jsp=\"http://java.sun.com/JSP/Page\"\n\t";
  private String version = "2.0";
  private String rootEnd = "version=\""+ version +"\"\n\tjsp:id=\"0\"\n>\n";
  private String rootSlashEnd = "</jsp:root>";
  public IDCounter id = null;
  private boolean isXml = false;
  private Element rootElement = null;
  /**
   * Constructs this with parsed Element.
   *
   * @param   rootElement  Element that is already parsed
   */
  public PageDataImpl(Element rootElement, boolean isXml, boolean isOldApplication) {
    root = rootElement.parser.getRootElement()._default_prefix_tag_start +  "root\n\txmlns:" + rootElement.parser.getRootElement()._default_prefix.peek() + "=\"http://java.sun.com/JSP/Page\"\n\t";
    rootEnd = "version=\""+ version +"\"\n" + rootElement.parser.getRootElement()._default_prefix.peek() + ":id=\"0\"\n>\n";
    rootSlashEnd = rootElement.parser.getRootElement()._default_prefix_tag_end + "root" + Element.END;
    id = new IDCounter();
    if (isOldApplication) { //fix for CTS1.3.1
      version = "1.2";
    }
    elements = rootElement.elements;
    this.isXml = isXml;
    this.rootElement = rootElement;
    //    System.out.println(body);
  }

  public void generateXMLView() throws JspParseException {
    if (isXml) {
      doXmlStyle(rootElement);
    } else {
      doJspStyle();
    }
    if ( LogContext.getLocationJspParserXmlView().beDebug() ){
    	LogContext.getLocationJspParserXmlView().debugT("XML view dump for file:"+rootElement.parser.currentFileName(), body);
    }
  }

  private void doJspStyle() throws JspParseException {
    body = root;

    // get taglibs and namespace from jsp:root and add them to the jsp:root of the XML view.
    // get taglibs directives from the pages with JSP syntax
    List<Attribute>  taglibs = extractTaglib(elements);
    // if there are included JSP documents the namespace attributes should be added too.
    RootTag rootTag = (RootTag)rootElement.parser.getRootElement().getRegisteredElement("root");
    if( rootTag.getAttributes() != null ) {
      for (int i = 0; i < rootTag.getAttributes().length; i++) {
        Attribute currAttirbute = rootTag.getAttributes()[i];
        if( currAttirbute.name.value.startsWith("xmlns:") &&
            !currAttirbute.name.value.startsWith("xmlns:jsp")) {
          taglibs.add(currAttirbute);
        }
      }
    }
    for (Iterator<Attribute> iter = taglibs.iterator(); iter.hasNext();) {
      Attribute attribute = iter.next();
      body += attribute.name + "=\"" + attribute.value+"\"\n\t";
    }
    body = body + rootEnd;

    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof OutputCommentElement || elements[i] instanceof JspCommentElement || isDirectiveName(elements[i], JspDirective.DIRECTIVE_NAME_TAGLIB)) {
        continue;
      }

      if (elements[i] instanceof TemplateData) {
        if (((TemplateData) elements[i]).isEmpty()) {
          continue;
        }
      }

      //System.out.println("------------------------------\n["+i+"] "+elements[i]+"\n*"+elements[i].getString()+"*\n");
      body = body + elements[i].getString(id) + "\n";
    }

    body = body + rootSlashEnd;
  }

  /**
   * Checks if the given element is directive with this name.
   * @param element
   * @return
   */
  private boolean isDirectiveName(Element element, String directiveName) {
    boolean result = false;
    if (element instanceof JspDirective) {
      JspDirective directive = (JspDirective) element;
      if( directiveName.equals(directive.getDirectiveName())) {
        result = true;
      }
    }
    return result;
  }


  /**
   * Searches for all Taglib Directives in the JSP and the statically included JSPs.
   * @param elements - elements of the given JSP page
   * @return - List with attributes suitable for attributes for the root tag of the XML view or empty list if the given element array is null.
   * @throws JspParseException
   */
  private List<Attribute>  extractTaglib(Element[] elements) throws JspParseException{
    List<Attribute> foundTaglibs = new ArrayList<Attribute>();
    if( elements == null ) {
      return foundTaglibs;
    }
    for (int i = 0; i < elements.length; i++) {
      if ( isDirectiveName(elements[i], JspDirective.DIRECTIVE_NAME_TAGLIB) ) {

        JspDirective taglibDirective = (JspDirective) elements[i];
        String prefix = taglibDirective.getAttributeValue("prefix");
        String uri = taglibDirective.getAttributeValue("uri");
        String tagDir = null;
        if ( uri == null ) {
          tagDir = taglibDirective.getAttributeValue("tagdir");
        }
        String attrName = "xmlns:"+prefix;
        String attrValue = uri;
        if( uri == null ) {
          attrValue = tagDir;
        }
        boolean alreadyFound = false;
        // check if this taglib is already found
        for(Attribute attribute : foundTaglibs) {
          if( attribute.name.value.equals(attrName) ) {
            alreadyFound = true;
            break;
          }
        }
        if( alreadyFound ) {
          break;
        }
        Attribute attribute = new Attribute(new Indentifier(attrName), new Indentifier(attrValue), false);
        foundTaglibs.add(attribute);
      }
      if( isDirectiveName(elements[i], JspDirective.DIRECTIVE_NAME_INCLUDE) ||
        elements[i] instanceof JspIncludeDirectiveTag || JspDirective.DIRECTIVE_NAME_INCLUDE_XML.equals(elements[i]._name) //XML syntax
        ) {
        JspTag includeDiirective = (JspTag)elements[i];
        Element[] includeElements = null;
        if( includeDiirective.getBody() instanceof ElementCollection ) {
          includeElements = includeDiirective.getBody().getElements();
        } else {
          includeElements = new Element[] {includeDiirective.getBody()};
        }
        mergeFoundTaglibs(foundTaglibs, extractTaglib(includeElements));
      }
      if( elements[i] instanceof JspElement) {
        // it is possible that customTag to have a JSP body with include directive in it.
        JspElement jspElement = (JspElement) elements[i];
        Element[] includeElements = null;
        if( jspElement.getBody() instanceof ElementCollection ) {
          includeElements = jspElement.getBody().getElements();
        } else {
          includeElements = new Element[] {jspElement.getBody()};
        }
        mergeFoundTaglibs(foundTaglibs, extractTaglib(includeElements));
      }
    }
    return foundTaglibs;
  }

  /**
   * Adds the attributes from the new list to the elements of the oldList.
   * Only the elements with new names are added.
   * @param oldList - destination list
   * @param newList - source list
   */
  private void mergeFoundTaglibs(List<Attribute> oldList, List<Attribute> newList) {
    if( newList == null || oldList == null || newList.isEmpty()) {
      return;
    }
    if( oldList.isEmpty() ) {
      oldList.addAll(newList);
    }
    for(Attribute newAttr : newList) {
      boolean found = false;
      for(Attribute oldAttr : oldList) {
        if( newAttr.name.value.equals(oldAttr.name.value) ) {
          found = true;
          break;
        }
      }
      if( !found ) {
        oldList.add(newAttr);
      }
    }

  }

  private void doXmlStyle(Element rootEl) throws JspParseException {
    //startTag always is RootTag
    JspElement jspEl = ((JspElement)rootEl);
    elements = jspEl.body.elements;

    RootTag  root = (RootTag)jspEl.startTag;
    List<Attribute> taglibs = extractTaglib(elements);
    for (Iterator<Attribute> iter = taglibs.iterator(); iter.hasNext();) {
      Attribute attribute = iter.next();
      root.addAttribute(attribute);
    }
    body = root.getString(id);

    if( elements == null && jspEl.body instanceof TemplateData ){
      // the body of the JSP document consists only of template text
      body = body + jspEl.body.getString(id) + "\n";
    }else{
      for (int i = 0; i < elements.length; i++) {
        if ( elements[i] instanceof JspCommentElement || elements[i] instanceof XMLTag) {
          continue;
        }
        //(spec 2.1)JSP.10.1.12 Template Text and XML Elements
        if (elements[i] instanceof TemplateData) {
          if (((TemplateData) elements[i]).isEmpty()) {
            continue;
          }
        }
        //System.out.println("------------------------------\n["+i+"] "+elements[i]+"\n*"+elements[i].getString()+"*\n");
        body = body + elements[i].getString(id) + "\n";
      }
    }
    body = body + rootSlashEnd;
  }

  /**
   * Returns an input stream on the XML view of a JSP page.
   *
   * @return
   */
  public InputStream getInputStream() {
    try {
      return new ByteArrayInputStream(body.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return new ByteArrayInputStream(body.getBytes());
    }
  }

  public String toString() {
    return body;
  }

}

