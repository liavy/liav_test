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

import java.io.File;
import java.io.IOException;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.xmlsyntax.RootTag;
import com.sap.engine.services.servlets_jsp.server.LogContext;

/*
 *
 * @author Galin Galchev,Ivo Simeonov
 * @version 4.0
 */
public class JspIncludeDirective extends JspDirective {

  /*
   * all attributes that this element can
   * accept
   */
  public static final String[] attributeNames = {"file"};

  /**
   * Constructs new JspIncludeDirective
   *
   */
  public JspIncludeDirective() {
    elementType = Element.INCLUDE_DIRECTIVE;
  }

  /**
   * Takes specific action corresponding to this jsp element
   * logic
   *
   * @exception   JspParseException  thrown if error occurs during
   * verification
   */
  public void action(StringBuffer buffer) throws JspParseException {
    body.action(buffer);
  }

  public void action() throws JspParseException {
    action(parser.getScriptletsCode());
  }

  /**
   * Verifies the attributes of this tag
   *
   * @exception   JspParseException  thrown if error occurs
   * during verification
   */
  public void verifyAttributes() throws JspParseException {
    checkAttributes(attributeNames);
    String path;
    String attributeValue = getAttributeValue("file");
    if (attributeValue.startsWith("/")) {
      path = parser.getRealPath(attributeValue);
    } else {
      path = parser.getPath() + "/" + attributeValue;
      path = new File(path).getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar);
    }
    // canonicalize even in http alias
    // should be the same as JSPProcessor.parse and JSPChecker.compileAndGetClassName
    path = ParseUtils.canonicalizeFS(path).replace(File.separatorChar, ParseUtils.separatorChar);
    try {
      if (!isIncludeAllowed(path)) {
        throw new JspParseException(JspParseException.CANNOT_INCLUDE_FILE, new Object[]{attributeValue}, parser.currentFileName(), debugInfo.start);
      }
    } catch (IOException io) {
      throw new JspParseException(JspParseException.CANNOT_INCLUDE_FILE, new Object[]{attributeValue}, parser.currentFileName(), debugInfo.start, io);
    }
    File f = new File(path);
    body = parser.parse(f);
    String[] included = (String[])parser.getParserParameters().getIncludedFilesHashtable().get(parser.currentFileName());
    if (included != null) {
      String[] temp = new String[included.length + 1];
      for (int i = 0; i < included.length; i++) {
        if (included[i].equals(path)) {  //already exist
           return;
        }
        temp[i] = included[i];
      }
      temp[included.length] = path;
      parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), temp);
    } else {
      parser.getParserParameters().getIncludedFilesHashtable().put(parser.currentFileName(), new String[] {path});
    }
  }

  /**
   * Constructs string representation of this element
   *
   * @return constructed string for this element
   */
  public String toString() {
    String ret = "";

    if (body != null) {
      ret += body;
    } else {
      ret += super.toString();
    }

    return ret;
  }

  public String getString(IDCounter id) throws JspParseException {
    // if included is JSP document => clear default namespace
    if( body.parser.isXml()) {
        JspElement rootBodyElement = (JspElement) body;
        if( rootBodyElement.getStartTag() instanceof RootTag ) {
          Element[] rootElements = null;
          if( rootBodyElement.getBody() instanceof JspElement ) {
            rootElements = new Element[] {rootBodyElement.getBody()};
          } else if (  rootBodyElement.getBody() instanceof ElementCollection) {
            rootElements = rootBodyElement.getBody().elements;
          }
         for (int i = 0; i < rootElements.length; i++) {
          if( rootElements[i] instanceof JspElement) {
            ((JspElement)rootElements[i]).getStartTag().clearDefaultnamespace();
          }
         }
        }else {
          rootBodyElement.getStartTag().clearDefaultnamespace();
        }
    }

    Element[] elements = body.elements;
    if (elements == null) {
      return body.getString(id);
    }
    String include = "";
    for (int i = 0; i < elements.length; i++) {
      if (elements[i] instanceof OutputCommentElement) {
        continue;
      }
      if (elements[i] instanceof JspTaglibDirective) {
        // xmlns already included in XML view
        // see PageDataImpl.extractTaglib
        continue;
      }
      include = include + elements[i].getString(id) + "\n";
    }

    return include;
  }

  private boolean isIncludeAllowed(String path) throws IOException {
    path = path.replace(File.separatorChar, ParseUtils.separatorChar);
    File f = new File(path);
    if (!f.exists() || !f.getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar).equals(path)) {
      return false;
    }
    // include only relative paths - no (absolute path) E:/test/test.jsp allowed
    // The new path is interpreted through the ServletContext object.
    // i.e. the resource should be inside the web application.
    String rootDir = parser.getRealPath("");
    String httpAliasPath = parser.getParserParameters().getHttpAliasValue();
    if( httpAliasPath != null ) {
      if( !path.startsWith(httpAliasPath)) {
        // most likely this is absolute path
        return false;
      }else {
        // this is JSP in HTTP alias
        return true;
      }
    }else if (!path.startsWith(rootDir)) {
      return false;
    }
    String relativeToRootPath = path.substring(rootDir.length()).toLowerCase();
    if (relativeToRootPath.startsWith("web-inf/")
        || relativeToRootPath.startsWith("/web-inf/")
        || relativeToRootPath.equals("web-inf")
        || relativeToRootPath.startsWith("meta-inf/")
        || relativeToRootPath.startsWith("/meta-inf/")
        || relativeToRootPath.equals("meta-inf")) {
      if (LogContext.getLocationRequestInfoServer().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning( "ASJ.web.000449",
					"Including a file from [META-INF] or [WEB-INF] directories in include directive. The file is: [{0}].", 
					new Object[]{relativeToRootPath}, null, null);
			}
    }
    return true;
  }

  /**
   * The directive name denoted by "directive" in the following syntax - <%@ directive { attr='value' }* %>
   * @return - String with one of the directive names - "page", "include", "taglib", "tag", "attribute", "variable" or some custom type
   */
  public String getDirectiveName(){
    return DIRECTIVE_NAME_INCLUDE;
  }

  /**
   * Subclass this method if you want to perform some validation of the usage of this directive.
   * Note the tree of element is still not created.
   * @param parser
   * @throws JspParseException - if for example page directive is used in tag file or tag directive in JSP page
   */
  public void verifyDirective(JspPageInterface parser) throws JspParseException{
    //do nothing - include directive can be used both in tag files and JSP pages
  }
}

