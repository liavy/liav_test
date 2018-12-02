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

import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagLibraries;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.TagVariableData;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;

import java.util.*;

import javax.servlet.jsp.tagext.TagLibraryInfo;

/*
 *
 * @author Maria Jurova
 * @version 4.0
 */
public class ParserAttributes {

  private int fragmentsCount = 0;
  private String pageClassName; //classname of the current jsp page
  private boolean pageActionTaken;
  private String pageImportAttribute;
  private Hashtable pageAttributes;
  private StringBuffer extendsDirective;
  private StringBuffer implemetsDirective;
  private StringBuffer importDirective;
  private StringBuffer declarationsCode;
  private StringBuffer scriptletsCode;
  private StringBuffer classBodyCode;
	private StringBuffer commentCode;
  private Vector tagMethodsCode;          //with StringBuffers
  private Vector tagFragmentMethodsCode;  //with StringBuffers
  private boolean createSession;
  private String contentType;
  private String pageEncoding;
  private String errorPage;
  private String servletInfo;
  private int bufferSize;
  private boolean autoFlush;
  private Hashtable beanId;
  private String path;
  /**
   * Holds all tag libraries for the given JSP. If there are included for both of them.
   */
  private LifoDuplicateMap<String, TagLibraryInfo> tagLibs;
  private boolean hasParam;
  private Hashtable allBeanNames;
  private boolean isInner;
  private Stack<TagVariableData> stack;
  private HashMap<String, Integer> tagVarNumbers;
  private TagLibraries tli;
  private boolean isErrorPage = false;
  private Hashtable jspAttributesCode;
  private int jspAttributeCounter = 0;
  private Hashtable jspBodyCode;
  private int jspBodyCounter = 0;
  private Hashtable functionsNames;
  private int functionCounter = 0;
  private StringBuffer functionsCode;
  private boolean hasJspRoot = false;
  private boolean omitXmlDeclarartion = true;
  private boolean isOmitXmlDeclarartionSet = false;
  private String doctypeRootElement = null;
  private String doctypeSystem = null;
  private String doctypePublic = null;
  private String jspBodyVariable = null;
  /**
   *  JSP 2.1 Table JSP.1-8 Page Directive Attributes
   * The default is buffered with an implementation buffer size of
   * not less than 8kb.
   */
  public static final int MIN_BUFFER_SIZE = 8 * 1024;
  /**
   *
   * Creates new ParserAttributes
   */
  public ParserAttributes() {
    pageImportAttribute = null;
    pageActionTaken = false;
    pageAttributes = new Hashtable();
    extendsDirective = new StringBuffer();
    implemetsDirective = new StringBuffer();
    importDirective = new StringBuffer();
    declarationsCode = new StringBuffer();
    scriptletsCode = new StringBuffer();
    classBodyCode = new StringBuffer();
    commentCode = new StringBuffer();
    createSession = true;
    contentType = null;//"text/html;charset=ISO-8859-1";
    errorPage = null;
    servletInfo = null;
    bufferSize = ServiceContext.getServiceContext().getWebContainerProperties().getServletOutputStreamBufferSize();
    if (bufferSize < MIN_BUFFER_SIZE) {
      bufferSize = MIN_BUFFER_SIZE;
    }
    autoFlush = true;
    beanId = new Hashtable();
    path = null;
    tagLibs = new LifoDuplicateMap<String, TagLibraryInfo>();
    hasParam = false;
    allBeanNames = new Hashtable();
    isInner = false;
    stack = new Stack<TagVariableData>();
    stack.push(new TagVariableData(null, null, false)); // create en entry for the root (considered as a tag)
    tagVarNumbers = new HashMap<String, Integer>();
    tli = new TagLibraries();
    tagMethodsCode = new Vector();
    tagFragmentMethodsCode = new Vector();
    isErrorPage = false;
    jspAttributesCode = new Hashtable();
    jspAttributeCounter = 0;
    jspBodyCode = new Hashtable();
    jspBodyCounter = 0;
    functionCounter = 0;
    functionsNames = new Hashtable();
    functionsCode = new StringBuffer();
    hasJspRoot = false;
    omitXmlDeclarartion = true;
    jspBodyVariable = null;
    doctypeRootElement = null;
    doctypeSystem = null;
    doctypePublic = null;
    isOmitXmlDeclarartionSet = false;
  }

  public Hashtable getAllBeanNames() {
    return this.allBeanNames;
  }

  public void setAllBeanNames(Hashtable allBeanNames) {
    this.allBeanNames = allBeanNames;
  }

  public boolean getAutoFlush() {
    return this.autoFlush;
  }

  public void setAutoFlush(boolean autoFlush) {
    this.autoFlush = autoFlush;
  }

  public Hashtable getBeanId() {
    return this.beanId;
  }

  public void setBeanId(Hashtable beanId) {
    this.beanId = beanId;
  }

  public int getBufferSize() {
    return this.bufferSize;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public String getContentType() {
    return this.contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getPageEncoding() {
    return this.pageEncoding;
  }

  public void setPageEncoding(String pageEncoding) {
    this.pageEncoding = pageEncoding;
  }

  public boolean getCreateSession() {
    return this.createSession;
  }

  public void setCreateSession(boolean createSession) {
    this.createSession = createSession;
  }

  public StringBuffer getDeclarationsCode() {
    return this.declarationsCode;
  }

  public void setDeclarationsCode(StringBuffer declarationsCode) {
    this.declarationsCode = declarationsCode;
  }

  public String getErrorPage() {
    return this.errorPage;
  }

  public void setErrorPage(String errorPage) {
    this.errorPage = errorPage;
  }


  public StringBuffer getExtendsDirective() {
    return this.extendsDirective;
  }

  public void setExtendsDirective(StringBuffer extendsDirective) {
    this.extendsDirective = extendsDirective;
  }

  public boolean getHasParam() {
    return this.hasParam;
  }

  public void setHasParam(boolean hasParam) {
    this.hasParam = hasParam;
  }

  public StringBuffer getImplemetsDirective() {
    return this.implemetsDirective;
  }

  public void setImplemetsDirective(StringBuffer implemetsDirective) {
    this.implemetsDirective = implemetsDirective;
  }

  public StringBuffer getImportDirective() {
    return this.importDirective;
  }

  public void setImportDirective(StringBuffer importDirective) {
    this.importDirective = importDirective;
  }
  
  public boolean getPageActionTaken() {
    return this.pageActionTaken;
  }

  public void setPageActionTaken(boolean pageActionTaken) {
    this.pageActionTaken = pageActionTaken;
  }

  public Hashtable getPageAttributes() {
    return this.pageAttributes;
  }

  public void setPageAttributes(Hashtable pageAttributes) {
    this.pageAttributes = pageAttributes;
  }

  public String getPageImportAttribute() {
    return this.pageImportAttribute;
  }

  public void setPageImportAttribute(String pageImportAttribute) {
    this.pageImportAttribute = pageImportAttribute;
  }

  public String getPath() {
    return this.path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public StringBuffer getScriptletsCode() {
    return this.scriptletsCode;
  }

  public void setScriptletsCode(StringBuffer scriptletsCode) {
    this.scriptletsCode = scriptletsCode;
  }

  public String getServletInfo() {
    return this.servletInfo;
  }

  public void setServletInfo(String servletInfo) {
    this.servletInfo = servletInfo;
  }
  
  /**
   * Map with linked map with all tag libraries used in the JSP.
   * Returns the same map as getTagLibs() but the return type is LifoDuplicateMap.
   * @deprecated 
   * @return
   */
  public LinkedHashMap getTaglibs() {
    return this.tagLibs;
  }
  
  /**
   * Map with linked map with all tag libraries used in the JSP
   * @return
   */
  public LifoDuplicateMap<String, TagLibraryInfo> getTagLibs(){
    return this.tagLibs;
  }
  
  public boolean isInner() {
    return this.isInner;
  }

  public void setInner(boolean isInner) {
    this.isInner = isInner;
  }

  public TagLibraries getTagLibraries() {
    return tli;
  }

  public Stack<TagVariableData> getTagsStack() {
    return stack;
  }

  public HashMap<String, Integer> getTagVarNumbers() {
    return tagVarNumbers;
  }

  public Vector getTagMethodsCode() {
    return this.tagMethodsCode;
  }

  public Vector getTagFragmentMethodsCode() {
    return this.tagFragmentMethodsCode;
  }

  public void setPageClassname(String classname) {
    this.pageClassName = classname;
  }

  public String getPageClassname() {
    return this.pageClassName;
  }

  public void setFragmentsCount(int count) {
    this.fragmentsCount = count;
  }

  public int getFragmentsCount() {
    return this.fragmentsCount;
  }

  public void setIsErrorPage(boolean isErrorPage) {
    this.isErrorPage = isErrorPage;
  }

  public boolean isErrorPage() {
    return isErrorPage;
  }

  public Hashtable getJspAttributesCode() {
    return jspAttributesCode;//tagVarNumbers;
  }

  public int getJspAttributeVarCount() {
    return jspAttributeCounter++;
  }

  public Hashtable getJspBodyCode() {
    return jspBodyCode;
  }

  public int getJspBodyVarCount() {
    return jspBodyCounter++;
  }

  public Hashtable getFunctionsNames() {
    return functionsNames;
  }

  public int getFunctionVarCount() {
    return functionCounter++;
  }

  public StringBuffer getFunctionsCode() {
    return functionsCode;
  }

  public boolean hasJspRoot() {
    return hasJspRoot;
  }

  public void setJspRoot(boolean flag) {
    hasJspRoot = flag;
  }

  public boolean isOmitXMLDeclaration() {
    return omitXmlDeclarartion;
  }

  public void setOmitXMLDeclaration(boolean omitXmlDeclarartion) {
    isOmitXmlDeclarartionSet = true;
    this.omitXmlDeclarartion = omitXmlDeclarartion;
  }

  public boolean isOmitXMLDeclarationSet() {
    return isOmitXmlDeclarartionSet;
  }
  /**
	 * This buffer holds the code that will be inserted in the body of the class
	 * the content is not strictly specified - could contain fileds, methods, inner classes ...
	 * but should include main entry method for generated from JSP servlet that will be invoke during runtime 
	 * from the container(web container, portal machinary , etc.)  
	 * @return StringBuffer
	 */
	public StringBuffer getClassBodyCode()
	{
		return classBodyCode;
	}


	/**
	 * returns the buffer for comments at the beggining of the generated java file
	 *  before package declaration  
	 * 
	 * @return StringBuffer
	 */
	public StringBuffer getCommentCode()
	{
		return commentCode;
	}

  public String getJspBodyVariable() {
    return jspBodyVariable;
  }

  public void setJspBodyVariable(String var) {
    jspBodyVariable = var;
  }

  public String getDoctypeRootElement() {
    return doctypeRootElement;
  }

  public void setDoctypeRootElement(String doctypeRootElement) {
    this.doctypeRootElement = doctypeRootElement;
  }

  public String getDoctypeSystem() {
    return doctypeSystem;
  }

  public void setDoctypeSystem(String doctypeSystem) {
    this.doctypeSystem = doctypeSystem;
  }

  public String getDoctypePublic() {
    return doctypePublic;
  }

  public void setDoctypePublic(String doctypePublic) {
    this.doctypePublic = doctypePublic;
  }


}

