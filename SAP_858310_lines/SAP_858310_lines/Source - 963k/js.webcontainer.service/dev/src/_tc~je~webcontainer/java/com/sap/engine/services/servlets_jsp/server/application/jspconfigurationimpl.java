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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.sap.engine.lib.descriptors5.javaee.PathType;
import com.sap.engine.lib.descriptors5.javaee.UrlPatternType;
import com.sap.engine.lib.descriptors5.web.JspPropertyGroupType;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;


/**
 * This class is an implementation of the JspConfiguration interface. 
 * 
 * @author Diyan Yordanov
 * @version 7.0
 */                            
public class JspConfigurationImpl implements JspConfiguration {
  /**
   * An ordered collection of all jsp configurations indexed by the url pattern.
   */
  private LinkedHashMap jspProperties = null;
  
  /**
   * A hashmap that keeps a corresponding path to a given url pattern. 
   */
  private HashMapObjectObject urlPatternPath = new HashMapObjectObject();
  
  /**
   * A hashmap that keeps a correpsonding extension to a given url pattern.
   */
  private HashMapObjectObject urlPatternExtension = new HashMapObjectObject();
  
  /**
   * This is the jsp configuration information read from the web.xml file. 
   */
  private JspPropertyGroupType [] jspPropertyGroupType = null;
  
  private JspConfigurationProperties defaultJspProperty = null;
  
  /**
   * Deployment descriptor version.
   */
  private String specVersion = null;
  private boolean initialized = false;

  private String defaultIsXml = null;   // unspecified
  private String defaultIsELIgnored = null; // unspecified
  private String defaultIsScriptingInvalid = "false";
  private String defaultDeferredSyntaxAllowedAsLiteral = "false";
  private String defaultTrimDirectiveWhitespaces = "false";


  /**
   * Creates new JspConfigurationImpl object.
   * @param jspPropertyGroupType a list of "jsp-property-groupType" elements 
   * read from the Web Application deployment descriptor.
   * @param specVersion the version of the web.xml file.
   */
  public JspConfigurationImpl(JspPropertyGroupType [] jspPropertyGroupType, String specVersion) {
    this.jspPropertyGroupType = jspPropertyGroupType;
    this.specVersion = specVersion;  
  }

  /**
   * Returns a property that best matches the supplied resource.
   * @param uri the resource supplied.
   * @return a JspProperty indicating the best match, or some default.
   */
  public JspConfigurationProperties getJspProperty(String uri) {

    init();

    // JSP Configuration settings do not apply to tag files
    if (jspProperties == null || uri.endsWith(".tag")
      || uri.endsWith(".tagx")) {
      return defaultJspProperty;
    }

    String uriPath = null;
    int index = uri.lastIndexOf('/');
    if (index >= 0) {
      uriPath = uri.substring(0, index + 1);
    }
    String uriExtension = null;
    index = uri.lastIndexOf('.');
    if (index >= 0) {
      uriExtension = uri.substring(index + 1);
    }

    List<String> includePreludes = new ArrayList<String>();
    List<String> includeCodas = new ArrayList<String>();

    //url-pattern keys in JspProperties if there is a previous mathc.
    String isXmlMatch = null;
    String elIgnoredMatch = null;
    String scriptingInvalidMatch = null;
    String pageEncodingMatch = null;
    String deferredSyntaxAllowedAsLiteralMatch = null;
    String trimDirectiveWhitespacesMatch = null;

    Iterator iter = jspProperties.keySet().iterator(); 
    while (iter.hasNext()) {
      String urlPattern = (String)iter.next();

      JspConfigurationProperties jp = (JspConfigurationProperties)jspProperties.get(urlPattern);

      String extension = (String)urlPatternExtension.get(urlPattern);
      String path = (String)urlPatternPath.get(urlPattern);

      if (extension == null) {
        // exact match pattern: /a/foo.jsp
        if (!uri.equals(path)) {
          // not matched;
          continue;
        }
      } else {
        // Matching patterns *.ext or /p/*
        if (path != null && uriPath != null &&
          !uriPath.startsWith(path)) {
          // not matched
          continue;
        }
        if (!extension.equals("*") &&
          !extension.equals(uriExtension)) {
          // not matched
          continue;
        }
      }
      // We have a match
      // Add include-preludes and include-codas
      if (jp.getIncludePrelude() != null) {
        includePreludes.addAll(jp.getIncludePrelude());
      }
      if (jp.getIncludeCoda() != null) {
        includeCodas.addAll(jp.getIncludeCoda());
      }

      // If there is a previous match for the same property, remember
      // the one that is more restrictive.
      if (jp.isXml() != null) {
        isXmlMatch = selectProperty(isXmlMatch, urlPattern);
      }
      if (jp.isELIgnored() != null) {
        elIgnoredMatch = selectProperty(elIgnoredMatch, urlPattern);
      }
      if (jp.isScriptingInvalid() != null) {
        scriptingInvalidMatch =
          selectProperty(scriptingInvalidMatch, urlPattern);
      }
      if (jp.getPageEncoding() != null) {
        pageEncodingMatch = selectProperty(pageEncodingMatch, urlPattern);
      }
      if (jp.isDeferredSyntaxAllowedAsLiteral() != null) {
        deferredSyntaxAllowedAsLiteralMatch = selectProperty(deferredSyntaxAllowedAsLiteralMatch, urlPattern);
      }
      if (jp.isTrimDirectiveWhitespaces() != null) {
        trimDirectiveWhitespacesMatch = selectProperty(trimDirectiveWhitespacesMatch, urlPattern);
      }
    }


    String isXml = defaultIsXml;
    String isELIgnored = defaultIsELIgnored;
    String isScriptingInvalid = defaultIsScriptingInvalid;
    String pageEncoding = null;
    String deferredSyntaxAllowedAsLiteral = null;
    String trimDirectiveWhitespaces = null;
    if (isXmlMatch != null) {
      isXml = ((JspConfigurationProperties)jspProperties.get(isXmlMatch)).isXml();
    }
    if (elIgnoredMatch != null) {
      isELIgnored = ((JspConfigurationProperties)jspProperties.get(elIgnoredMatch)).isELIgnored();
    }
    if (scriptingInvalidMatch != null) {
      isScriptingInvalid =
      ((JspConfigurationProperties)jspProperties.get(scriptingInvalidMatch)).isScriptingInvalid();
    }
    if (pageEncodingMatch != null) {
      pageEncoding = ((JspConfigurationProperties)jspProperties.get(pageEncodingMatch)).getPageEncoding();
    }
    if (deferredSyntaxAllowedAsLiteralMatch != null) {
      deferredSyntaxAllowedAsLiteral = ((JspConfigurationProperties)jspProperties.get(deferredSyntaxAllowedAsLiteralMatch)).isDeferredSyntaxAllowedAsLiteral();
    }
    if (trimDirectiveWhitespacesMatch != null) {
      trimDirectiveWhitespaces = ((JspConfigurationProperties)jspProperties.get(trimDirectiveWhitespacesMatch)).isTrimDirectiveWhitespaces();
    }

    return new JspConfigurationProperties(isXml, isELIgnored, isScriptingInvalid,
      pageEncoding, includePreludes, includeCodas, deferredSyntaxAllowedAsLiteral, 
      trimDirectiveWhitespaces, specVersion);
  }

  /**
   * Returns the value of "is-xml" property specified for the supplied resource.
   * @param uri the resource supplied. 
   * @return "true", if the group of resources that are represented by the uri
   * parameter are JSP documents, and thus must be interpreted as XML documents.
   * Returns "false" if the resources are assumed not to be JSP documents.
   */
  public String isXml(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    return jspProperty.isXml();
  }
  
  /**
   * Returns the value of the "el-ignored" property for the suplied resouorse.
   * @param uri the resource supplied.
   * @return "true" when the EL evaluation must be deactivated for this resource,
   * "false" otherwise.
   */  
  public boolean isELIgnored(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    String isELIgnored = jspProperty.isELIgnored();
    if (isELIgnored == null) {
      if (specVersion.compareTo("2.3") <= 0){
        return true;
      } else {
        return false;
      }
    } else { 
      return isELIgnored.equals("true");
    }
  }

  /**
   * Returns the value of "scripting-invalid" property for the supplied resource.
   * @param uri the resource supplied.
   * @return "true" if scripting elements are disabled and "false" otherwise.
   */
  public boolean isScriptingInvalid(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    String isScriptingInvalid = jspProperty.isScriptingInvalid();
    if (isScriptingInvalid == null) {
      return false;
    } else {
      return isScriptingInvalid.equals("true");
    }
  }
    
  /**
   * Returns the value of "page-encoding" property for the suplied resource.
   * @param uri the resource supplied.
   * @return the page-encoding property for jsp pages the match the uri.
   */
  public String getPageEncoding(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    return jspProperty.getPageEncoding();
  }
    
  /**
   * Returns a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included (as in an include directive) at the 
   * beginning of the JSP page denoted by the supplied resource.
   * @param uri the resource supplied. 
   * @return a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included at the beginning of the JSP page.
   */
  public List<String> getIncludePreludes(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    return jspProperty.getIncludePrelude();
  }  
  
  /**
   * Returns a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included (as in an include directive) at the 
   * end of the JSP page denoted by the supplied resource.
   * @param uri the resource supplied.
   * @return a list of context-relative paths corresponding to elements in the
   * Web Applcation that must be included at the end of the JSP page.
   */
  public List<String> getIncludeCodas(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    return jspProperty.getIncludeCoda();
  }
  
  /**
   * Returns the version of the JSP page.
   * @return 1.3 for JSP page if Deployment Descriptor version is less or equal
   * to 2.3, and 2.0 for all ather cases.
   */
  public String getJspVersion(){
    if (specVersion.compareTo("2.3") <= 0){
      return "1.3";
    } else if (specVersion.equals("2.4")){
      return "2.0";
    } else {
      return "2.1";
    }
  }

  /**
   * Checks whether the uri matches an url pattern in the jsp configuration
   * elements.  If so, then the uri is a JSP page.
   * @param uri the resource supplied.
   * @return "true" if the suplied resourse is a JSP page.
   */
  public boolean isJspPage(String uri) {

    init();
    if (jspProperties == null) {
      return false;
    }

    String uriPath = null;
    int index = uri.lastIndexOf('/');
    if (index >= 0) {
      uriPath = uri.substring(0, index + 1);
    }
    String uriExtension = null;
    index = uri.lastIndexOf('.');
    if (index >= 0) {
      uriExtension = uri.substring(index + 1);
    }

    Iterator iter = jspProperties.keySet().iterator(); 
    
    while (iter.hasNext()) {
      String urlPattern = (String)iter.next();

      String extension = (String)urlPatternExtension.get(urlPattern);
      String path = (String)urlPatternPath.get(urlPattern);
       
      if (extension == null) {
        if (uri.equals(path)) {
          // There is an exact match
          return true;
        }
      } else {
        if ((path == null || path.equals(uriPath)) &&
          (extension.equals("*") || extension.equals(uriExtension))) {
          // Matches *, *.ext, /p/*, or /p/*.ext
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Initialize the JSP configurations.
   */
  private void init() {
    if (!initialized) {
      processJspPropertyGroups(jspPropertyGroupType);
      defaultJspProperty = new JspConfigurationProperties(defaultIsXml,
        defaultIsELIgnored,
        defaultIsScriptingInvalid,
        null, null, null, defaultDeferredSyntaxAllowedAsLiteral, 
        defaultTrimDirectiveWhitespaces, specVersion);
      initialized = true;
    }
  }

  /**
   * Process all "jsp-property-groupType" elements and creates a list of properties
   * defined for each url pattern found in the jspPropertyGroupType.
   * @param jspPropertyGroupType the list of "jsp-property-groupType" elements 
   * read from web.xml file.
   */
  private void processJspPropertyGroups(JspPropertyGroupType [] jspPropertyGroupType) {
    if (jspPropertyGroupType == null){
      return;
    }
    jspProperties = new LinkedHashMap(); 
    for (int i = 0; i < jspPropertyGroupType.length; i++){
      UrlPatternType [] urlPatterns = jspPropertyGroupType[i].getUrlPattern();
      String pageEncoding = null;
      if (jspPropertyGroupType[i].getPageEncoding() != null) {
        pageEncoding = jspPropertyGroupType[i].getPageEncoding().get_value();
      }  
      //TODO: chek the following values if they are missing in the web.xml
      String scriptingInvalid = null;
      if (jspPropertyGroupType[i].getScriptingInvalid() != null) {
        scriptingInvalid = 
          jspPropertyGroupType[i].getScriptingInvalid().is_value() == true ? "true":"false";
      }   
      String elIgnored = null;
      if (jspPropertyGroupType[i].getElIgnored() != null) { 
        elIgnored = 
          jspPropertyGroupType[i].getElIgnored().is_value() == true ? "true":"false";
      }
      String isXml = null;
      if (jspPropertyGroupType[i].getIsXml() != null) { 
        isXml = 
          jspPropertyGroupType[i].getIsXml().is_value() == true ? "true":"false";
      }

      String deferredSyntaxAllowedAsLiteral = defaultDeferredSyntaxAllowedAsLiteral;
      if (jspPropertyGroupType[i].getDeferredSyntaxAllowedAsLiteral() != null) {
        deferredSyntaxAllowedAsLiteral =
          jspPropertyGroupType[i].getDeferredSyntaxAllowedAsLiteral().is_value() == true ? "true" : "false";
      }
      String trimDirectiveWhitespaces = defaultTrimDirectiveWhitespaces;
      if (jspPropertyGroupType[i].getTrimDirectiveWhitespaces() != null) {
        trimDirectiveWhitespaces =
          jspPropertyGroupType[i].getTrimDirectiveWhitespaces().is_value() == true ? "true" : "false";;
      }
      List<String> includePrelude = new ArrayList<String>();
      List<String> includeCoda = new ArrayList<String>();
      PathType [] preludePathType = jspPropertyGroupType[i].getIncludePrelude();
      PathType [] codaPathType = jspPropertyGroupType[i].getIncludeCoda();
      for (int j = 0; j < preludePathType.length; j++){ 
        includePrelude.add(preludePathType[j].get_value());
      }
      for (int j = 0; j < codaPathType.length; j++){
        includeCoda.add(codaPathType[j].get_value());
      }
      if (urlPatterns.length == 0) {
        continue;
      }
      
      //Add one JspPropertyGroup for each URL Pattern.  This makes
      //the matching logic easier.
      for (int p = 0; p < urlPatterns.length; p++) {
        String urlPattern = urlPatterns[p].get_value();
        String path = null;
        String extension = null;

        if (urlPattern.indexOf('*') < 0) {
          // Exact match
          path = urlPattern;
        } else {
          int slashIndex = urlPattern.lastIndexOf('/');
          String file;
          if (slashIndex >= 0) {
            path = urlPattern.substring(0, slashIndex + 1);
            file = urlPattern.substring(slashIndex + 1);
          } else {
            file = urlPattern;
          }

          if (file.equals("*")) {
            extension = "*";
          } else if (file.startsWith("*.")) {
            extension = file.substring(file.indexOf('.') + 1);
          }

          boolean isStar = "*".equals(extension);
          if ((path == null && (extension == null || isStar))
            || (path != null && !isStar)) {
            //TODO  add log trace
            continue;
          }
        }

        JspConfigurationProperties property = (JspConfigurationProperties)jspProperties.get(urlPattern);
        if (property == null){
          property = new JspConfigurationProperties(isXml, elIgnored, scriptingInvalid,
            pageEncoding, includePrelude, includeCoda, deferredSyntaxAllowedAsLiteral, 
            trimDirectiveWhitespaces, specVersion);
        } else {
          property.addProperties(isXml, elIgnored, scriptingInvalid,
          pageEncoding, includePrelude, includeCoda, deferredSyntaxAllowedAsLiteral, trimDirectiveWhitespaces);
        }
        jspProperties.put(urlPattern, property);
        if (path != null) {
          urlPatternPath.put(urlPattern, path);
        }
        if (extension != null) {
          urlPatternExtension.put(urlPattern, extension);
        }
      }
    }
  }
  
  /**
   * Select the most restrictive url-pattern. In case of tie, select the first.
   * @param prevUrlPattern the url pattern that had been previously chosen to
   * best match a given resourse.
   * @param currUrlPattern the currently checked url pattern.
   * @return the url pattern that is most restrictive.
   */
  private String selectProperty(String prevUrlPattern, String currUrlPattern) {
    if (prevUrlPattern == null) {
      return currUrlPattern;
    }
    if (urlPatternExtension.get(prevUrlPattern) == null) {
      // exact match
    return prevUrlPattern;
    }
    if (urlPatternExtension.get(currUrlPattern) == null) {
      // exact match
      return currUrlPattern;
    }
    String prevPath = (String)urlPatternPath.get(prevUrlPattern);
    String currPath = (String)urlPatternPath.get(currUrlPattern);
    if (prevPath == null && currPath == null) {
      // Both specifies a *.ext, keep the first one
      return prevUrlPattern;
    }
    if (prevPath == null && currPath != null) {
      return currUrlPattern;
    }
    if (prevPath != null && currPath == null) {
      return prevUrlPattern;
    }
    if (prevPath.length() >= currPath.length()) {
      return prevUrlPattern;
    }
    return currUrlPattern;
  }

  /**
   * Returns the value of "deferredSyntaxAllowedAsLiteral" property for the supplied resource.
   * @param uri the resource supplied.
   * @return "true" if it is allowed and "false" otherwise.
   */
  public boolean isDeferredSyntaxAllowedAsLiteral(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    String deferredSyntaxAllowedAsLiteral = jspProperty.isDeferredSyntaxAllowedAsLiteral();
    if (deferredSyntaxAllowedAsLiteral == null) {
      return false;
    } else {
      return deferredSyntaxAllowedAsLiteral.equals("true");
    }
  }

   /**
   * Returns the value of "trimDirectiveWhitespaces" property for the supplied resource.
   * @param uri the resource supplied.
   * @return "true" if it is allowed and "false" otherwise.
   */
  public boolean isTrimDirectiveWhitespaces(String uri) {
    JspConfigurationProperties jspProperty = getJspProperty(uri);
    String trimDirectiveWhitespaces = jspProperty.isTrimDirectiveWhitespaces();
    if (trimDirectiveWhitespaces == null) {
      return false;
    } else {
      return trimDirectiveWhitespaces.equals("true");
    }
  }
}