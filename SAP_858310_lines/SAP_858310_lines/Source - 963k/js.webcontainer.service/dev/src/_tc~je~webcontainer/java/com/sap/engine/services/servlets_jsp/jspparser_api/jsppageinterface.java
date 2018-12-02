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
package com.sap.engine.services.servlets_jsp.jspparser_api;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.Vector;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Position;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagLibraries;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagVariableData;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;

/**
 *
 * Main interface of the parser. Represents instance of the parser that parses current JSP request.
 * @author Todor Mollov, Bojidar Kadrev
 *
 * @version 7.0
 */
public interface JspPageInterface
{
	/**
	 * Register handlers for the Element parsing
	 * @param el the Element implementation to be registered
	 * @throws JspParseException if an error occurs
	 */
	public void registerParsingElement(Element el) throws JspParseException;


	/**
	 * This buffer holds the code that will be inserted in the body of the class
	 * the content is not strictly specified - could contain fileds, methods, inner classes ...
	 * but should include main entry method for generated from JSP servlet that will be invoke during runtime
	 * from the container(web container, portal machinary , etc.)
	 * @return  StringBuffer
	 */
	public StringBuffer getClassBodyCode();

	/**
	 * Returns the buffer for comments at the beggining of the generated java file
	 *  before package declaration
	 *
	 * @return   StringBuffer
	 */
	public StringBuffer getCommentCode();

	/**
	 * generates the file and returns path to it.
	 * @param obj - unclear , some arguments may be necessary later
	 * @return

	public File generateJavaFile(Object obj);
*/
	public abstract ClassLoader getApplicationClassLoader();
	/**
	 *
	 * @return - hashmap with key: uri/tagdir and value:TagLibraryInfo
	 */
	public abstract ConcurrentHashMapObjectObject getTagLibDescriptors();

	/**
	* Returns the current position in the parsing file
	* @return int current position in the jsp source
	*/
	public abstract int currentPos();
	/**
	* Return current debug position in the jsp source
	*
	* @return current debug position in the jsp source
	*/
	public abstract Position currentDebugPos();
	/**
	* Returns the path of curently parsed jsp file
	*
	* @return file name of the jsp source file
	*/
	public abstract String currentFileName();
	/**
	* Returns  next char in the jsp source
	*
	* @return next char in the jsp source
	*/
	public abstract char nextChar();
	/**
	* Returns current char in the jsp source
	*
	* @return current char in the jsp source
	*/
	public abstract char currentChar();
	/**
	* Returns character for given position
	*
	* @param   pos index in the jsp source
	* @return  char at the specified position
	*/
	public abstract char charAt(int pos);

	/**
	* Takes parsing action to the specified token
	*
	* @param   symbols token used as end for parsing
	* @param   skip  denotes whether parser must skip
	* end token or stop parsing on it
	* @return   number of chars that are parsed
	* @exception   JspParseException  thrown if error ocures
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
	* @exception   JspParseException  thrown if error ocures
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
   * Compares a part of the jsp source with the specified chars.
   * If the end tag is of XML form whitespaces are ignored before the closng bracket. 
   * //   ETag ::=  '</' Name S? '>'
   * @param startPosition - start  start position for comparing in the jsp source
   * @param endIndent - symbols witch are compared with the jsp source, the closing tag - "%>", "</jsp:include>" or "</jsp:include >"
   * @return
   * @throws JspParseException
   */
  public boolean compareEndIndex(int startPosition, char[] endIndent) throws JspParseException;
  
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
	* Check for end of jsp source
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
	* @exception   JspParseException  thrown if error ocures
	* during parsing
	*/
	public abstract Element parse(File file) throws JspParseException;

	/**
	 * Parses jsp source and returns newly created element
	 *
	 * @return jsp element representing all jsp source
	 * @exception   JspParseException  thrown if error ocures
	 * during parsing
	 */
	public Element parse() throws JspParseException;

	/**
	* Returnes the root parsing element that will contain
	* all parsed elements
	*
	* @return  the root element for this parser
	*/
	public abstract Element getRootElement();
	/**
	* Transform a java bean attribute name into part ot its 'set'
	* or 'get' methods names
	*
	* @param   old  java bean attribute name
	* @return  the same string with first letter made to
	* upper case
	*/
	public abstract String firstCap(String old);

	/**
	* Escapes jsp script string
	*
	* @param   expr  script string
	* @return escaped script string
	*/
	public abstract String replaceScriptQuotes(String expr);
	/**
	* Escapes jsp template text string
	*
	* @param   expr  template text string
	* @return escaped template text string
	*/
	public abstract String replaceTemplateTextQuotes(String expr);
	/**
	* Escapes jsp attribute value string
	*
	* @param   expr  attribute value string
	* @return escaped attribute value string
	*/
	public abstract String replaceAttributeQuotes(String expr);

  /**
   * Getter for hashtable containing the names of all parsed beans
   * @return Hashtable with the names of all parsed beans
   */
  public abstract Hashtable getAllBeanNames();

  /**
   * Returns flag for page's buffer auto flush
   * @return boolean correspondign to the page buffer's attribute
   */
  public abstract boolean getAutoFlush();

  /**
   * Set the boolean flag for page buffer auto flush.
   * Usualy it is an attribute value of page directive
   * @param autoFlush
   */
  public abstract void setAutoFlush(boolean autoFlush);

  /**
   * Getter for hashtable containing parsed beans by IDs
   * @return  Hashtable containing parsed beans by IDs
   */
  public abstract Hashtable getBeanId();

  /**
   * Getter for the current buffer size of the page
   * @return int correspondign to the page buffer's attribute
   */
  public abstract int getBufferSize();

  /**
   * Set the value for buffer size - from page directive or from webcontainer property
   * It should be 8k at least
   * @param bufferSize in KB
   */
  public abstract void setBufferSize(int bufferSize);

  /**
   * Getter for the content type of generated response
   * @return content-type string
   */
  public abstract String getContentType();

  /**
   * Sets the content type for generated response
   * @param contentType  String in form of "type/subtype;charset=xxx"
   */
  public abstract void setContentType(String contentType);

  /**
   * Returns page encoding of the page
   * @return page encoding of the page
   */
  public abstract String getPageEncoding();

  /**
   * Returns flag indication session creation
   * @return boolean indicating session creation
   */
  public abstract boolean getCreateSession();

  /**
   * Sets flag indicating wheather to create implicit "session" object in the page.
   * @param createSession with value true if the implicit object should be created
   */
  public abstract void setCreateSession(boolean createSession);

  /**
   * Returns the buffer for decalaration code
   * @return StringBuffer for adding decalaration
   */
  public abstract StringBuffer getDeclarationsCode();

  /**
   * Sets the value for decalration buffer with already parsed declarations.
   * @param declarationsCode buufer to be set
   */
  public abstract void setDeclarationsCode(StringBuffer declarationsCode);

  /**
   * Returns error page path
   * @return path to the error page
   */
  public abstract String getErrorPage();

  /**
   * Sets the path to error page
   * @param path to the error page
   */
  public abstract void setErrorPage(String errorPage);

  /**
   * Retuns flag for ignoring EL expressions
   * @return boolean indication if EL will be ignored
   */
  public abstract boolean getIsELIgnored();

  /**
   * Sets flag for ignoring EL expressions
   * @param isELIgnored indication if EL will be ignored
   */
  public abstract void setIsELIgnored(boolean isELIgnored);

  /**
   * Returns the buffer for extends code
   * @return StringBuffer to hold extend code
   */
  public abstract StringBuffer getExtendsDirective();

  /**
   * Returns the buffer for implements code
   * @return StringBuffer to hold implements code
   */
  public abstract StringBuffer getImplemetsDirective();

  /**
   * Returns the buffer for import statements
   * @return StringBuffer to hold imports
   */
  public abstract StringBuffer getImportDirective();

  /**
   * Return flag indicating that the page directive is already processed
   * @return true if the page directive is already processed
   */
  public abstract boolean getPageActionTaken();

  /**
   * Sets flag indicating that the page directive is already processed
   * @param pageActionTaken flag indicating that the page directive is already processed
   */
  public abstract void setPageActionTaken(boolean pageActionTaken);

  /**
   * Returns Hastable with already parsed attributes of page directive(s)
   * @return Hastable with already parsed attributes of page directive(s)
   */
  public abstract Hashtable getPageAttributes();

  /**
   * Returns import statements string
   * @return String containg imports from page directive
   */
  public abstract String getPageImportAttribute();

  /**
   * Sets import statements string
   * @param pageImportAttribute String with imports
   */
  public abstract void setPageImportAttribute(String pageImportAttribute);
  /**
   * The path with File.separator.
   * @return - the real path to the JSP page to be parsed.
   */
	public abstract String getPath();

  /**
   * Sets path of curently parsed JSP file
   * @param path the full path to the JSP file to be parsed
   */
  public abstract void setPath(String path);

  /**
   * Returns the main code buffer, which contains the generated java code
   * @return StringBuffer containing the real code for generated JSP
   */
  public abstract StringBuffer getScriptletsCode();

  /**
   * Sets the buffer with curently generated java code
   * @param scriptletsCode presets with generated code for the JSP
   */
  public abstract void setScriptletsCode(StringBuffer scriptletsCode);

  /**
   * Returns server information string
   * @return String containiong server information
   */
  public abstract String getServletInfo();

  /**
   * Sets server information string
   * @param servletInfo containiong server information
   */
  public abstract void setServletInfo(String servletInfo);
	//  public Hashtable getTaglib() {
  /**
   * Sets the tag specific information
   * @param tagInfo  TagInfoImpl implementation
   */
  public abstract void setTagInfo(TagInfoImpl tagInfo);
	/**
	* Get the tag information in case this is a tag file
	* @return  TagInfoImpl
	*/
	public abstract TagInfoImpl getTagInfo();
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
	public abstract LinkedHashMap getTaglibs();

  /**
   * Sets flag indicating that currently parsed code is in bean tag
   * @param isInner parameter indicating whether currently parsed code is in bean tag 
   */
  public abstract void setInner(boolean isInner);

  /**
   * Checks if the parser is currently parsing a bean tag
   * @return boolean indicating whether currently parsed code is in bean tag
   */
  public abstract boolean isInner();
	/**
	* Get the stack of all nested tags that are parents of this one
	* The root is also considered as a tag and is therefore included -
	* i.e. the root is at the bottom of the stack
	* @return the tags stack
	*/
	public abstract Stack<TagVariableData> getTagsStack();

  /**
   * Hashmap containig the  consecutively  genarated names for tag variables
   * @return Hashmap containig the  consecutively  genarated names for tag variables
   */
  public abstract HashMap<String, Integer> getTagVarNumbers();

  /**
   * Returns  container for all tag libraries that have been imported using
   * the taglib directive.
   * @return   TagLibraries object containing chache with tag libraries for the JSP
   */
  public abstract TagLibraries getTagLibraries();

  /**
   * Returns genarated methods for tags
   * @return Vector containing code for generated methods
   */
  public abstract Vector getTagMethodsCode();
	/**
	* Get the methods that should be generated inside the Fragment Helper class
	* @return a Vector containing the methods code, including the method
	*         declaration, code and closing bracket
	*/
	public abstract Vector getTagFragmentMethodsCode();

  /**
   * Returns class name of the page
   * @return String with name of the genarated class
   */
  public abstract String getPageClassname();

  /**
   * Sets a class name of generated java file for curently parsed page
   * @param pageClassname name of the genarated class
   */
  public abstract void setPageClassname(String pageClassname);

  /**
   * Return the last number value for fragments
   * @return number of last genarated fragment variable
   */
  public abstract int getFragmentsCount();

  /**
   * Sets the counter for fragments with the latest value that is used
   * @param count the new value of the counter
   */
  public abstract void setFragmentsCount(int count);

  /**
   * Returns flag indicating if the current page is in XML syntax
   * @return true if it is in XML syntax, afls eif it is in standart
   */
  public abstract boolean isXml();
  /**
   * Returns flag indicating if the current page is declared as error page
   * @return true if the current page is declared as error page
   */
  public abstract boolean isErrorPage();

  /**
   * Sets flag indicating if the current page is declared as error page
   * @param isErrorPage flag indicating whather the current page is an error page
   */
  public abstract void setIsErrorPage(boolean isErrorPage);

  /**
   * Utitlity method used to get the absolute pathname of the given path
   * @param path relative path
   * @return String containing the real full path
   */
  public abstract String getRealPath(String path);

  /**
   * Hashtable containing buffers with generated code for jsp:attributes
   * @return Hashtable containing buffers with generated code for jsp:attributes
   */
  public abstract Hashtable getJspAttributesCode();

  /**
   * Returns auto incremented number for jsp:attributes variables
   * @return number for  jsp:attributes variables
   */
  public abstract int getJspAttributeVarCount();

  /**
   * Returns auto incremented number for function variables
   * @return  number for  function variables
   */
  public abstract int getFunctionVarCount();

  /**
   * Hashtable containing buffers with generated code for jsp:body
   * @return Hashtable containing buffers with generated code for jsp:body
   */
  public abstract Hashtable getJspBodyCode();

  /**
   * Returns auto incremented number for jsp:body variables
   * @return number for jsp:body variables
   */
  public abstract int getJspBodyVarCount();

  /**
   * Retunrs Hastable containing parsed functions and corresponding function mappers
   * @return Hastable containing parsed functions and corresponding function mappers
   */
  public abstract Hashtable getFunctionsNames();

  /**
   * Buffer containing the generated code for functions.
   * @return StringBuffer containing the generated code for functions
   */
  public abstract StringBuffer getFunctionsCode();

  /**
   * Sets flag for curent page if it is a tag file
   * @param isTagFile flag indicating whether the current page is tag file
   */
  public abstract void setIsTagFile(boolean isTagFile);

  /**
   * Flag indicating whether the curent file is tag file
   * @return true if it is a tag file, false if it is JSP
   */
  public abstract boolean isTagFile();

  /**
   * Sets flag indicating that the curently parsed file has jsp:root element
   * @param flag indicating that the curently parsed file has jsp:root element
   */
  public abstract void setJspRoot(boolean flag);

  /**
   * Flag indicating whether the parsed file has jsp:root element
   * @return true if there is jsp:root in the JSP
   */
  public abstract boolean hasJspRoot();

  /**
   * If there is no explicitely set the charset for jsp page, this flag returns true,
   * otherwise - false
   * @return  true  if there is no explicitely set the charset for jsp page, otherwise - false
   */
  public abstract boolean isDefaultEncodingUsed();

  /**
   * Sets the name of curently parsed jsp:body in order to indicate nested elements
   * @param variableName the name of variable
   */
  public abstract void setJspBodyVariable(String variableName);

  /**
   * Returns the name of jsp:body variable
   * @return the name of variable
   */
  public abstract String getJspBodyVariable();

	/**
   * Hashmap containig tag library validators for correspondigs TLDs
	 * @return Hashmap containig tag library validators for correspondigs TLDs
	 */
	public abstract ConcurrentHashMapObjectObject getTagLibraryValidators();
	/**
	 * Returns parameters for this Parser
	 * @return ParserParameters assosiated with this Parser, set during creation of the parser
	 */
	public abstract ParserParameters getParserParameters();

  /**
   * Returns jsp congiguration properties, declared in the web descriptor
   * @return JspConfigurationProperties object represent the congiguration properties
   */
  public abstract JspConfigurationProperties getJspProperty();

  /**
   * Returns the already parsed tree of elements
   * @return Element containing the parsed elements
   */
  public Element getParsed();

	
	/**
	 * returns part of the webcontaner properties together with some environment data like serverID and service classloader 
	 * @return  WebContainerParameters object
	 */
	public WebContainerParameters getWebContainerParameters();
	
  /**
   * returns number of the character this JSP page contains 
   * @return -1 if no body of JSP page is found
   */
  public int getSourceLength();


  /**
    * Generates new unique for this page jspId that serves for the purpose of the tag handlers
    * which implement javax.servlet.jsp.tagext.JspIdConsumer.
    * @return - unique id for this JSP page.
    */
  public String getNextTagID();

  /**
   * As of JSP 2.1, the character sequence #{ is reserved for EL expressions and translation error should be thrown
   * if it is used as a String literal.
   * This flag can be set using page/tag directive or setting tag in web.xml.
   * @return boolean indicating whether defferred EL are alowed
   */
  public abstract boolean isDeferredSyntaxAllowedAsLiteral();

  /**
   * This flag can be used to indicate that template text containing only whitespaces
   * must be removed from the response output.
   * @return boolean indicating whether trimming of WhiteSpaces are allowed
   */
  public abstract boolean isTrimDirectiveWhitespaces();

  /**
   * Sets DeferredSyntaxAllowedAsLiteral with boolean value.
   * @param flag indicating whether defferred EL are alowed
   */
  public abstract void setDeferredSyntaxAllowedAsLiteral(boolean flag);

  /**
   * Sets TrimDirectiveWhitespaces with boolean value.
   * @param flag indicating whether trimming of WhiteSpaces are allowed
   */
  public abstract void setTrimDirectiveWhitespaces(boolean flag);
  
  /**
   * If current file is tag file this will contain the implicit.tld 
   * or the TLD where the tag-file is described.
   * @return  TagLibDescriptor object
   */
  public TagLibDescriptor getTagFileTLD();
  
  /**
   * If current file is tag file this will contain the implicit.tld 
   * or the TLD where the tag-file is described.
   * @param tagFileTLD  TagLibDescriptor object
   */
  public void setTagFileTLD(TagLibDescriptor tagFileTLD);

  /**
   * Returns curently set flag for XML declaration
   * @return true if it has to be ommited, otherwise - false
   */
  public abstract boolean isOmitXMLDeclaration();

  /**
   * Sets flag for omitting XML declaration in generated output
   * @param omit indicating whether to omit XML declaration
   */
  public abstract void setOmitXMLDeclaration(boolean omit);

  /**
   * Checks if the flag for omitXMLDeclaration is set by calling setter method
   * @return  true if it is set, false in it is no called setOmitXMLDeclaration method.
   */
  public abstract boolean isOmitXMLDeclarationSet();

  /**
   * Returns Doctype root element of XML declaration in generated output
   * @return  String with Doctype root element
   */
  public abstract String getDoctypeRootElement();

  /**
   * Sets Doctype root element of XML declaration in generated output
   * @param doctypeRootElement String containing Doctype root element
   */
  public abstract void setDoctypeRootElement(String doctypeRootElement);

  /**
   * Returns Doctype system element of XML declaration in generated output
   * @return  String  Doctype system element
   */
  public abstract String getDoctypeSystem();

  /**
   * Sets Doctype system element of XML declaration in generated output
   * @param doctypeSystem Doctype system element
   */
  public abstract void setDoctypeSystem(String doctypeSystem);

  /**
   * Returns Doctype public element of XML declaration in generated output
    * @return  String Doctype public element
   */
  public abstract String getDoctypePublic();

  /**
   * Sets Doctype public element of XML declaration in generated output
   * @param doctypePublic Doctype public element
   */
  public abstract void setDoctypePublic(String doctypePublic);
}
