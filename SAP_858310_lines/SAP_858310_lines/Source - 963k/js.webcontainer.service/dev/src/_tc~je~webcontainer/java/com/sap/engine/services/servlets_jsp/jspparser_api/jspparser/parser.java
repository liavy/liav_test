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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.Vector;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.ParserParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.ElementCollection;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagLibraries;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagVariableData;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;

/*
 *
 * @author Ivo Simeonov
 * @version 4.0
 */
public abstract class Parser implements JspPageInterface {


//  protected ClassLoader serviceClassLoader = null;
  protected ConcurrentHashMapObjectObject tagLibDescriptors = null;
  protected ParserAttributes shareAttributes = new ParserAttributes();
  protected int lineNr;
  protected int srcPos;
  protected String src = null;
  protected boolean parsingHtml = false;
  protected Vector begs = new Vector();
  protected Element rootElement = new ElementCollection();
  protected boolean isTagFile = false;
  // Container for the tag information in case of a tag file
  protected TagInfoImpl tagInfo = null;
  private TagLibDescriptor tagFileTLD = null;
  
	//properties taken from appplication and service context
	protected ParserParameters parserProperties;
  private boolean isELIgnored = false;
  private boolean deferredSyntaxAllowedAsLiteral = false;
  private boolean trimDirectiveWhitespaces = false;

  public Parser() {
	}

	/**
	 * creates new parser with parent p
	 *
	 * @param   p parser to use as parent
	 */
	public Parser(Parser p) {
		this.parserProperties = p.getParserParameters();
		src = p.src;
		parsingHtml = p.parsingHtml;
		begs = p.begs;
		rootElement = p.rootElement;
		this.shareAttributes = p.shareAttributes;
		this.isTagFile = p.isTagFile;
        setTagFileTLD(p.getTagFileTLD());
	}

  public ClassLoader getApplicationClassLoader() {
    return parserProperties.getAppClassLoader();
  }



//  public ClassLoader getServiceClassLoader() {
//    return serviceClassLoader;
//  }


  public ConcurrentHashMapObjectObject getTagLibDescriptors() {
    return tagLibDescriptors;
  }

  /**
   *
   * @return current position in the jsp source
   */
  public abstract int currentPos();

  /**
   *
   *
   * @return current debug position in the jsp source
   */
  public abstract Position currentDebugPos();

  /**
   *
   *
   * @return file name of the jsp source
   */
  public abstract String currentFileName();

  /**
   *
   *
   * @return next char in the jsp source
   */
  public abstract char nextChar();

  /**
   *
   *
   * @return current char in the jsp source
   */
  public abstract char currentChar();

  /**
   *
   *
   * @param   pos index in the jsp source
   * @return  char at the specified position
   */
  public abstract char charAt(int pos);

  /**
   * Takes parsing action to the specified token
   *
   * @param   symbol token used as end for parsing
   * @param   skip  denotes whether parser must skip
   * end token or stop parsing on it
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public abstract int parseTo(char symbol, boolean skip) throws JspParseException;

  /**
   * Takes parsing action to the specified token
   *
   * @param   symbols token used as end for parsing
   * @param   skip  denotes whether parser must skip
   * end token or stop parsing on it
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public abstract int parseTo(char[] symbols, boolean skip) throws JspParseException;

  /**
   * Skips current all current chars that are white
   * spaces
   *
   * @return   number of chars that are parsed
   */
  public abstract int skipWhiteSpace();

  /**
   * Skips current all current chars that are part of java
   * indentifier
   *
   * @return   number of chars that are parsed
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public abstract int skipIndentifier() throws JspParseException;

  /**
   * Compares a part of the jsp source with the specified
   * chars
   *
   * @param   start  start position for comparing in the
   * jsp source
   * @param   end  end position for comparing in the
   * jsp source
   * @param   symbols symbols witch are compared with
   * the jsp source
   * @return  true if chars are equal
   */
  public abstract boolean compare(int start, int end, char[] symbols) throws JspParseException;

  /**
   * Compares a part of the jsp source with the specified
   * chars, ignoring case differences.
   *
   * @param   start  start position for comparing in the
   * jsp source
   * @param   end  end position for comparing in the
   * jsp source
   * @param   symbols symbols witch are compared with
   * the jsp source
   * @return  true if chars are equal
   */
  public abstract boolean compareIgnoreCase(int start, int end, char[] symbols) throws JspParseException;


  /**
   *
   *
   * @return  true if end of the jsp source is reached
   */
  public abstract boolean endReached();

  /**
   * String representation of part of the jsp source
   *
   * @param   start  start index for the part in the
   * jsp source
   * @param   end  end index for the part in the
   * jsp source
   * @return  string representation of the portion
   */
  public abstract String getChars(int start, int end);

  /**
   * Trimmed string representation of part of the jsp source
   *
   * @param   start  start index for the part in the
   * jsp source
   * @param   end  end index for the part in the
   * jsp source
   * @return  trimmed string representation of the portion
   */
  public abstract String getCharsTrim(int start, int end);

  /**
   * Parses specified file
   *
   * @param   file  file to parse
   * @return   Element that represents all jsp source
   * @exception   JspParseException  thrown if error occurs
   * during parsing
   */
  public abstract Element parse(File file) throws JspParseException;



  /**
   * Returnes the root parsing element that will contain
   * all parsed elements
   *
   * @return  the root element for this parser
   */
  public Element getRootElement() {
    return rootElement;
  }

  /**
   * Transform a java bean attribute name into part ot its 'set'
   * or 'get' methods names
   *
   * @param   old  java bean attribute name
   * @return  the same string with first letter made to
   * upper case
   */
  public String firstCap(String old) {
    String firstL = old.substring(0, 1).toUpperCase();
    String newStr = firstL + old.substring(1);
    return newStr;
  }



  /**
   * Escapes jsp script string
   *
   * @param   expr  script string
   * @return escaped script string
   */
  public String replaceScriptQuotes(String expr) {
    int pos = -1;
    String temp;

    while ((pos = expr.indexOf("%\\>")) != -1) {
      temp = expr.substring(0, pos + 1);
      expr = temp + expr.substring(pos + 2);
    }

    return expr;
  }

  /**
   * Escapes jsp template text string
   *
   * @param   expr  template text string
   * @return escaped template text string
   */
  public String replaceTemplateTextQuotes(String expr) {
    int pos = -1;
    String temp;

    while ((pos = expr.indexOf("<\\%")) != -1) {
      temp = expr.substring(0, pos + 1);
      expr = temp + expr.substring(pos + 2);
    }

    return expr;
  }

  /**
   * Escapes jsp attribute value string
   *
   * @param   expr  attribute value string
   * @return escaped attribute value string
   */
  public String replaceAttributeQuotes(String expr) {
    int pos = -1;
    String temp;

    while ((pos = expr.indexOf("\\\"")) != -1) {
      temp = expr.substring(0, pos);
      expr = temp + expr.substring(pos + 1);
    }

    while ((pos = expr.indexOf("\\'")) != -1) {
      temp = expr.substring(0, pos);
      expr = temp + expr.substring(pos + 1);
    }

    while ((pos = expr.indexOf("\\\\")) != -1) {
      temp = expr.substring(0, pos);
      expr = temp + expr.substring(pos + 1);
    }

    while ((pos = expr.indexOf("<\\%")) != -1) {
      temp = expr.substring(0, pos + 1);
      expr = temp + expr.substring(pos + 2);
    }

    while ((pos = expr.indexOf("%\\>")) != -1) {
      temp = expr.substring(0, pos + 1);
      expr = temp + expr.substring(pos + 2);
    }

    return expr;
  }

  public Hashtable getAllBeanNames() {
    return shareAttributes.getAllBeanNames();
  }
  public boolean getAutoFlush() {
    return shareAttributes.getAutoFlush();
  }

  public void setAutoFlush(boolean autoFlush) {
    shareAttributes.setAutoFlush(autoFlush);
  }

  public Hashtable getBeanId() {
    return shareAttributes.getBeanId();
  }
  public int getBufferSize() {
    return shareAttributes.getBufferSize();
  }

  public void setBufferSize(int bufferSize) {
    shareAttributes.setBufferSize(bufferSize);
  }

  public String getContentType() {
    return shareAttributes.getContentType();
  }

  public void setContentType(String contentType) {
    shareAttributes.setContentType(contentType);
  }

  public String getPageEncoding() {
    return shareAttributes.getPageEncoding();
  }

  public void setPageEncoding(String pageEncoding) {
    shareAttributes.setPageEncoding(pageEncoding);
  }

  public boolean getCreateSession() {
    return shareAttributes.getCreateSession();
  }

  public void setCreateSession(boolean createSession) {
    shareAttributes.setCreateSession(createSession);
  }

  public StringBuffer getDeclarationsCode() {
    return shareAttributes.getDeclarationsCode();
  }

  public void setDeclarationsCode(StringBuffer declarationsCode) {
    shareAttributes.setDeclarationsCode(declarationsCode);
  }

  public String getErrorPage() {
    return shareAttributes.getErrorPage();
  }

  public void setErrorPage(String errorPage) {
    shareAttributes.setErrorPage(errorPage);
  }

  public boolean getIsELIgnored() {
    return isELIgnored;
  }

  public void setIsELIgnored(boolean isELIgnored) {
   this.isELIgnored = isELIgnored;
  }

  public StringBuffer getExtendsDirective() {
    return shareAttributes.getExtendsDirective();
  }

  public StringBuffer getImplemetsDirective() {
    return shareAttributes.getImplemetsDirective();
  }

  public StringBuffer getImportDirective() {
    return shareAttributes.getImportDirective();
  }

  public boolean getPageActionTaken() {
    return shareAttributes.getPageActionTaken();
  }

  public void setPageActionTaken(boolean pageActionTaken) {
    shareAttributes.setPageActionTaken(pageActionTaken);
  }

  public Hashtable getPageAttributes() {
    return shareAttributes.getPageAttributes();
  }

  public String getPageImportAttribute() {
    return shareAttributes.getPageImportAttribute();
  }

  public void setPageImportAttribute(String pageImportAttribute) {
    shareAttributes.setPageImportAttribute(pageImportAttribute);
  }

  public String getPath() {
    return shareAttributes.getPath();
  }

  public void setPath(String path) {
    shareAttributes.setPath(path);
  }

  public StringBuffer getScriptletsCode() {
    return shareAttributes.getScriptletsCode();
  }

  public void setScriptletsCode(StringBuffer scriptletsCode) {
    shareAttributes.setScriptletsCode(scriptletsCode);
  }
	/**
	 * This buffer holds the code that will be inserted in the body of the class
	 * the content is not strictly specified - could contain fields, methods, inner classes ...
	 * but should include main entry method for generated from JSP servlet that will be invoke during runtime 
	 * from the container(web container, portal machinary , etc.)  
	 * @return
	 */
	public StringBuffer getClassBodyCode()
	{
		return shareAttributes.getClassBodyCode();
	}
	
	/**
	 * returns the buffer for comments at the beggining of the generated java file
	 *  before package declaration  
	 * 
	 * @return 
	 */
	public StringBuffer getCommentCode()
	{
		return shareAttributes.getCommentCode();
	}

  public String getServletInfo() {
    return shareAttributes.getServletInfo();
  }

  public void setServletInfo(String servletInfo) {
    shareAttributes.setServletInfo(servletInfo);
  }

  public void setTagInfo(TagInfoImpl tagInfo) {
    this.tagInfo = tagInfo;
  }

  /**
   * Get the tag information in case this is a tag file
   * @return
   */
  public TagInfoImpl getTagInfo() {
    return tagInfo;
  }

  /**
   * Get the tag libraries for this JSP as a hashtable
   * the key is the prefix
   * the content is the TagLibraryInfo
   *
   * <p>Aug 08, 2004 mladen-m
   * Changed to LinkedHashMap to preserve the order in which the &lt;taglib&gt;
   * directives appear in the JSP page.<br>
   * Used to be a ConcurrentHashMapObjectObject, but since no concurrent threads
   * will access this hashtable, there's no need for synchronization<br>
   * This object is used only by the Parser, which is bound to the current JSP
   * being parsed.
   *
   * <p>Mandatory for calling the taglib validator class, is present.
   * The taglibs must be validate in the order they are present in the JSP
   *
   * <p>see: JSP.13.9, page 2-100
   * @return
   */
  public LinkedHashMap getTaglibs() {
    return shareAttributes.getTaglibs();
  }

  public void setInner(boolean isInner) {
    shareAttributes.setInner(isInner);
  }

  public boolean isInner() {
    return shareAttributes.isInner();
  }

  /**
   * Get the stack of all nested tags that are parents of this one
   * The root is also considered as a tag and is therefore included -
   * i.e. the root is at the bottom of the stack
   * @return the tags stack
   */
  public Stack<TagVariableData> getTagsStack() {
    return shareAttributes.getTagsStack();
  }

  public HashMap<String, Integer> getTagVarNumbers() {
    return shareAttributes.getTagVarNumbers();
  }

  public TagLibraries getTagLibraries() {
    return shareAttributes.getTagLibraries();
  }

  public Vector getTagMethodsCode() {
    return shareAttributes.getTagMethodsCode();
  }

  /**
   * Get the methods that should be generated inside the Fragment Helper class
   * @return a Vector containing the methods code, including the method
   *         declaration, code and closing bracket
   */
  public Vector getTagFragmentMethodsCode() {
    return shareAttributes.getTagFragmentMethodsCode();
  }

  public String getPageClassname() {
    return shareAttributes.getPageClassname();
  }

  public void setPageClassname(String pageClassname) {
    shareAttributes.setPageClassname(pageClassname);
  }

  public int getFragmentsCount() {
    return shareAttributes.getFragmentsCount();
  }

  public void setFragmentsCount(int count) {
    shareAttributes.setFragmentsCount(count);
  }

  public boolean isErrorPage() {
    return shareAttributes.isErrorPage();
  }

  public void setIsErrorPage(boolean isErrorPage) {
    shareAttributes.setIsErrorPage(isErrorPage);
  }

  public String getRealPath(String path) {
    if (path == null) {
      return null;
    }
    String realPath1 = null;
    String tDir = new File(parserProperties.getApplicationRootDir()).getAbsolutePath() + File.separator;
    realPath1 = (tDir + (path.startsWith("/") ? path.substring(1) : path));
    realPath1 = realPath1.replace(ParseUtils.separatorChar, File.separatorChar);
    if (realPath1.endsWith(File.separator)) {
      return ((new File(realPath1)).getAbsolutePath() + File.separator).replace(File.separatorChar, ParseUtils.separatorChar);
    } else {
      return (new File(realPath1)).getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar);
    }
  }

  public Hashtable getJspAttributesCode() {
    return shareAttributes.getJspAttributesCode();
  }

  public int getJspAttributeVarCount() {
    return shareAttributes.getJspAttributeVarCount();
  }

  public int getFunctionVarCount() {
    return shareAttributes.getFunctionVarCount();
  }

  public Hashtable getJspBodyCode() {
    return shareAttributes.getJspBodyCode();
  }

  public int getJspBodyVarCount() {
    return shareAttributes.getJspBodyVarCount();
  }

  public Hashtable getFunctionsNames() {
    return shareAttributes.getFunctionsNames();
  }

  public StringBuffer getFunctionsCode() {
    return shareAttributes.getFunctionsCode();
  }

  public void setIsTagFile(boolean isTagFile) {
    this.isTagFile = isTagFile;
  }

  public boolean isTagFile() {
    return isTagFile;
  }

	public boolean hasJspRoot() {
    return shareAttributes.hasJspRoot();
  }

  public void setJspRoot(boolean flag) {
    shareAttributes.setJspRoot(flag);
  }

  public boolean isOmitXMLDeclaration() {
    return shareAttributes.isOmitXMLDeclaration();
  }

  public void setOmitXMLDeclaration(boolean flag) {
    shareAttributes.setOmitXMLDeclaration(flag);
  }

   public boolean isOmitXMLDeclarationSet() {
    return shareAttributes.isOmitXMLDeclarationSet();
  }

  /**
	 * @return
	 */
	public ConcurrentHashMapObjectObject getTagLibraryValidators() {
		return parserProperties.getTagLibraryValidators();
	}
	
	/**
	 * 
	 * @return ParserParameters assosiated with this Parser, set during creation of the parser
	 */
	public ParserParameters getParserParameters()
	{
		return parserProperties;
	}
	
	public abstract JspConfigurationProperties getJspProperty();

  public boolean isDeferredSyntaxAllowedAsLiteral() {
    return deferredSyntaxAllowedAsLiteral;
  }

  public void setDeferredSyntaxAllowedAsLiteral(boolean flag) {
    deferredSyntaxAllowedAsLiteral = flag;
  }
  public boolean isTrimDirectiveWhitespaces() {
    return trimDirectiveWhitespaces;
  }

  public void setTrimDirectiveWhitespaces(boolean flag) {
    trimDirectiveWhitespaces = flag;
  }

  public String getJspBodyVariable() {
    return shareAttributes.getJspBodyVariable();
  }

  public void setJspBodyVariable(String var) {
    shareAttributes.setJspBodyVariable(var);
  }
  
  /**
   * If current file is tag file this will contain the implicit.tld 
   * or the TLD where the tag-file is described.
   * @return
   */
  public TagLibDescriptor getTagFileTLD() {
    return tagFileTLD;
  }
  
  /**
   * If current file is tag file this will contain the implicit.tld 
   * or the TLD where the tag-file is described.
   * @param tagFileTLD
   */
  public void setTagFileTLD(TagLibDescriptor tagFileTLD) {
    this.tagFileTLD = tagFileTLD;
  }
public String getDoctypeRootElement() {
    return shareAttributes.getDoctypeRootElement();
  }

  public void setDoctypeRootElement(String doctypeRootElement) {
    shareAttributes.setDoctypeRootElement(doctypeRootElement);
  }

  public String getDoctypeSystem() {
    return shareAttributes.getDoctypeSystem();
  }

  public void setDoctypeSystem(String doctypeSystem) {
    shareAttributes.setDoctypeSystem(doctypeSystem);
  }

  public String getDoctypePublic() {
    return shareAttributes.getDoctypePublic();
  }

  public void setDoctypePublic(String doctypePublic) {
    shareAttributes.setDoctypePublic(doctypePublic);
  }

}

