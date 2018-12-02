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
package com.sap.engine.services.servlets_jsp.server.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import com.sap.engine.lib.descriptors.webj2eeengine.CookieType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieTypeType;
import com.sap.engine.lib.descriptors.webj2eeengine.ProgrammaticSecurityAgainstType;
import com.sap.engine.lib.descriptors.webj2eeengine.ResponseStatusType;
import com.sap.engine.lib.descriptors.webj2eeengine.WebJ2EeEngineType;
import com.sap.engine.lib.descriptors5.javaee.ListenerType;
import com.sap.engine.lib.descriptors5.javaee.ParamValueType;
import com.sap.engine.lib.descriptors5.javaee.RoleNameType;
import com.sap.engine.lib.descriptors5.javaee.RunAsType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleRefType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.javaee.UrlPatternType;
import com.sap.engine.lib.descriptors5.web.ErrorPageType;
import com.sap.engine.lib.descriptors5.web.FilterType;
import com.sap.engine.lib.descriptors5.web.JspPropertyGroupType;
import com.sap.engine.lib.descriptors5.web.LocaleEncodingMappingListType;
import com.sap.engine.lib.descriptors5.web.LocaleEncodingMappingType;
import com.sap.engine.lib.descriptors5.web.MimeMappingType;
import com.sap.engine.lib.descriptors5.web.SecurityConstraintType;
import com.sap.engine.lib.descriptors5.web.ServletMappingType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.TaglibType;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.HashMapIntObject;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.op.util.FailOver;
import com.sap.engine.services.deploy.container.op.util.FailOverEnable;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.WebCookieConfig;
import com.sap.engine.services.httpserver.lib.util.SortUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;


public class WebApplicationConfig {

  //Cookie constants
  public static final int DEFAULT_SESSTION_TIMEOUT = 30;
  public static final String APPLICATION_PATH = "APPLICATION";
  public static final String SERVER_PATH = "SERVER";
  public static final String NONE_PATH = "NONE";
  public static final String SERVER_DOMAIN = "SERVER";
  public static final String NONE_DOMAIN = "NONE";

  // constants for programmatic-security-against tag. Relevant for request.getRemoteUser(); request.isUserInRole(role)
  // and request.getUserPrincipal()
  private static final short PROGRAMMATIC_SECURITY_AGAINST__NONE_DEFINED = 0;
  private static final short PROGRAMMATIC_SECURITY_AGAINST__RUNAS_USER = 1;
  private static final short PROGRAMMATIC_SECURITY_AGAINST__REMOTE_USER = -1;
  private static final String PROGRAMMATIC_SECURITY_AGAINST_TAG_NAME = "programmatic-security-against"; // since it is not present in j2eedescriptors

  private static Location currentLocation = Location.getLocation(WebApplicationConfig.class);
  private static Location traceLocation = LogContext.getLocationDeploy();
  private ApplicationContext servletContextFacade = null;

  private String displayName = null;
  private FilterType[] filters = null;
  private String[] listeners = null;
  private ErrorPageType[] errorPages = null;
  private String domainName = null;
  private String realmName = null;
  private String loginPage = null;
  private String errorPage = null;

  private SecurityRoleType[] secRoles = null;

  private String changePasswordLoginPage = null;
  private String changePasswordErrorPage = null;
  private final ArrayObject welcomeFiles = new ArrayObject();
  private boolean urlSessionTracking = false;
  private boolean programmaticSecurityAgainstRunAsIdentity = false;
  private int progSecurityAgainstInGlobalDesc = PROGRAMMATIC_SECURITY_AGAINST__NONE_DEFINED;
  private int sessionTimeout = 1800;  //DEFAULT_SESSTION_TIMEOUT = 30 min
  private int maxSessions = -1;
  private TaglibType[] tagLibs = null;
  private HashMap<String, Hashtable<String, String>>  servletArgs = new HashMap<String, Hashtable<String, String>>();
  private HashMap<String, String> servletClasses = new HashMap<String, String>();
  private HashMapObjectObject servletRunAs = new HashMapObjectObject();
  private HashMapObjectObject localeEncodingMappings = new HashMapObjectObject();
  private String[][] all = null;
  private int intFailOverFromDescriptor = FailOver.DISABLE.getId().byteValue();
  private WebCookieConfig sessionCookieConfig = null;
  private WebCookieConfig applicationCookieConfig = null;
  private JspPropertyGroupType[] jspPropertyGroupType = null;
  private NamingResources namingResources = null;
  //From additional web DD what was the web application version before converting
  private String specVersion = null;
  private Double specVersionDb = new Double(2.4d);
  //this is web application version from web DD
  private String webAppVersion = null;
  /**
   * If in web descriptor are defined any security constraints
   */
  private boolean webAppContainsSecurityContraints = false;

  public WebApplicationConfig(ApplicationContext servletContextFacade) {
    this.servletContextFacade = servletContextFacade;
    sessionCookieConfig = new WebCookieConfig(servletContextFacade.getAliasName(), WebCookieConfig.COOKIE_TYPE_SESSION);
    applicationCookieConfig = new WebCookieConfig(servletContextFacade.getAliasName(), WebCookieConfig.COOKIE_TYPE_APPLICATION);
    namingResources = new NamingResources(servletContextFacade);
  }

  public void parse(WebDeploymentDescriptor webDesc, boolean isGlobal) throws DeploymentException {
    String aliasName = servletContextFacade.getAliasName();

    namingResources.initNamingReferences(webDesc);
    if (webDesc.getDisplayNames() != null && webDesc.getDisplayNames().length > 0) {
      displayName = webDesc.getDisplayNames()[0].get_value();
    }
    if (webDesc.getWebJ2EEEngine().getUrlSessionTracking() != null) {
      urlSessionTracking = webDesc.getWebJ2EEEngine().getUrlSessionTracking().booleanValue();
    }
    
    if (!isGlobal){ //check if the tag is present in the local descriptor
    	if(webDesc.isMaxSessionsTagConfigured()){
    		maxSessions = webDesc.getWebJ2EEEngine().getMaxSessions().intValue();
    		if (maxSessions <= 0){
    			maxSessions = -1;
    		}
    	}
    }else{ //take the value from the global descriptor if not null
    	if (webDesc.getWebJ2EEEngine().getMaxSessions() != null){
    		maxSessions = webDesc.getWebJ2EEEngine().getMaxSessions().intValue();
    		if (maxSessions <= 0){
    			maxSessions = -1;
    		}
    	}
    }
    
    initServletContextParams(webDesc);
    initResponseCodes(webDesc, aliasName);
    initWelcomeFiles(webDesc);
    initTagLibs(webDesc);
    initJspPropertyGroup(webDesc);
    initFilters(webDesc);
    initListeners(webDesc);
    initErrorPages(webDesc);
    if( isGlobal ) {
      HashMap<String, String> globalMimeTypes =  ServiceContext.getServiceContext().getDeployContext().getGlobalMimeTypes();
      servletContextFacade.setGlobalMimeTypes(globalMimeTypes);
    }else {
      // mime types in global-web.xml are kept in singleton hashmap
      initMimeMappings(webDesc);
    }
    initSessionTimeout(webDesc);
    if (webDesc.getLoginConfig() != null) {
      if (webDesc.getLoginConfig().getRealmName() != null) {
        realmName = webDesc.getLoginConfig().getRealmName().get_value();
      }
      if (webDesc.getLoginConfig().getFormLoginConfig() != null) {
        if (webDesc.getLoginConfig().getFormLoginConfig().getFormLoginPage() != null) {
          loginPage = webDesc.getLoginConfig().getFormLoginConfig().getFormLoginPage().get_value();
        }
        if (webDesc.getLoginConfig().getFormLoginConfig().getFormErrorPage() != null) {
          errorPage = webDesc.getLoginConfig().getFormLoginConfig().getFormErrorPage().get_value();
        }
      }
    }
    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() != null && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getSecurityPolicyDomain() != null) {
      domainName = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getSecurityPolicyDomain();
    }
    if (domainName == null || domainName.length() == 0) {
        domainName = "/" + aliasName;
        //domainName = domainName.replace('/', '*').replace('\\', '*');
    }
    initPasswordChangePages(webDesc);
    try {
      initSecurityRoles(webDesc);
    } catch (Exception ex) {
      throw new DeploymentException("Invalid element content in SecurityRoles description", ex);
    }
    initSecurityConstraints(webDesc);

    servletContextFacade.getWebMappings().addFilterMapping(webDesc.getFilterMappings());
    initServletMappings(webDesc, aliasName);
    initServlets(webDesc, aliasName);
    initJspMappings(jspPropertyGroupType, aliasName);
    initLocaleMappings(webDesc);
   
    if (!isGlobal){
    	if (webDesc.isSessCookieConfigured() || webDesc.isAppCookieConfigured()){
    		initCookieConfigs(webDesc.getWebJ2EEEngine().getCookieConfig().getCookie(), aliasName, isGlobal, webDesc.isSessCookieConfigured());
    	}
    }else{
    	if (webDesc.getWebJ2EEEngine().getCookieConfig() != null) {
    		initCookieConfigs(webDesc.getWebJ2EEEngine().getCookieConfig().getCookie(), aliasName, isGlobal, webDesc.isSessCookieConfigured());
    	}
    }
    
    if (webDesc.getWebJ2EEEngine().getSpecVersion() != null) {
      specVersion = webDesc.getWebJ2EEEngine().getSpecVersion().getValue();
    }

    if (specVersion != null) {
      try {
        specVersionDb = new Double(specVersion);
      } catch (NumberFormatException e1) {
        //if unable to convert the version to double : leave it null
    	  if (traceLocation.beWarning()) {
              LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000458",
            		  "Incorrect web application version found. Will use the default one [2.4].", e1, null, null);
        }
      }
    }

    webAppVersion = webDesc.getWebAppVersion() != null ? webDesc.getWebAppVersion().getValue() : "2.5";
    if ("2.5".equalsIgnoreCase(webAppVersion)) {
      servletContextFacade.getInjectionWrapper().setPostConstructMethods(webDesc.getPostConstruct());
      servletContextFacade.getInjectionWrapper().setPreDestroyMethods(webDesc.getPreDestroy());
    }
    setProgrammaticSecurityAgainstValue(webDesc, isGlobal, aliasName);
  }//end of parse(InputStream xmlFile, InputStream additonalXmlFile, boolean isGlobal)

  private void setProgrammaticSecurityAgainstValue(WebDeploymentDescriptor webDesc, boolean isGlobal, String aliasName) {
    WebJ2EeEngineType webJ2EEEngine = webDesc.getWebJ2EEEngine();
    ProgrammaticSecurityAgainstType programmaticSecurityAgainst = webJ2EEEngine.getProgrammaticSecurityAgainst();
    if (traceLocation.beDebug()) { // TODO debug
    	traceLocation.debugT("setProgrammaticSecurityAgainstValue(isGlobal: " +  isGlobal + ", aliasName:" + aliasName +")");
    }
    if (isGlobal) { // global additional descriptor case
      progSecurityAgainstInGlobalDesc = PROGRAMMATIC_SECURITY_AGAINST__NONE_DEFINED;
      if (programmaticSecurityAgainst != null && programmaticSecurityAgainst.getValue() != null) {
        String value = programmaticSecurityAgainst.getValue();
        if (ProgrammaticSecurityAgainstType._RUNASIDENTITY.equals(value)) {
          progSecurityAgainstInGlobalDesc = PROGRAMMATIC_SECURITY_AGAINST__RUNAS_USER;
        } else {
          // no parsing - default
          progSecurityAgainstInGlobalDesc = PROGRAMMATIC_SECURITY_AGAINST__REMOTE_USER;
          if (!ProgrammaticSecurityAgainstType._CALLERIDENTITY.equals(value)) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000344",
              "Unknown value in global descriptor for tag '{0}'."
              + "The default value '{1}' will be used. "
              + "For more info check documentation of web-j2ee-engine.xml descriptor.", 
              new Object[]{PROGRAMMATIC_SECURITY_AGAINST_TAG_NAME, ProgrammaticSecurityAgainstType._CALLERIDENTITY}, null, null);
          }
        }
      }
      //    TODO remove
      if (traceLocation.beDebug()) { // TODO debug
      	traceLocation.debugT("setProgrammaticSecurityAgainstValue(isGlobal: " +  isGlobal + ", aliasName:" + aliasName +"): inGlobalDesc: " + progSecurityAgainstInGlobalDesc);
      }
      return;
    }

    // local web.xml value
    boolean runAsTagFound = isRunAsTagDefinedForSomeServlet(webDesc.getServlets());
    if (programmaticSecurityAgainst == null || programmaticSecurityAgainst.getValue() == null) {
      // no value defined
      if (progSecurityAgainstInGlobalDesc == PROGRAMMATIC_SECURITY_AGAINST__NONE_DEFINED) {
        if (isJ2ee13OrLess()) { // old behavior - programmatic security checks are against user from RunAs role defined
          programmaticSecurityAgainstRunAsIdentity = true;
          if (runAsTagFound && traceLocation.beWarning()) {
        	  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000586", "Old web module version (2.3 or less) detected and no " +
        			  "programmatic-security-againsttag is defined in additional web descriptor "
                + "for module with alias [{0}]. The deprecated value RUNAS-IDENTITY will be used. "
                + "For more information please check Note 993932.", new Object[] {aliasName}, null, null);
          }
        } else {
          programmaticSecurityAgainstRunAsIdentity = false;
        }
      } else {
        // value from global descriptor will be used
        programmaticSecurityAgainstRunAsIdentity = (progSecurityAgainstInGlobalDesc == PROGRAMMATIC_SECURITY_AGAINST__RUNAS_USER);
        if (runAsTagFound && programmaticSecurityAgainstRunAsIdentity ) {
          if (traceLocation.beWarning()) {
        	  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000587",
              "The deprecated value RUNAS-IDENTITY is defined globally. " +
              "Affected web module with alias [{0}]. For more info check documentation of web-j2ee-engine.xml descriptor. " +
              "SAP Note 993932.", new Object[] {aliasName}, null, null);
          }
        }
      }
    } else {
      // value defined in web.xml
      String value = programmaticSecurityAgainst.getValue();
      if (ProgrammaticSecurityAgainstType._RUNASIDENTITY.equals(value)) {
        programmaticSecurityAgainstRunAsIdentity = true;
        if (runAsTagFound && traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000588",
            "The deprecated value RUNAS-IDENTITY is defined for web module with alias [{0}" +
            "]. For more info check documentation of web-j2ee-engine.xml descriptor. SAP Note 993932.", new Object[] {aliasName}, null, null);
        }
      } else {
        programmaticSecurityAgainstRunAsIdentity = false;
      }
    }
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("programmatic-security-against(global: " +  isGlobal + ", alias:"
          + aliasName +"): against run-as: " + programmaticSecurityAgainstRunAsIdentity );
    }
  } // setProgrammaticSecurityAgainstValue()

  public boolean isProgrammaticSecurityAgainstRunAsIdentity() {
    return programmaticSecurityAgainstRunAsIdentity;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String[][] getAllServlets() {
    return all;
  }

  public String getRealmName() {
    return realmName;
  }

  public String getLoginPage() {
    return loginPage;
  }

  public String getErrorPage() {
    return errorPage;
  }

  public String getDomainName() {
    return domainName;
  }

  HashMap<String, Hashtable<String, String>>  getServletArguments() {
    return servletArgs;
  }

  public HashMap<String, String> getServletClasses() {
    return servletClasses;
  }

  public String getRunAsRoleName(String servletName) {
    return (String) servletRunAs.get(servletName);
  }

  public ArrayObject getWelcomeFiles() {
    return welcomeFiles;
  }

  public SecurityRoleType[] getSecurityRoles() {
    return secRoles;
  }

  public String getChangePasswordLoginPage() {
    return changePasswordLoginPage;
  }

  public String getChangePasswordErrorPage() {
    return changePasswordErrorPage;
  }

  public boolean isURLSessionTracking() {
    return urlSessionTracking;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public int getMaxSessions() {
    return maxSessions;
  }

  public WebCookieConfig getSessionCookieConfig() {
    return sessionCookieConfig;
  }

  public WebCookieConfig getApplicationCookieConfig() {
    return applicationCookieConfig;
  }

  public NamingResources getNamingResources() {
    return namingResources;
  }

  public TaglibType[] getTagLibs() {
    return tagLibs;
  }

  public FilterType[] getFilters() {
    return filters;
  }

  public String[] getListeners() {
    return listeners;
  }

  public JspPropertyGroupType[] getJspPropertyGroup() {
    return jspPropertyGroupType;
  }

  public String getSpecVersion() {
    return specVersion;
  }

  public ErrorPageType[] getErrorPages() {
    return errorPages;
  }

  public int getIntFailOver() {
    return intFailOverFromDescriptor;
  }

  public void setFailOver(String failover) {
    if (FailOver.DISABLE.getName().equals(failover)) {
      intFailOverFromDescriptor = FailOver.DISABLE.getId().byteValue();
    } else if (FailOverEnable.ON_REQUEST_4_INSTANCE_LOCAL.getName().equals(failover)) {
      intFailOverFromDescriptor = FailOverEnable.ON_REQUEST_4_INSTANCE_LOCAL.getId().byteValue();
    } else if (FailOverEnable.ON_ATTRIBUTE_4_INSTANCE_LOCAL.getName().equals(failover)) {
      intFailOverFromDescriptor = FailOverEnable.ON_ATTRIBUTE_4_INSTANCE_LOCAL.getId().byteValue();
    }
  }

  public HashMapObjectObject getLocaleMappings() {
    return localeEncodingMappings;
  }

  private void initJspMappings(JspPropertyGroupType[] jspPropertyGroupType, String aliasName) {
    if (jspPropertyGroupType != null) {
      for (int i = 0; i < jspPropertyGroupType.length; i++) {
        UrlPatternType[] urlPatterns = jspPropertyGroupType[i].getUrlPattern();
        for (int p = 0; p < urlPatterns.length; p++) {
          String urlPattern = urlPatterns[p].get_value();
          try {
            servletContextFacade.getWebMappings().addJspMapping(canonicalizeMapping(urlPattern, aliasName));
          } catch (DeploymentException e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000352",
              "Incorrect value [{0}] found in the url-pattern subtag in the web deployment descriptor web.xml of [{1}] web application. " +
              "Please specify correct value.", new Object[]{urlPattern, aliasName}, e, null, null);
          }
        }
      }
    }
  }//end of initJspMappings(JspPropertyGroupType[] jspPropertyGroupType, String aliasName)

  public static String canonicalizeMapping(String urlPattern, String aliasName) throws DeploymentException {
    if (urlPattern.indexOf(0x0D) != -1 || urlPattern.indexOf(0x0A) != -1) {
      throw new WebDeploymentException(WebDeploymentException.INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION,
        new Object[]{urlPattern, aliasName});
    }

    if (urlPattern.indexOf("*.") != -1 || urlPattern.equals("/")) {
      return urlPattern;
    }

    String result = null;
    StringTokenizer pathCanonize = new StringTokenizer(urlPattern, "/");
    while (pathCanonize.hasMoreTokens()) {
      String token = pathCanonize.nextToken();
      if (token.equals("..")) {
        if (result == null || result.equals("")) {
          throw new WebDeploymentException(WebDeploymentException.INVALID_URL_PATTERN_IN_DEPLOYMENT_DESCRIPTOR_FOR_WEBAPPLICATION,
            new Object[]{urlPattern, aliasName});
        } else {
          result = result.substring(0, result.lastIndexOf("/"));
        }
      } else if (!token.equals(".")) {
        if (result == null) {
          result = "/" + token;
        } else {
          result = result + "/" + token;
        }
      }
    }
    return result;
  }//end of canonicalizeMapping(String urlPattern, String aliasName)

  private void initResponseCodes(WebDeploymentDescriptor webDesc, String aliasName) {
    ResponseStatusType[] responseStatuses = webDesc.getWebJ2EEEngine().getResponseStatus();
    if (responseStatuses == null) {
      return;
    }
   HashMapIntObject responseCodeToMessage = new HashMapIntObject();
    for (int i = 0; i < responseStatuses.length; i++) {
      responseCodeToMessage.put(responseStatuses[i].getStatusCode(), responseStatuses[i].getReasonPhrase());
    }

    ResponseCodes.setClientMessages(aliasName, responseCodeToMessage);
  }//end of initResponseCodes(WebDeploymentDescriptor webDesc, String aliasName)

  private void initWelcomeFiles(WebDeploymentDescriptor webDesc) {
    if (webDesc.getWelcomeFileList() != null && webDesc.getWelcomeFileList().getWelcomeFile() != null) {
      String[] temp = webDesc.getWelcomeFileList().getWelcomeFile();
      for (int i = 0; i < temp.length; i++) {
        if (temp[i] != null) {
          welcomeFiles.addElement(temp[i].getBytes());
        }
      }
    }
  }//end of initWelcomeFiles(WebDeploymentDescriptor webDesc)

  private void initTagLibs(WebDeploymentDescriptor webDesc) {
    if (webDesc.getJspConfig() == null) {
      return;
    }

    if (tagLibs == null) {
      tagLibs = webDesc.getJspConfig().getTaglib();
    } else if (webDesc.getJspConfig().getTaglib() != null) {
      List<TaglibType> concat = concatenateArrays(tagLibs, webDesc.getJspConfig().getTaglib());
      tagLibs = concat.toArray(new TaglibType[concat.size()]);
    }
  }//end of initTagLibs(WebDeploymentDescriptor webDesc)

  private void initFilters(WebDeploymentDescriptor webDesc) {
    if (filters == null) {
      filters = webDesc.getFilters();
    } else if (webDesc.getFilters() != null) {
      List<FilterType> concat = concatenateArrays(filters, webDesc.getFilters());
      filters = concat.toArray(new FilterType[concat.size()]);
    }
  }//end of initFilters(WebDeploymentDescriptor webDesc)

  private void initJspPropertyGroup(WebDeploymentDescriptor webDesc) {
    if (webDesc.getJspConfig() == null) {
      return;
    }
    if (jspPropertyGroupType == null) {
      jspPropertyGroupType = webDesc.getJspConfig().getJspPropertyGroup();
    } else if (webDesc.getJspConfig().getJspPropertyGroup() != null) {
      List<JspPropertyGroupType> concat = concatenateArrays(jspPropertyGroupType, webDesc.getJspConfig().getJspPropertyGroup());
      jspPropertyGroupType = concat.toArray(new JspPropertyGroupType[concat.size()]);
    }
  }//end of initJspPropertyGroup(WebDeploymentDescriptor webDesc)

  private void initServlets(WebDeploymentDescriptor webDesc, String aliasName) {
    ServletType[] servletDesc = webDesc.getServlets();
    if (servletDesc == null) {
      return;
    }

    for (int i = 0; i < servletDesc.length; i++) {
      initServlet(servletDesc[i], aliasName);
    }

    if (all != null) {
      String newAll[][] = new String[servletDesc.length][3];
      for (int i = 0; i < servletDesc.length; i++) {
        initRunAs(servletDesc[i], aliasName);

        if (servletDesc[i].getChoiceGroup1() == null) {
          continue;
        }

        if (servletDesc[i].getLoadOnStartup() != null && servletDesc[i].getLoadOnStartup().toString() != null) {
          newAll[i][0] = servletDesc[i].getLoadOnStartup().toString();
        } else {
          newAll[i][0] = Integer.toString(-1);
        }

        String servletName = servletDesc[i].getServletName().get_value();
        newAll[i][1] = servletName;

        try {
          if (servletDesc[i].getChoiceGroup1().isSetJspFile()) {
            String jsp = servletDesc[i].getChoiceGroup1().getJspFile().get_value();
            if (!jsp.startsWith("/")) {
              jsp = "/" + jsp;
            }
            servletContextFacade.getWebComponents().addJsp(servletName, jsp);
            newAll[i][2] = jsp;
          } else {
            servletContextFacade.getWebComponents().addServlet(servletName);
            newAll[i][2] = servletDesc[i].getChoiceGroup1().getServletClass().get_value();
          }
        } catch (Exception e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000355",
            "Incorrect data in servlet-class or jsp-file tags in the web deployment descriptor web.xml of [{0}] web application.", 
            new Object[]{aliasName}, e, null, null);
        }
      }

      String oldAll[][] = (String[][]) all.clone();
      all = new String[oldAll.length + newAll.length][3];
      for (int i = 0; i < oldAll.length; i++) {
        for (int j = 0; j < oldAll[i].length; j++) {
          all[i][j] = oldAll[i][j];
        }
      }
      int index = oldAll.length;
      for (int i = 0; i < newAll.length; i++) {
        for (int j = 0; j < newAll[i].length; j++) {
          all[i + index][j] = newAll[i][j];
        }
      }

      SortUtils.sort(all);
    } else {
      all = new String[servletDesc.length][3];
      for (int i = 0; i < servletDesc.length; i++) {
        initRunAs(servletDesc[i], aliasName);

        if (servletDesc[i].getChoiceGroup1() == null) {
          continue;
        }

        try {
          if (servletDesc[i].getLoadOnStartup() != null && servletDesc[i].getLoadOnStartup().toString() != null) {
            all[i][0] = servletDesc[i].getLoadOnStartup().toString();
          } else {
            all[i][0] = Integer.toString(-1);
          }

          String servletName = servletDesc[i].getServletName().get_value();
          all[i][1] = servletName;

          if (servletDesc[i].getChoiceGroup1().isSetJspFile()) {
            String jspFile = servletDesc[i].getChoiceGroup1().getJspFile().get_value();
            if (!jspFile.startsWith("/")) {
              jspFile = "/" + jspFile;
            }
            servletContextFacade.getWebComponents().addJsp(servletName, jspFile);
            all[i][2] = jspFile;
          } else {
            servletContextFacade.getWebComponents().addServlet(servletName);
            all[i][2] = servletDesc[i].getChoiceGroup1().getServletClass().get_value();
          }
        } catch (Exception e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000356", 
            "Incorrect data in jsp-file tag in the web deployment descriptor web.xml of [{0}] web application.", new Object[]{aliasName}, e, null, null);
        }
      }
    }
  }//end of initServlets(WebDeploymentDescriptor webDesc, String aliasName)

  private void initServlet(ServletType servletDescriptor, String aliasName) {
    if (servletDescriptor.getServletName() == null || servletDescriptor.getServletName().get_value() == null) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000340",
        "Incorrect data in servlet tag in the deployment descriptor web.xml of [{0}] web application. Servlet name is not specified.", 
        new Object[]{aliasName}, null, null);
      return;
    }
    String servletName = servletDescriptor.getServletName().get_value();

    HashMapObjectObject tmp = new HashMapObjectObject();
    SecurityRoleRefType[] roleReferences = servletDescriptor.getSecurityRoleRef();
    if (roleReferences != null) {
      for (int p = 0; p < roleReferences.length; p++) {
        if (roleReferences[p].getRoleLink() == null || roleReferences[p].getRoleLink().get_value() == null) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000341",
        		"Incorrect servlet tag found in the web deployment descriptor web.xml of [{0}] web application ." +
        		" Incorrect role reference for servlet [{1}]. Please specify a valid value for the role link subtag.", 
        		new Object[]{aliasName, servletName}, null, null);
        } else {
          tmp.put(roleReferences[p].getRoleName().get_value(), roleReferences[p].getRoleLink().get_value());
        }
      }
    }

    if (servletDescriptor.getChoiceGroup1() == null || !(servletDescriptor.getChoiceGroup1().isSetServletClass() || servletDescriptor.getChoiceGroup1().isSetJspFile())) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000342",
    		"Incorrect servlet tag found in the web deployment descriptor web.xml of [{0}] web application. " +
    		"Please specify class name or JSP file for servlet [{1}].", new Object[]{aliasName, servletName}, null, null);
      return;
    }

    ParamValueType[] params = servletDescriptor.getInitParam();
    Hashtable<String, String> paramsH = new Hashtable<String, String>();
    if (params != null) {
      for (int j = 0; j < params.length; j++) {
        if (params[j] != null) {
          if (params[j].getParamName() == null || params[j].getParamName().get_value() == null
            || params[j].getParamValue() == null || params[j].getParamValue().get_value() == null) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000343",
              "Incorrect parameters of the servlet [{0}]. Parameter name or value is null." +
            	"Please specify correct parameters in the web deployment descriptor web.xml of [{1}] web application.", 
            	new Object[]{servletName, aliasName}, null, null);
          } else {
            paramsH.put(params[j].getParamName().get_value(), params[j].getParamValue().get_value());
          }
        }
      }
    }

    try {
      if (servletDescriptor.getChoiceGroup1().isSetServletClass()) {
        servletClasses.put(servletName, servletDescriptor.getChoiceGroup1().getServletClass().get_value());
      }
      if( !paramsH.isEmpty() ) {
        servletArgs.put(servletName, paramsH);
      }
    } catch (Exception ex) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000353", 
        "Incorrect data in servlet-class tag found in the web deployment descriptor web.xml of [{0}] web application.", 
        new Object[]{aliasName}, ex, null, null);
    }
  }//end of initServlet(ServletType servletDescriptor, String aliasName)

  private void initRunAs(ServletType servletDescriptor, String aliasName) {
    String name = servletDescriptor.getServletName() != null ? servletDescriptor.getServletName().get_value() : null;

    if (name == null) {
      return;
    }

    if (servletDescriptor.getRunAs() != null && servletDescriptor.getRunAs().getRoleName() != null && secRoles != null) {
      String roleName = servletDescriptor.getRunAs().getRoleName().get_value();
      for (int m = 0; m < secRoles.length; m++) {
        if (roleName.equals(secRoles[m].getRoleName().get_value())) {
          servletRunAs.put(name, roleName);
          return;
        }
      }
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000339",
        "Incorrect run-as tag for servlet [{0}]. " +
        "User-role is not specified. Please specify correct user-role in the web deployment descriptor web.xml of [{1}] web application.", 
        new Object[]{name, aliasName}, null, null);
    }
  }//end of initRunAs(ServletType servletDescriptor, String aliasName)

  private void initListeners(WebDeploymentDescriptor webDesc) {
    List<String> temp = new ArrayList<String>();
    ListenerType[] tempArray = webDesc.getListeners();
    if (tempArray != null) {
      for (int i = 0; i < tempArray.length; i++) {
        if (tempArray[i] != null && tempArray[i].getListenerClass() != null) {
          temp.add(tempArray[i].getListenerClass().get_value());
        }
      }
    }

    if (listeners == null) {
      if (temp.size() > 0) {
        listeners = temp.toArray(new String[temp.size()]);
      }
    } else if (temp.size() > 0) {
      List<String> concat = concatenateArrays(listeners, temp.toArray(new String[] {}));
      listeners = concat.toArray(new String[concat.size()]);
    }
  }//end of initListeners(WebDeploymentDescriptor webDesc)

  private void initErrorPages(WebDeploymentDescriptor webDesc) {
    if (errorPages == null) {
      errorPages = webDesc.getErrorPage();
    } else if (webDesc.getErrorPage() != null) {
      List<ErrorPageType> concat = concatenateArrays(errorPages, webDesc.getErrorPage());
      errorPages = concat.toArray(new ErrorPageType[concat.size()]);
    }
  }//end of initErrorPages(WebDeploymentDescriptor webDesc)

  private void initMimeMappings(WebDeploymentDescriptor webDesc) {
    MimeMappingType[] mimeMapping = webDesc.getMIMEMapping();
    if (mimeMapping != null) {
      for (int i = 0; i < mimeMapping.length; i++) {
        if (mimeMapping[i].getExtension() != null && mimeMapping[i].getMimeType() != null) {
          servletContextFacade.addMimeType(mimeMapping[i].getExtension().get_value(), mimeMapping[i].getMimeType().get_value());
        }
      }
    }
  }//end of initMimeMappings(WebDeploymentDescriptor webDesc)

  private void initSessionTimeout(WebDeploymentDescriptor webDesc) {
    if (webDesc.getSessionConfig() != null && webDesc.getSessionConfig().getSessionTimeout() != null) {
      sessionTimeout = webDesc.getSessionConfig().getSessionTimeout().get_value().intValue();
      sessionTimeout = (sessionTimeout > 0) ? sessionTimeout * 60 : -1;
    } 
  }//end of initSessionTimeout(WebDeploymentDescriptor webDesc)

  private void initServletContextParams(WebDeploymentDescriptor webDesc) {
    ParamValueType[] contextParams = webDesc.getContextParams();
    if (contextParams != null) {
      for (int i = 0; i < contextParams.length; i++) {
        if (contextParams[i].getParamName() != null && contextParams[i].getParamValue() != null) {
          servletContextFacade.addInitParameter(contextParams[i].getParamName().get_value(), contextParams[i].getParamValue().get_value());
        }
      }
    }
  }//end of initServletContextParams(WebDeploymentDescriptor webDesc)

  private void initServletMappings(WebDeploymentDescriptor webDesc, String aliasName) {
    ServletMappingType[] servletMapping = webDesc.getServletMapping();
    if (servletMapping != null) {
      for (int i = 0; i < servletMapping.length; i++) {
        if (servletMapping[i].getUrlPattern() != null && servletMapping[i].getServletName() != null) {
          UrlPatternType[] urlMappings = servletMapping[i].getUrlPattern();
          String servletName = servletMapping[i].getServletName().get_value();
          for (int j = 0; j < urlMappings.length; j++) {
            String urlMappingValue = urlMappings[j].get_value();
            try {
              urlMappingValue = canonicalizeMapping(urlMappingValue, aliasName);
              servletContextFacade.getWebMappings().addMapping(urlMappingValue, servletName);
            } catch (DeploymentException e) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000354",
                "Incorrect servlet mapping found: [{0}] for servlet [{1}].  " +
                "Please specify correct value for url-pattern subtag of servlet-mapping tag in the web deployment descriptor web.xml of [{2}] web application.", 
                new Object[]{urlMappingValue, servletName, aliasName}, e, null, null);
            }
          }
        }
      }
    }
  }//end of initServletMappings(WebDeploymentDescriptor webDesc, String aliasName)

  private void initCookieConfigs(CookieType[] cookieConfigs, String aliasName, boolean isGlobal, boolean isSessCookieConfigured) {
    if (cookieConfigs == null) {
      return;
    }

    for (int i = 0; i < cookieConfigs.length; i++) {
      if (cookieConfigs[i] != null) {
        if (cookieConfigs[i].getType() == null) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000337",
            "Required subtag is not specified in the additional deployment descriptor web-j2ee-engine.xml of [{0}] web application. " +
            "Please specify correct value for <type> subtag of <cookie-config> tag.", new Object[]{aliasName}, null, null);
        } else if ((cookieConfigs[i].getType().getValue()).equals(CookieTypeType._SESSION) || (cookieConfigs[i].getType().getValue()).equals("")) {
        	//session cookie is configured by default if the cookie config tag is missing in the local descriptor => do not apply cookieConfigs in this case 
        	if(!isGlobal && !isSessCookieConfigured){continue;}
        	//in all other cases cookie configurations must be applied
        	initCookie(sessionCookieConfig, cookieConfigs[i]);
        } else if ((cookieConfigs[i].getType().getValue()).equals(CookieTypeType._APPLICATION)) {
          initCookie(applicationCookieConfig, cookieConfigs[i]);
        } else {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000338",
            "Incorrect value of cookie-config tag in the additional deployment descriptor web-j2ee-engine.xml of [{0}] web application. " +
            "The subtag <type> must have value either SESSION or APPLICATION. The value is [{1}].", 
            new Object[]{aliasName, cookieConfigs[i].getType().getValue()}, null, null);
        }
      }
    }
  }//end of initCookieConfigs(CookieType[] cookieConfigs, String aliasName)

  private void initCookie(WebCookieConfig cookie, CookieType config) {
    if (config.getPath() != null) {
      if (config.getPath().equals(NONE_PATH)) {
        cookie.setPath(WebCookieConfig.NONE, config.getPath());
      } else if (config.getPath().equals(APPLICATION_PATH)) {
        cookie.setPath(WebCookieConfig.APPLICATION, config.getPath());
      } else if (config.getPath().equals(SERVER_PATH)) {
        cookie.setPath(WebCookieConfig.SERVER, config.getPath());
      } else if ("".equals(config.getPath())) {
        cookie.setPath(WebCookieConfig.OTHER, "/");
      } else {
        cookie.setPath(WebCookieConfig.OTHER, config.getPath());
      }
    }

    if (config.getDomain() != null) {
      if (config.getDomain().equals(NONE_DOMAIN)) {
        cookie.setDomain(WebCookieConfig.NONE, config.getDomain());
      } else if (config.getDomain().equals(SERVER_DOMAIN)) {
        cookie.setDomain(WebCookieConfig.SERVER, config.getDomain());
      } else if ("".equals(config.getDomain())) {
        if (CookieTypeType._SESSION.equals(config.getType().getValue())) {
          //Set domain none by default as it is not security sensible to issue domain by default 
          cookie.setDomain(WebCookieConfig.NONE, config.getDomain());
        } else if (CookieTypeType._APPLICATION.equals(config.getType().getValue())) {
          cookie.setDomain(WebCookieConfig.NONE, config.getDomain());
        }
      } else {
        cookie.setDomain(WebCookieConfig.OTHER, config.getDomain());
      }
    } else {
      if (CookieTypeType._SESSION.equals(config.getType().getValue())) {
//      Set domain none by default as it is not security sensible to issue domain by default 
        cookie.setDomain(WebCookieConfig.NONE, config.getDomain()); 
      } else if (CookieTypeType._APPLICATION.equals(config.getType().getValue())) {
        cookie.setDomain(WebCookieConfig.NONE, config.getDomain());
      }
    }

    if (config.getMaxAge() != null && config.getMaxAge().intValue() > 0) {
      cookie.setMaxAgen(config.getMaxAge().intValue());
    } else {
      cookie.setMaxAgen(-1);
    }
  }//end of initCookie(WebCookieConfig cookie, CookieType config)

  private void initPasswordChangePages(WebDeploymentDescriptor webDesc) {
    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() == null || webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig() == null) {
      return;
    }

    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getLoginPage() != null) {
      changePasswordLoginPage = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getLoginPage();
    }

    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getErrorPage() != null) {
      changePasswordErrorPage = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getErrorPage();
    }
  }//end of initPasswordChangePages(WebDeploymentDescriptor webDesc)

  private void initSecurityRoles(WebDeploymentDescriptor webDesc) throws Exception {
    SecurityRoleType[] secRolesTemp = webDesc.getSecurityRoles();
    HashMap<String, SecurityRoleType> secRolesMap = new HashMap<String, SecurityRoleType>();
    if (secRoles == null) {
      if (secRolesTemp != null) {
        secRoles = secRolesTemp;
      }
    } else if (secRolesTemp != null) {
      for (int i = 0; i < secRoles.length; i++) {
        secRolesMap.put(secRoles[i].getRoleName().get_value(), secRoles[i]);
      }

      for (int i = 0; i < secRolesTemp.length; i++) {
        secRolesMap.put(secRolesTemp[i].getRoleName().get_value(), secRolesTemp[i]);
      }

      if (secRolesMap.size() > 0) {
        secRoles = (SecurityRoleType[]) secRolesMap.values().toArray(new SecurityRoleType[secRolesMap.size()]);
      }
    }
  }//end of initSecurityRoles(WebDeploymentDescriptor webDesc)


  /**
   * Accumulate security constraints from web descriptor.
   * <p><strong>Note:</strong> Called twice. First for global deployment descriptor
   * and later for the particular web application's descriptor.
   * </p>
   *
   * @param webDesc - a structure of common web-related descriptor data.
   */
  private void initSecurityConstraints(WebDeploymentDescriptor webDesc) {
    SecurityConstraintType[] secConstraintsTemp = webDesc.getSecConstraints();
    webAppContainsSecurityContraints = secConstraintsTemp != null && secConstraintsTemp.length > 0;
  }//end of initSecurityConstraints(WebDeploymentDescriptor webDesc)

  public boolean isWebAppWithSecurityConstraints() {
    return webAppContainsSecurityContraints;
  }

  /**
   * @param webDesc
   */
  private void initLocaleMappings(WebDeploymentDescriptor webDesc) {
    LocaleEncodingMappingListType localeMappingList =
      webDesc.getLocaleEncodingMappings();
    if (localeMappingList != null) {
      LocaleEncodingMappingType localeEncodings [] =
        localeMappingList.getLocaleEncodingMapping();
      if (localeEncodings != null) {
        for (int i = 0; i < localeEncodings.length; i++) {
          String locale = localeEncodings[i].getLocale();
          String encoding = localeEncodings[i].getEncoding();
          localeEncodingMappings.put(locale, encoding);
        }
      }
    }
  }

  private <T> List<T> concatenateArrays(T[] array1, T[] array2) {
    if (array1 == null && array2 == null) {
      return null;
    }

    if (array1 != null && array2 == null) {
      return new ArrayList<T>(Arrays.asList(array1));
    }

    if (array1 == null && array2 != null) {
      return new ArrayList<T>(Arrays.asList(array2));
    }

    List<T> result = new ArrayList<T>(Arrays.asList(array1));
    result.addAll(Arrays.asList(array2));
    return result;
  }//end of concatenateArrays(Object[] array1, Object[] array2)

  /**
   * Returns true if the version is specified with format N.N and
   * if it is earlier than 2.4.
   * If there is no version (null) this means that it is not converted,
   * i.e. the latest.
   *
   * @return true if version is earlier than 2.4, false otherwise
   */
  public boolean isJ2ee13OrLess() {
    return (specVersionDb != null) && (specVersionDb.doubleValue() < 2.4d);
  }

  public String getWebAppVersion() {
    return webAppVersion;
  }//end of getWebAppVersion()


  /**
   * Searches if there is run-as role defined for at least one servlet
   * @param servlets
   * @return true if there is servlet with such tag
   */
  private boolean isRunAsTagDefinedForSomeServlet(ServletType[] servlets) {
    if (servlets != null) {
      boolean runAsTagExists = false;
      for (int i = 0; i < servlets.length && !runAsTagExists; i++) {
        ServletType servletType = servlets[i];
        if (servletType != null) {
          RunAsType runAs = servletType.getRunAs();
          if (runAs != null && runAs.getRoleName() != null) {
            RoleNameType roleName = runAs.getRoleName();
            if (roleName != null) {
              if (roleName.get_value() != null) {
                runAsTagExists = true;
                return runAsTagExists;
              }
            }
          }
        }
      }
      return runAsTagExists;
    } else {
      return false;
    }
  } // isRunAsTagDefinedForSomeServlet(1)

}//end of class
