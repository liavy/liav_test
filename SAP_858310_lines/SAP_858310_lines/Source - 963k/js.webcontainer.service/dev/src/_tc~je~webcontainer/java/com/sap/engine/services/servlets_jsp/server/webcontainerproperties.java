/*
 * Copyright (c) 2005-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.frame.NestedProperties;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.runtime.RuntimeConfiguration;
import com.sap.engine.services.deploy.ear.common.EqualUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactoryImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.WebContainerParameters;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

public class WebContainerProperties extends RuntimeConfiguration {
  private static Location currentLocation = Location.getLocation(WebContainerProperties.class);
  private static Location traceLocation = LogContext.getLocationService();
  //property names
  private static final String COMPILE_ON_STARTUP = "CompileOnStartUp";
  private static final String INTERNAL_COMPILER = "InternalCompiler";
  private static final String EXTERNAL_COMPILER = "ExternalCompiler";
  private static final String SERVLETOUITPUTSTREAM_BUFFER_SIZE = "ServletOutputStreamBufferSize";
  private static final String DESTROY_TIME_OUT = "DestroyTimeOut";
  private static final String ENABLE_CHUNKED_RESPONSE = "EnableChunkedResponse";
  private static final String JSP_DEBUG_SUPPORT = "JSPDebugSupport";
  private static final String HEADER_FOR_NO_COMPRESSION = "HeaderForNoCompression";
  private static final String HEADER_FOR_COMPRESSION = "HeaderForCompression";
  private static final String MULTIPART_BODY_PARAMETER_NAME_KEY = "MultipartBodyParameterName";
  private static final String POOL_SIZE_KEY = "PoolSize";
  private static final String RESOLVE_HOST_NAME = "ResolveHostName";
  private static final String APPLICATION_STOPPED_FILE = "ApplicationStoppedFile";
  private static final String APPLICATION_STOPPED_ALIASES = "ApplicationStoppedAliases";
  private static final String REQUEST_DISPATCHER_OVER_WEB_INF = "RequestDispatcherOverWebInf";
  private static final String SUPPRESS_X_POWERED_BY = "SuppressXPoweredBy";
  private static final String ENABLE_MBEANS = "EnableMBeans";
  private static final String DISABLE_DYNAMIC_RESPONSE_CACHING = "DisableDynamicResponseCaching";
  private static final String REMOVE_CONTENT_LENGTH_FROM_304 = "RemoveContentLengthFrom304";
  private static final String JAVA_ENCODING = "JavaEncoding";
  private static final String PRODUCTION_MODE = "ProductionMode";
  /** The hard coded list with all WCE providers' names and theirs descriptors names. */
  private static final String WCE_PROVIDERS = "WebContainerExtensionsProviders";
  private static final String JACC_1_1_Authorization = "JACC11Authorization"; // remove after JACC migration
  private static final String CTS5_MODE = "CTS5Mode"; // not used anymore
  private static final String SESSION_PROLONGATION = "SessionProlongation"; // whether to check for session prolongation
  private static final String SESSION_IP_PROTECTION_ENABLED = "SessionIPProtectionEnabled"; // whether session ip protection is enabled
  private static final String SESSION_ID_REGENERATION_ENABLED = "SessionIdRegenerationEnabled"; // whether session regeneration is enabled
  private static final String SESSION_ID_CHANGE_ENABLED = "SessionIdChangeEnabled"; // whether session ID change is enabled
  // custom global application configurations - Global_app_config properties (override global descriptor and are overridden by local configurations (if any) -can be edited through the offline config editor */
  private static final String MAX_SESSIONS="Global_app_config/maxSessions";
  private static final String SESSION_TIMEOUT="Global_app_config/session_config/sessionTimeout";
  private static final String SESSION_COOKIE_PATH="Global_app_config/cookie_config/session_cookie/path";
  private static final String SESSION_COOKIE_DOMAIN="Global_app_config/cookie_config/session_cookie/domain";
  private static final String SESSION_COOKIE_MAXAGE="Global_app_config/cookie_config/session_cookie/max-age";
  private static final String APP_COOKIE_PATH="Global_app_config/cookie_config/application_cookie/path";
  private static final String APP_COOKIE_DOMAIN="Global_app_config/cookie_config/application_cookie/domain";
  private static final String APP_COOKIE_MAXAGE="Global_app_config/cookie_config/application_cookie/max-age";
  private static final String ERROR_PAGE_LOCATION = "Global_app_config/error_page/location";
  private static final String ERROR_PAGE_CONTEXT_ROOT = "Global_app_config/error_page/context_root";
  //all nested properties' names should be added to the following String array
  private static final String[] ALL_NESTED_PROPS = new String[]{MAX_SESSIONS, SESSION_TIMEOUT, SESSION_COOKIE_PATH, SESSION_COOKIE_DOMAIN, SESSION_COOKIE_MAXAGE, APP_COOKIE_PATH, APP_COOKIE_DOMAIN, APP_COOKIE_MAXAGE, ERROR_PAGE_LOCATION, ERROR_PAGE_CONTEXT_ROOT};

  private static final String USE_REQUEST_OBJECTS_POOL = "UseRequestObjectsPool";
  private static final String ENABLE_QoS_RESTRICTIONS_FOR_RD = "EnableQoSRestrictionsForRD";
  private static final String RD_THREAD_COUNT_FACTOR = "RDThreadCountFactor";
  private static final String ERROR500_MONITORS_CLEANUP_PERIOD = "Error500MonitorsCleanupPeriod";
  private static final String SECURITY_SESSION_ID_DOMAIN = "SecuritySessionIdDomain";
  private static final String DISABLE_QOS_STATISTICS_FOR_WCERD="DisableQoSStatisticsForWCERD";

  private boolean eclipseJspDebugSupport = true;
  private boolean compilerDebuggingInfo = false;
  public boolean eclipseSupport() {
    return eclipseJspDebugSupport;
  }
  public boolean compileWithDebugInfo() {
    return compilerDebuggingInfo;
  }

  // end of remove

  /**
   * Property do not exists by default.
   * Should be used only in very extreme situation which include customer escalations.
   * In case you add this property, you should restart the service manually.
   */
  private static final String EXTENDED_JSP_IMPORTS = "ExtendedJSPImport";

  private String[] appStoppedAliases = null;

  //property values
  private boolean internalCompiler = false;
  private String compiler = "javac";
  private long destroyTimeout = 5000;
  private int servletOutputStreamBufferSize = 32768;

  /**
   * If servlets will be compiled on starting of the service
   */
  private boolean compileOnStartUp = false;
  /**
   * Enable or disable support for JSR-045
   */
  private boolean jspDebugSupport = true;
  private boolean enableChunkedResponse = true;
  private String headerForNoCompression = "No-Compress";
  private String headerForCompression = null;
  private String multipartBodyParameterName = "com.sap.servlet.multipart.body";
  private int minPoolSize = 100;
  private int maxPoolSize = 5000;
  private int decreaseCapacityPoolSize = 200;
  private boolean resolveHostName = false;
  private String appStoppedFile = null;
  private boolean requestDispatcherOverWebInf = false;
  private boolean suppressXPoweredBy = true;
  private boolean disableDynamicResponseCaching = false;
  private boolean removeContentLengthFrom304 = true;
  private String javaEncoding = "UTF-8";
  private boolean productionMode = false;
  private boolean sessionProlongation = false;
  private boolean sessionIPProtectionEnabled = false;
  private boolean sessionIDRegenerationEnabled = false;
  private boolean sessionIDChangeEnabled = false;
  // Global_app_config properties */
  //The properties are all initialized with empty strings as an indication that they are not set by the end user. Hence the global descriptors' configurations will be globally applied
  private String sessionCookiePath = "";
  private String sessionCookieDomain = "";
  private String sessionCookieMaxAge ="";
  private String appCookiePath = "";
  private String appCookieDomain = "";
  private String appCookieMaxAge = "";
  private String maxSessions = "";
  private String sessionTimeout = "";
  private String errorPageLocation = Constants.ERROR_HANDLER_SERVLET;
  private String errorPageContextRoot = "";

  private boolean useRequestObjectsPool = true;
  private boolean enableQoSRestrictionsForRD = true;
  private int rdThreadCountFactor = 3;
  private long error500MonitorsCleanupPeriod = 24*60*60;
  private String securitySessionIdDomain = "sessionCookie";
  private boolean disableQoSStatisticsForWCERD = false;
  
  /**
   * The hard coded list with all WCE providers' names and theirs descriptors names.
   * The value of the WebContainerExtensionsProviders (WCE_PROVIDERS) property
   * is of type {{WCEProvName1, Descriptor1, Descriptor2...}, ...,}, e.g.
   * {{Portal, portalappl.xml},{WebDynpro,webdynpro.xml},{Portlet,portlet.xml}}
   */
  private Hashtable wceProviders = new Hashtable();
  /**
   * @deprecated - to be removed for AP7
   */
  private boolean extendedJspImports = false;

  public String javaEncoding() {
    return javaEncoding;
  }

  public boolean internalCompiler() {
    return internalCompiler;
  }

  public String getExternalCompiler() {
    return compiler;
  }

  public long getDestroyTimeout() {
    return destroyTimeout;
  }

  public int getServletOutputStreamBufferSize() {
    return servletOutputStreamBufferSize;
  }

  public boolean compileOnStartup() {
    return compileOnStartUp;
  }

  public boolean jspDebugSupport() {
    return jspDebugSupport;
  }

  public boolean chunkResponseEnabled() {
    return enableChunkedResponse;
  }

  public String headerForNoCompression() {
    return headerForNoCompression;
  }

  public String headerForCompression() {
    return headerForCompression;
  }

  public String getMultipartBodyParameterName() {
    return multipartBodyParameterName;
  }

  public int getMinPoolSize() {
    return minPoolSize;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public int getDecreaseCapacityPoolSize() {
    return decreaseCapacityPoolSize;
  }

  public boolean resolveHostName() {
    return resolveHostName;
  }

  public String appStoppedFile() {
    return appStoppedFile;
  }

  public String[] appStoppedAliases() {
    return appStoppedAliases;
  }

  public boolean getRequestDispatcherOverWebInf() {
    return requestDispatcherOverWebInf;
  }

  public boolean isSuppressXPoweredBy() {
    return suppressXPoweredBy;
  }

  public boolean getSessionProlongation() {
    return sessionProlongation;
  }

  public boolean getSessionIPProtectionEnabled() {
    return sessionIPProtectionEnabled;
  }

  public boolean getSessionIDRegenerationEnabled() {
    return sessionIDRegenerationEnabled;
  }

  public boolean getSessionIDChangeEnabled() {
    return sessionIDChangeEnabled;
  }

  public String getSessionCookiePath() {
    return sessionCookiePath;
  }

  public String getSessionCookieDomain() {
    return sessionCookieDomain;
  }

  public String getSessionCookieMaxAge() {
    return sessionCookieMaxAge;
  }

  public String getAppCookiePath() {
    return appCookiePath;
  }

  public String getAppCookieDomain() {
    return appCookieDomain;
  }

  public String getAppCookieMaxAge() {
    return appCookieMaxAge;
  }

  public String getMaxSessions() {
    return maxSessions;
  }

  public String getSessionTimeout() {
    return sessionTimeout;
  }

  public String getErrorPageLocation() {
    return errorPageLocation;
  }//end of getErrorHandlerName()

  public String getErrorPageContextRoot() {
    return errorPageContextRoot;
  }//end of getErrorHandlerContextRoot()

  public boolean isEnableQoSRestrictionsForRD() {
    return enableQoSRestrictionsForRD;
  }
  
  public int getRDThreadCountFactor() {
    return rdThreadCountFactor;
  }
  
  public long getError500MonitorsCleanupPeriod(){
	  return error500MonitorsCleanupPeriod;
  }
  
  public String getSecuritySessionIdDomain(){
	  return securitySessionIdDomain;
  }
  
  public boolean isDisableQoSStatisticsForWCERD() {
    return disableQoSStatisticsForWCERD;
  }
  
  /**
   * Updates service runtime changeable properties. The properties set must be applied or
   * rejected if some of the values is not acceptable
   *
   * @param sp a set of changed service properties
   * @throws ServiceException if there is incorrect value and the hole set is not applied
   */
  public void updateProperties(Properties sp) throws ServiceException {
	if (sp instanceof NestedProperties){
		  updateOnlyNestedProperties((NestedProperties) sp);
	}
	//this is for the rest of properties in the properties.xml that are actually non nested
	Enumeration propKeys = sp.keys();
    while (propKeys.hasMoreElements()) {
      String propName = (String) propKeys.nextElement();
      setServiceProperty(propName, sp.getProperty(propName));
    }
 }

  /**
   * Updates only the nested service properties declared in ALL_NESTED_PROPS.
   * In order to be updated correctly each new nested property needs to be added to this String array.
   * @param np
   */
  private void updateOnlyNestedProperties(NestedProperties np){
	  for (String nestedProp:ALL_NESTED_PROPS){
		  if (np.getProperty(nestedProp) != null){
			  setServiceProperty(nestedProp, np.getProperty(nestedProp));
		  }
	  }
  }

  /**
   * @deprecated should be used only updateProperties, but VisualAdmin still uses the setProperties() methods
   * @return needRestart
   */
  public boolean setServiceProperties(Properties sp) {
    boolean needRestart = false;
    Enumeration propKeys = sp.keys();
    while (propKeys.hasMoreElements()) {
      String propName = (String) propKeys.nextElement();
      if (setServiceProperty(propName, sp.getProperty(propName))) {
        needRestart = true;
      }
    }
    return needRestart;
  }

  public boolean setServiceProperty(String name, String value) {
    boolean needRestart = false;
    if (name != null) {
      name = name.trim();
    }
    if (value != null) {
      value = value.trim();
    }
    boolean beDebug = traceLocation.beDebug();
    if (INTERNAL_COMPILER.equals(name)) {
      boolean tmp = Boolean.valueOf(value).booleanValue();
      if (tmp != internalCompiler) {
        needRestart = true;
      }
      internalCompiler = tmp;
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
      containerParameters.setInternalCompiler(internalCompiler());
    } else if (EXTERNAL_COMPILER.equals(name)) {
      if (!compiler.equals(value)) {
        needRestart = true;
      }
      compiler = value;
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
      containerParameters.setExternalCompiler(getExternalCompiler());
    } else if (JAVA_ENCODING.equals(name)) {
      if (value != null) {
        value = value.trim();
        if (value.length() > 0) {
          if (!javaEncoding.equals(value)) {
            try {
              byte buffer[] = {(byte) 'a'};
              new String(buffer, value);
              javaEncoding = value;
              needRestart = true;
            } catch (java.io.UnsupportedEncodingException e) {
              LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000171",
                "Incorrect value {0} for the property [{1}]. Will use the default UTF-8.", new Object[]{value, JAVA_ENCODING}, e, null, null);
              javaEncoding = "UTF-8";
            }
          }
        }
      }
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
      containerParameters.setJavaEncoding(javaEncoding());
    } else if (DESTROY_TIME_OUT.equals(name)) {
      try {
        destroyTimeout = Integer.parseInt(value);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000172",
          "Incorrect value {0} for the property [{1}]. Will use the default 5000.", new Object[]{value, DESTROY_TIME_OUT}, e, null, null);
        destroyTimeout = 5000;
      }
    } else if (SERVLETOUITPUTSTREAM_BUFFER_SIZE.equals(name)) {
      try {
        servletOutputStreamBufferSize = Integer.parseInt(value);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000572",
          "Incorrect value {0} for the property {1}. Will use the default 32768.",
          new Object[]{value, SERVLETOUITPUTSTREAM_BUFFER_SIZE}, e, null, null);
        servletOutputStreamBufferSize = 32768;
      }
    } else if (COMPILE_ON_STARTUP.equals(name)) {
      compileOnStartUp = Boolean.valueOf(value).booleanValue();
    } else if (JSP_DEBUG_SUPPORT.equals(name)) {
      jspDebugSupport = Boolean.valueOf(value).booleanValue();
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
      containerParameters.setJspDebugSupport(jspDebugSupport());
    } else if (ENABLE_CHUNKED_RESPONSE.equals(name)) {
      enableChunkedResponse = Boolean.valueOf(value).booleanValue();
    } else if (HEADER_FOR_NO_COMPRESSION.equals(name)) {
      headerForNoCompression = value;
    } else if (HEADER_FOR_COMPRESSION.equals(name)) {
      headerForCompression = value;
    } else if (MULTIPART_BODY_PARAMETER_NAME_KEY.equals(name)) {
      if (value != null) {
        value = value.trim();
        if (value.length() == 0) {
          value = null;
        }
      }
      multipartBodyParameterName = value;
    } else if (POOL_SIZE_KEY.equals(name)) {
      try {
        boolean parsedSuccesfully = false;
        if (value.startsWith("{") && value.endsWith("}")) {
          value = value.substring(value.indexOf('{') + 1, value.lastIndexOf('}'));
          StringTokenizer valueST = new StringTokenizer(value, ",");
          if (valueST.countTokens() == 3) {
            int tempValue = Integer.valueOf(valueST.nextToken().trim()).intValue();
            if (tempValue != minPoolSize) {
              minPoolSize = tempValue;
              needRestart = true;
            }

            tempValue = Integer.valueOf(valueST.nextToken().trim()).intValue();
            if (tempValue != maxPoolSize) {
              maxPoolSize = tempValue;
              needRestart = true;
            }

            tempValue = Integer.valueOf(valueST.nextToken().trim()).intValue();
            if (tempValue != decreaseCapacityPoolSize) {
              decreaseCapacityPoolSize = tempValue;
              needRestart = true;
            }
            if (minPoolSize < 0 || maxPoolSize <= 0 || decreaseCapacityPoolSize <= 0 || maxPoolSize <= minPoolSize) {
              parsedSuccesfully = false;
            }
            parsedSuccesfully = true;
          }
        }
        if (!parsedSuccesfully) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000173",
            "Incompatible value of the property [{0}]. The default value {100, 5000, 200} will be used.", new Object[]{name}, null, null);
          minPoolSize = 100;
          maxPoolSize = 5000;
          decreaseCapacityPoolSize = 200;
          needRestart = true;
        }
      } catch (NumberFormatException t) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000174",
          "Incompatible value of the property {0}. The default value {100, 5000, 200} will be used.", new Object[]{name}, t, null, null);
        minPoolSize = 100;
        maxPoolSize = 5000;
        decreaseCapacityPoolSize = 200;
        needRestart = true;
      }
    } else if (RESOLVE_HOST_NAME.equals(name)) {
      resolveHostName = Boolean.valueOf(value).booleanValue();
    } else if (APPLICATION_STOPPED_FILE.equals(name)) {
      if (value != null && value.length() == 0) {
        value = null;
      }
      appStoppedFile = value;
    } else if (APPLICATION_STOPPED_ALIASES.equals(name)) {
      if (value != null) {
        value = value.trim();
        if (value.length() == 0) {
          appStoppedAliases = null;
        } else {
          Vector vAliases = new Vector();
          StringTokenizer alias = new StringTokenizer(value, ",");

          while (alias.hasMoreElements()) {
            vAliases.add(((String) alias.nextElement()).trim());
          }
          if (vAliases.size() > 0) {
            String[] aliases = new String[vAliases.size()];
            vAliases.copyInto(aliases);
            appStoppedAliases = aliases;
          } else {
            appStoppedAliases = null;
          }
        }
      } else {
        appStoppedAliases = null;

      }
    } else if (REQUEST_DISPATCHER_OVER_WEB_INF.equals(name)) {
      requestDispatcherOverWebInf = Boolean.valueOf(value).booleanValue();
    } else if (SUPPRESS_X_POWERED_BY.equals(name)) {
      suppressXPoweredBy = Boolean.valueOf(value).booleanValue();
    } else if (ENABLE_MBEANS.equals(name)) {
    	if (beDebug) {
				//Removed property since SP3
				traceLocation.debugT("Property setting ignored. Not supported. Name: [" + name + "], value: [" + value + "].");
			}
			needRestart = false;
    } else if (DISABLE_DYNAMIC_RESPONSE_CACHING.equals(name)) {
      disableDynamicResponseCaching = new Boolean(value).booleanValue();
    } else if (REMOVE_CONTENT_LENGTH_FROM_304.equals(name)) {
      removeContentLengthFrom304 = new Boolean(value).booleanValue();
    } else if (WCE_PROVIDERS.equals(name)) {
      if (!checkNewProperty(value)) {
        setWCEProviders(value);
        needRestart = true;
      }
    } else if (EXTENDED_JSP_IMPORTS.equals(name)) {
      boolean tmp = Boolean.valueOf(value).booleanValue();
      if (tmp != extendedJspImports) {
        extendedJspImports = tmp;
        needRestart = true;
      }
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
			containerParameters.setExtendedJspImports(isExtendedJspImports());
    } else if (PRODUCTION_MODE.equals(name)) {
      productionMode = Boolean.valueOf(value).booleanValue();
      WebContainerParameters containerParameters = ((JspParserFactoryImpl)JspParserFactory.getInstance()).getContainerProperties();
			containerParameters.setProductionMode(isProductionMode());
    } else if (JACC_1_1_Authorization.equals(name)) {
      if (beDebug) {
				//jacc11Authorization = Boolean.valueOf(value).booleanValue();
				traceLocation.debugT("Property setting ignored. Not supported. Name: [" + name + "], value: [" + value + "].");
			}
			needRestart = false;
    } else if (CTS5_MODE.equals(name)) { // not used
      if (beDebug) {
				//isCTS5Mode = Boolean.valueOf(value).booleanValue();
				traceLocation.debugT("Property setting ignored. Not supported. Name: [" + name + "], value: [" + value + "].");
			}
			needRestart = false;
    } else if (SESSION_PROLONGATION.equals(name)) {
      sessionProlongation = Boolean.valueOf(value).booleanValue();
      if (beDebug) {
        traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
      }
      needRestart = false;
    } else if (SESSION_IP_PROTECTION_ENABLED.equals(name)) {
        sessionIPProtectionEnabled = Boolean.valueOf(value).booleanValue();
        com.sap.engine.session.runtime.SessionRequest.setCheckIpEnabled(sessionIPProtectionEnabled);
        if (beDebug) {
          traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
        }
        needRestart = false;
    } else if (SESSION_ID_REGENERATION_ENABLED.equals(name)) {
        sessionIDRegenerationEnabled = Boolean.valueOf(value).booleanValue();
        com.sap.engine.session.runtime.SessionRequest.setCheckMarkIdEnabled(sessionIDRegenerationEnabled);
        if (beDebug) {
          traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
        }
        needRestart = false;
    } else if (SESSION_ID_CHANGE_ENABLED.equals(name)) {
        sessionIDChangeEnabled = Boolean.valueOf(value).booleanValue();
        if (beDebug) {
          traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
        }
        needRestart = false;
    }else if (MAX_SESSIONS.equals(name)){
    	if (value.equals("") || checkProp_toInt(value)){
    		maxSessions = value;
    		needRestart = true;
    		if (beDebug){
    			traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    		}
    	}else{ //value is not correctly converted to int
    		//TODO: set the msgId correctly
    		LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000628",
    	            "Incompatible value of the property [{0}]. In order the custom setting to take effect please set the property with a correct int value.", new Object[]{name}, null, null);
    	}
    } else if (SESSION_TIMEOUT.equals(name)){
    	if (value.equals("") || checkProp_toInt(value)){
    		sessionTimeout = value;
    		needRestart = true;
    		if (beDebug){
    			traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    		}
    	}else{
    		LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000629",
    	            "Incompatible value of the property [{0}]. In order the custom setting to take effect please set the property with a correct int value.", new Object[]{name}, null, null);
    	}
    } else if (SESSION_COOKIE_PATH.equals(name)){
    	sessionCookiePath = value;
		needRestart = true;
    	if (beDebug){
    	  traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    	}
    } else if (SESSION_COOKIE_DOMAIN.equals(name)){
    	sessionCookieDomain = value;
		needRestart = true;
    	if (beDebug){
    	  traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    	}
    } else if (SESSION_COOKIE_MAXAGE.equals(name)){
    	if (value.equals("") || checkProp_toInt(value)){
    		sessionCookieMaxAge = value;
    		needRestart = true;
    		if (beDebug){
    			traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    		}
    	}else{
    		LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000630",
    	            "Incompatible value of the property [{0}]. In order the custom setting to take effect please set the property with a correct int value.", new Object[]{name}, null, null);
    	}
    } else if (APP_COOKIE_PATH.equals(name)){
    	appCookiePath = value;
		needRestart = true;
    	if (beDebug){
    	  traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    	}
    } else if (APP_COOKIE_DOMAIN.equals(name)){
    	appCookieDomain = value;
		needRestart = true;
    	if (beDebug){
    	  traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    	}
    }else if (APP_COOKIE_MAXAGE.equals(name)){
    	if (value.equals("") || checkProp_toInt(value)){
    		appCookieMaxAge = value;
    		needRestart = true;
    		if (beDebug){
    			traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    		}
    	}else{
    		LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000631",
    	            "Incompatible value of the property [{0}]. In order the custom setting to take effect please set the property with a correct int value.", new Object[]{name}, null, null);
    	}
    } else if (USE_REQUEST_OBJECTS_POOL.equals(name)) {
      useRequestObjectsPool = new Boolean(value).booleanValue();
    } else if (ERROR_PAGE_LOCATION.equals(name)) {
      errorPageLocation = value;
      if (beDebug){
        traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
      }
      if (errorPageLocation != null && !errorPageLocation.equals("") && !errorPageLocation.startsWith("/")) {
        errorPageLocation = "/" + errorPageLocation;
      }
    } else if (ERROR_PAGE_CONTEXT_ROOT.equals(name)) {
      errorPageContextRoot = value;
      if (beDebug){
        traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
      }
    } else if (ENABLE_QoS_RESTRICTIONS_FOR_RD.equals(name)) {
      enableQoSRestrictionsForRD = new Boolean(value).booleanValue();
    } else if (RD_THREAD_COUNT_FACTOR.equals(name)) {
      try {
        rdThreadCountFactor = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000686",
          "Incompatible value [{0}] of the property [{1}]. In order the custom setting to take effect please set the property with a correct int value.", new Object[]{value, name}, null, null);
        rdThreadCountFactor = 3;
      }
    } else if (ERROR500_MONITORS_CLEANUP_PERIOD.equals(name)) {
        try {
          long formerValue = error500MonitorsCleanupPeriod;
          error500MonitorsCleanupPeriod = Long.parseLong(value);
          //if the value is changed - update the timeout listener's repeat time
          if ((formerValue != error500MonitorsCleanupPeriod)&&(ServiceContext.getServiceContext().getWebMonitoring() != null)
        		  && (ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted())){
        	  ServiceContext.getServiceContext().getWebMonitoring().changeEror500CleanupPeriod(error500MonitorsCleanupPeriod);
          }
        } catch (NumberFormatException e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000689",
            "Incompatible value [{0}] of the property [{1}]. In order the custom setting to take effect please set the property with a correct long value.", new Object[]{value, name}, null, null);
        }
     } else if (SECURITY_SESSION_ID_DOMAIN.equals(name)){
    	if ("sessionCookie".equalsIgnoreCase(value) || "NONE".equalsIgnoreCase(value)){
    		securitySessionIdDomain = value;
    		if (beDebug){
    	        traceLocation.debugT("Property set. Name: [" + name + "], value: [" + value + "].");
    	    }
    	}else{
    	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000711",
    	    "Incompatible value [{0}] of the property [{1}]. In order the custom setting to take effect please " +
    	    "choose one of the two possible values for this property: sessionCookie or NONE.", new Object[]{value, name}, null, null);
    	}
    }else if (DISABLE_QOS_STATISTICS_FOR_WCERD.equals(name)){
      disableQoSStatisticsForWCERD = new Boolean(value).booleanValue();
    }else {
    
    	//todo return logWarning
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000227",
        "Unknown property read. Name: [{0}], value: [{1}].", new Object[]{name, value}, null, null);
    }
    return needRestart;
  }

  /**
   * The property indicates if service HTTP headers will be sent for dynamic responses.
   * If true Cache-control: no-cache and Expires:${yesterday} will be set in the response if no other cache mechanism is specified by the client application.
   *
   * @return Returns the disableDynamicResponseCaching.
   */
  public boolean isDisableDynamicResponseCaching() {
    return disableDynamicResponseCaching;
  }

  /**
   * @param disableDynamicResponseCaching The disableDynamicResponseCaching to set.
   */
  public void setDisableDynamicResponseCaching(boolean disableDynamicResponseCaching) {
    this.disableDynamicResponseCaching = disableDynamicResponseCaching;
  }

  /**
   * If the property is true removes Content-Length from 204,304, 10*
   * The property is installed on demand i.e. it is not present in webcontainer.properties file.
   *
   * @return
   */
  public boolean isRemoveContentLengthFrom304() {
    return removeContentLengthFrom304;
  }

  /**
   * If the property is true removes Content-Length from 204,304, 10*
   * The property is installed on demand i.e. it is not present in webcontainer.properties file.
   *
   * @param removeContentLengthFrom304
   */
  public void setRemoveContentLengthFrom304(boolean removeContentLengthFrom304) {
    this.removeContentLengthFrom304 = removeContentLengthFrom304;
  }

  public Hashtable getWCEProviders() {
    return wceProviders;
  }//end of getWCEProviders()

  /**
   * Applies the new value <code>wce</code> to the WebContainerExtensionsProviders property
   * @param wce	the new value of type {{WCEProvName, WCEDescriptor1, ...} , ...}
   */
  public void setWCEProviders(String wce) {
    Hashtable newvalue = new Hashtable();
    int iBegin = wce.indexOf("{");
    int iEnd = wce.lastIndexOf("}");

    if (iBegin == -1 && iEnd == -1) {
      return;
    }

    if (iBegin == -1 || iEnd == -1) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.WCE_MAPPINGS_SHOULD_START_WITH_AND_END_WITH, new Object[]{"{", "}"});
    }

    String sRestList = wce.substring(iBegin + 1, iEnd);

    while (sRestList.indexOf("{") != -1) {
      int iElemBegin = sRestList.indexOf("{");
      int iElemEnd = sRestList.indexOf("}");

      if (iElemEnd != -1) {
        String sSubElem = sRestList.substring(iElemBegin + 1, iElemEnd);
        sRestList = sRestList.substring(iElemEnd + 1);

        Pattern p = Pattern.compile(",");
        String[] items = p.split(sSubElem);
        Vector mapping = new Vector();
        for (int i = 1; i < items.length; i++) {
          mapping.addElement(items[i].trim());
        }

        newvalue.put(items[0].trim(), mapping);
      }
    }

    wceProviders = newvalue;
  }//end of setWCEProviders(String[][] wce)

  /**
   * Checks the syntax and whether the new value of WebContainerExtensionsProviders
   * property is the same, as the already set one, in which case returns true.
   * @param value
   * @return false if it is a new value and has to be applied,
   * 		i.e. the new value has new mapping or has a changed mapping;
   * 		true otherwise
   * @throws WebIllegalArgumentException if the value is not correct,
   * 		except for the case when there are neither starting nor ending curly brackets,
   *    in which case it returns true.
   */
  private boolean checkNewProperty(String value) {
    int iBegin = value.indexOf("{");
    int iEnd = value.lastIndexOf("}");

    if (iBegin == -1 && iEnd == -1) {
      return true;
    }

    if (iBegin == -1 || iEnd == -1) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.WCE_MAPPINGS_SHOULD_START_WITH_AND_END_WITH, new Object[]{"{", "}"});
    }

    String sRestList = value.substring(iBegin + 1, iEnd);
    int counter = 0;
    while (sRestList.indexOf("{") != -1) {
      int iElemBegin = sRestList.indexOf("{");
      int iElemEnd = sRestList.indexOf("}");

      if (iElemEnd != -1) {
        counter++;
        String sSubElem = sRestList.substring(iElemBegin + 1, iElemEnd);
        sRestList = sRestList.substring(iElemEnd + 1);

        Pattern p = Pattern.compile(",");
        String[] items = p.split(sSubElem);
        Vector newvec = new Vector();
        for (int i = 1; i < items.length; i++) {
          newvec.addElement(items[i].trim());
        }

        if (wceProviders.containsKey(items[0].trim())) {
          Vector current = (Vector) wceProviders.get(items[0].trim());
          if (!EqualUtils.equalVectors(current, newvec)) {
            return false;
          }
        } else {
          return false;
        }
      }
    }

    if (counter != wceProviders.size()) {
      return false;
    }

    return true;
  }//end of checkNewProperty(String value)

  /**
   * Property do not exists by default.
   * Should be used only in very extreme situation which include customer escalations.
   * @return
   * @deprecated - to be removed for AP7
   */
  public boolean isExtendedJspImports() {
    return extendedJspImports;
  }

  /**
   * Production mode concerns JSP processing. When a class file is already compiled and production mode is true,
   * then no check for JSP file modification is performed.
   *
   * @return -
   */
  public boolean isProductionMode() {
    return productionMode;
  }

  public boolean isUseRequestObjectPools() {
    return useRequestObjectsPool;
  }

//returns true if the property has correct int value
private boolean checkProp_toInt (String propValue){
	try{
		Integer.parseInt(propValue);
		return true;
	}catch (NumberFormatException nfe){
		return false;
	}
}

}

