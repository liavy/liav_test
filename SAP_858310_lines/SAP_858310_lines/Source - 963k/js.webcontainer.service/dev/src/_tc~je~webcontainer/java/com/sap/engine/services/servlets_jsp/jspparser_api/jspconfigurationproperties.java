/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import java.util.List;

/**
 * This class represents the JSP configuration properties defined in jsp-config
 * element WEB-INF/web.xml for a specific url pattern.
 * 
 * @author Diyan Yordanov
 * @version 7.0
 */
public class JspConfigurationProperties  {

  private String isXml = null;
  private String elIgnored = null;
  private String scriptingInvalid = null;
  private String pageEncoding = null;
  private List<String> includePrelude = null;
  private List<String> includeCoda = null;


  //JSP2.1
  private String deferredSyntaxAllowedAsLiteral = null;
  private String trimDirectiveWhitespaces = null;
  private String specVersion = null;
  

  
  /**
   * Creates a new JSP property element. 
   * @param isXml if true, then the resourse for which this property is refered
   * is a JSP document.
   * @param elIgnored if true, the EL evaluation is ignored.
   * @param scriptingInvalid if true, the scripting elements are disabled.
   * @param pageEncoding defines the page encoding.
   * @param includePrelude list of relative paths to Web Application element to
   * be included at the begining of the jsp page.
   * @param includeCoda list of relative paths to Web Application element to
   * be included at the end of the jsp page.
   * @param deferredSyntaxAllowedAsLiteral - from web.xml 
   * @param trimDirectiveWhitespaces - from web.xml
   * @param specVersion - the value of the <spec-version> tag in the web-j2ee.engine.xml or 
   * if not present the version of the web.xml or if not found "2.5"
   * @param metaDataComplete - from web.xml
   */
  public JspConfigurationProperties(String isXml, String elIgnored, String scriptingInvalid,
                                    String pageEncoding, List<String> includePrelude, List<String> includeCoda, 
                                    String deferredSyntaxAllowedAsLiteral, String trimDirectiveWhitespaces, String specVersion) {
      this.isXml = isXml;
      this.elIgnored = elIgnored;
      this.scriptingInvalid = scriptingInvalid;
      this.pageEncoding = pageEncoding;
      this.includePrelude = includePrelude;
      this.includeCoda = includeCoda;
      this.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
      this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
      this.specVersion = specVersion;
  }

  /**
   * Returns the value of "is-xml" property.
   * @return the value of "is-xml" property.
   */
  public String isXml() {
    return isXml;
  }

  /**
   * Returns the value of "el-ignored" property.
   * @return the value of "el-ignored" property.
   */
  public String isELIgnored() {
    return elIgnored;
  }

  /**
   * Returns the value of "scripting-invalid" property.
   * @return the value of "scripting-invalid" property.
   */
  public String isScriptingInvalid() {
    return scriptingInvalid;
  }

  /**
   * Returns the value of "page-encoding" property.
   * @return the value of "page-encoding" property.
   */
  public String getPageEncoding() {
    return pageEncoding;
  }

  /**
   * Returns a list of all "include-prelude" properties.
   * @return a list of all "include-prelude" properties.
   */
  public List<String> getIncludePrelude() {
    return includePrelude;
  }

  /**
   * Returns a list of all "include-coda" properties.
   * @return a list of all "include-coda" properties.
   */
  public List<String> getIncludeCoda() {
    return includeCoda;
  }

  /**
   * Returns if getSpecificationVersion().compareTo("2.3") <= 0.
   * @return the value of isOldApplication.
   */
  public boolean isOldApplication() {
    return specVersion.compareTo("2.3") <= 0;
  }

  /**
   * Returns the value of deferredSyntaxAllowedAsLiteral.
   * @return the value of deferredSyntaxAllowedAsLiteral.
   */
  public String isDeferredSyntaxAllowedAsLiteral() {
    return deferredSyntaxAllowedAsLiteral;
  }

  /**
   * Returns the value of trimDirectiveWhitespaces.
   * @return the value of trimDirectiveWhitespaces.
   */
  public String isTrimDirectiveWhitespaces() {
    return trimDirectiveWhitespaces;
  }

  /**
   * The value of the <spec-version> tag in the web-j2ee.engine.xml or 
   * if not present the version of the web.xml or if not found "2.5"
   * @return the value of specVersion.
   */
  public String getSpecificationVersion() {
    return specVersion;
  }

  /**
   * Add new values for the elements of the JSP property. The "include-prelude"
   * and "include-coda" elements will be add to the list of existing elements. 
   * All the other elements will be set with the specified values only if the 
   * corresponding elements have no values.
   * @param isXml if true, then the resourse for which this property is refered
   * is a JSP document.
   * @param elIgnored if true, the EL evaluation is ignored.
   * @param scriptingInvalid if true, the scripting elements are disabled.
   * @param pageEncoding defines the page encoding.
   * @param includePrelude list of relative paths to Web Application element to
   * be included at the begining of the jsp page.
   * @param includeCoda list of relative paths to Web Application element to
   * be included at the end of the jsp page.
   */
  public void addProperties(String isXml, String elIgnored, 
    String scriptingInvalid, String pageEncoding, List<String> includePrelude,
    List<String> includeCoda, String deferredSyntaxAllowedAsLiteral, String trimDirectiveWhitespaces) {
      setIsXml(isXml);
      setElIgnored(elIgnored);
      setScriptingInvalid(scriptingInvalid);
      setPageEncoding(pageEncoding);
      addIncludePrelude(includePrelude);
      addIncludeCoda(includeCoda);
      setDeferredSyntaxAllowedAsLiteral(deferredSyntaxAllowedAsLiteral);
      setTrimDirectiveWhitespaces(trimDirectiveWhitespaces);
  }

  /**
   * Add the specified "include-coda" element to the list of existing values.
   * @param includeCoda the "include-coda" element to be add.
   */
  private void addIncludeCoda(List<String> includeCoda) {
    if (this.includeCoda == null) {
      this.includeCoda = includeCoda;
    } else {
      this.includeCoda.addAll(includeCoda);
    }
  }

  /**
   * Add the specified "include-prelude" element to the list of existing values.
   * @param includePrelude the "include-prelude" element to be add.
   */
  private void addIncludePrelude(List<String> includePrelude) {
    if (this.includePrelude == null) {
      this.includePrelude = includePrelude;
    } else {
      this.includePrelude.addAll(includePrelude);
    }
  }

  /**
   * Sets value for the "page-encoding" property.
   * @param pageEncoding the value of the property to be set.
   */
  private void setPageEncoding(String pageEncoding) {
    if (this.pageEncoding == null) {
      this.pageEncoding = pageEncoding;
    }
  }

  /**
   * Sets value for the "scripting-invalid" property.
   * @param scriptingInvalid the value of the property to be set.
   */
  private void setScriptingInvalid(String scriptingInvalid) {
    if (this.scriptingInvalid == null) {
      this.scriptingInvalid = scriptingInvalid;
    }
  }

  /**
   * Sets value for the "el-ignored" property.
   * @param elIgnored the value of the property to be set.
   */
  private void setElIgnored(String elIgnored) {
    if (this.elIgnored == null) {
      this.elIgnored = elIgnored;
    }
  }

  /**
   * Sets value for the "is-xml" property.
   * @param isXml the value of the property to be set.
   */
  private void setIsXml(String isXml) {
    if (this.isXml == null) {
      this.isXml = isXml;
    }
  }

  /**
   * Sets value for the "deferredSyntaxAllowedAsLiteral" property.
   * @param deferredSyntaxAllowedAsLiteral the value of the property to be set.
   */
  private void setDeferredSyntaxAllowedAsLiteral(String deferredSyntaxAllowedAsLiteral) {
    if (this.deferredSyntaxAllowedAsLiteral == null) {
      this.deferredSyntaxAllowedAsLiteral = deferredSyntaxAllowedAsLiteral;
    }
  }

  /**
   * Sets value for the "trimDirectiveWhitespaces" property.
   * @param trimDirectiveWhitespaces the value of the property to be set.
   */
  private void setTrimDirectiveWhitespaces(String trimDirectiveWhitespaces) {
    if (this.trimDirectiveWhitespaces == null) {
      this.trimDirectiveWhitespaces = trimDirectiveWhitespaces;
    }
  }

}