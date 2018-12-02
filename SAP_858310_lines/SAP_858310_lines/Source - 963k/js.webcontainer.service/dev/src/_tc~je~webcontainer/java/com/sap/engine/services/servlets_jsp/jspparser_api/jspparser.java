/*
 * Copyright (c) 2005-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.servlet.ServletRequest;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.tc.logging.Location;

/**
 * Instance of this class represents one type of the parser - portal or engine parser.
 * @author Todor Mollov, Bojidar Kadrev
 * @version 7.0
 *
  */
public class JspParser {
  private static Location currentLocation = Location.getLocation(JspParser.class);
	private ComponentDecorator parsable;
	private String parserName;
	private Element[] tagHandlers = null;
	
	/**
	 * Properties from the web container service. UPdated during startup of the application and when some of the properties are changed.
	 */
	private WebContainerParameters containerParameters;

  /**
   * Crreates parser instance with given name, handlers , component decorator and webcontainer properties
   * @param parserName the name that uniquely identifies this parser type.
   * @param handlers array with specific implementations of parsing elements
   * @param parsable  ComponentDecorator which will render the java file
   * @param containerParameters parameters of the WebContainer service, related to the parser
   */
	JspParser(String parserName, Element[] handlers, ComponentDecorator parsable, WebContainerParameters containerParameters) {
		this.tagHandlers = handlers;
		this.parsable = parsable;
		this.parserName = parserName;
		this.containerParameters = containerParameters;
	}
	
	/** //TODO: remove this method when portals synced.
	 * Parses given JSP file.
	 * @param file
	 * @param className - uses this classname for generated java file
	 * @param parserParameters
	 * @return
	 * @throws JspParseException
	 * @deprecated
	 */
	public ByteArrayOutputStream parse(
			File file,
			String className,
			ParserParameters parserParameters)
			throws JspParseException {
		return null;
	}
	

	/**
	 * Parses given JSP file.
	 * @param file - with absolute path to the JSP page to be parsed 
	 * @param portalProperties - transport for custom objects needed by some of the parsing elements. Can be null.
	 * @param request - Used to check if the request is made with http alias. Can be null.
	 * @return className - the classname of the generated classfile. 
	 * @throws JspParseException
	 */
	public String generateJspClass(
		File file,
		ConcurrentHashMapObjectObject portalProperties, 
		String applicationAlias,
		ServletRequest request)
		throws JspParseException {
    return generateJspClass(file, portalProperties, applicationAlias, null, null, false);
	}

  /**
	 * Parses given JSP file. Performes necessary checks.
	 * 1) has no class file
	 * 2) has class file but the JSP is newer
	 * 3) has included JSPs and they are newer
	 * @param file - with absolute path to the JSP page to be parsed
	 * @param portalProperties - transport for custom objects needed by some of the parsing elements. Can be null.
	 * @param applicationAlias - The alias for the application. For default application is "/".
	 * @param httpAliasName - the value of the http alias. For most cases this is equal to application alias. Can be null.
	 * @param httpAliasValue - the directory that maps to the given http alias. In most cases will be equal to the root directory of the application. Can be null
	 * @return className - the classname of the generated classfile.
	 * @param forceProcess - if true, then JSP processing is forced e.g. request parameter "jspPrecompile"
	 * @throws JspParseException
	 */
	public String generateJspClass(
		File file,
		ConcurrentHashMapObjectObject portalProperties,
		String applicationAlias,
		String httpAliasName,
		String httpAliasValue,
		boolean forceProcess)
		throws JspParseException {
    try{
      return generateJspClass(file.getCanonicalPath(), portalProperties, applicationAlias, httpAliasName, httpAliasValue, forceProcess);
    }catch (JspParseException jspParseException){
      throw jspParseException;
    }catch (Exception commonException){
      throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[]{file}, commonException);
    }
	}

  /**
	 * Parses given JSP file. Performes necessary checks.
	 * 1) has no class file
	 * 2) has class file but the JSP is newer
	 * 3) has included JSPs and they are newer
	 * @param jspFullPath - with absolute path to the JSP page to be parsed
	 * @param portalProperties - transport for custom objects needed by some of the parsing elements. Can be null.
	 * @param applicationAlias - The alias for the application. For default application is "/".
	 * @param httpAliasName - the value of the http alias. For most cases this is equal to application alias. Can be null.
	 * @param httpAliasValue - the directory that maps to the given http alias. In most cases will be equal to the root directory of the application. Can be null
	 * @return className - the classname of the generated classfile. 
	 * @param forceProcess - if true, then JSP processing is forced e.g. request parameter "jspPrecompile"
	 * @throws JspParseException
	 */
	public String generateJspClass(
		String jspFullPath,
		ConcurrentHashMapObjectObject portalProperties, 
		String applicationAlias,
		String httpAliasName,
		String httpAliasValue,
		boolean forceProcess)
		throws JspParseException {
	  ApplicationContext applicationContext = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(applicationAlias.getBytes()));
	  String className = null;
	  try {
		  jspFullPath = jspFullPath.replace(File.separatorChar, ParseUtils.separatorChar);
		  JSPProcessor processor = new JSPProcessor(applicationContext,this, portalProperties, httpAliasName, httpAliasValue);
		  boolean productionMode = getContainerParameters().isProductionMode();
		  className = applicationContext.getJspChecker().processJSPRequest(jspFullPath, httpAliasName, httpAliasValue, processor, forceProcess, productionMode);
    }catch (JspParseException jspParseException){
      throw jspParseException;
    }catch (Exception commonException){
      throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[]{jspFullPath}, commonException);
    }
    return className;
	}

  /**
	 * Returns parameters of the WebContainer service, related to the parser
	 * @return - The web container properties
	 */
  public WebContainerParameters getContainerParameters() {
    return containerParameters;
  }
  
  /**
   * ComponentDecorator implementation, which is responsible for java file genaration
   * @return - the decorator object
   */
  public ComponentDecorator getParsable() {
    return parsable;
  }
  
  /**
   * Returns the name that this parser instance is registered with
   * @return - The hardcoded parser name that this parser instance is registered with.
   */
  public String getParserName() {
    return parserName;
  }
  
  /**
   * Returns all registered parsing elements
   * @return - the tag handlers passed during registration of this parser instance.
   */
  public Element[] getTagHandlers() {
    return tagHandlers;
  }
}
