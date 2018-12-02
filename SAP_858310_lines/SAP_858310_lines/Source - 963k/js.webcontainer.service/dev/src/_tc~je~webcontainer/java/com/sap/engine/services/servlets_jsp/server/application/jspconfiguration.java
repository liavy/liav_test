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
package com.sap.engine.services.servlets_jsp.server.application;

import java.util.List;

import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;


/**
 * This interface is used for handling JSP configuration information element 
 * of a Web Application.
 * 
 * @author Diyan Yordanov
 * @version 7.0
 */
public interface JspConfiguration {  
  
 /**
  * Returns a property that best matches the supplied resource.
  * @param uri the resource supplied.
  * @return a JspProperty indicating the best match, or some default.
  */
  public JspConfigurationProperties getJspProperty(String uri);

  /**
   * Checks whether to activate EL expression evaluation or not for the suplied 
   * resource.
   * @param uri the resource supplied.
   * @return "true" when the EL evaluation must be deactivated for this resource,
   * "false" otherwise.
   */
  public boolean isELIgnored(String uri);
  
  /**
   * Checks whether scripting elements are not allowed for the supplied resource. 
   * @param uri the resource supplied.
   * @return "true" if scripting elements are disabled and "false" otherwise.
   */
  public boolean isScriptingInvalid(String uri);
  
  /**
   * Returns the page-encoding property for jsp pages denoted by the specified
   * uri parameter.
   * @param uri the resource supplied.
   * @return the page-encoding property for jsp pages the match the uri.
   */
  public String getPageEncoding(String uri);

  /**
   * Checks if the group of supplied resources are JSP documents.
   * @param uri the resource supplied. 
   * @return "true", if the group of resources that are represented by the uri
   * parameter are JSP documents, and thus must be interpreted as XML documents.
   * Returns "false" if the resources are assumed not to be JSP documents.
   */
  public String isXml(String uri);

  /**
   * Returns a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included (as in an include directive) at the 
   * beginning of the JSP page denoted by the supplied resource.
   * @param uri the resource supplied. 
   * @return a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included at the beginning of the JSP page.
   */
  public List<String> getIncludePreludes(String uri);
  
  /**
   * Returns a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included (as in an include directive) at the 
   * end of the JSP page denoted by the supplied resource.
   * @param uri the resource supplied.
   * @return a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included at the end of the JSP page.
   */
  public List<String> getIncludeCodas(String uri);
  
  /**
   * Checks whether the uri matches an url pattern in the jsp configuration
   * elements.  If so, then the uri is a JSP page.
   * @param uri the resource supplied.
   * @return "true" if the suplied resourse is a JSP page.
   */
  public boolean isJspPage(String uri);

  public boolean isDeferredSyntaxAllowedAsLiteral(String uri);
  
  public boolean isTrimDirectiveWhitespaces(String uri);
}