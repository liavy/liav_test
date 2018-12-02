/** Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.admin;

// TODO Change the name of Servlet implementation to differ from Servlet interface -- ServletImpl or ServletMBean ?

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.openmbean.OpenDataException;

import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeActionStatus;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeEJBLocalReferenceSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeEJBRemoteReferenceSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeEnvironmentEntrySettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeMessageDestinationReferenceSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeMessageDestinationSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeProperty;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeResourceReferenceSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeServletMappingSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeServletSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebCookieSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebErrorPageSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebFilterMappingSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebFilterSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebJSPPropertyGroupSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebListenerSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebMIMEMappingSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebModuleSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebModule_Adapter;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebResponseStatusSettings;
import com.sap.engine.admin.model.itsam.jsr77.web.SAP_ITSAMJ2eeWebTaglibSettings;
import com.sap.engine.admin.model.jsr77.JSR77ObjectNameFactory;
import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieConfigType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieTypeType;
import com.sap.engine.lib.descriptors.webj2eeengine.ResponseStatusType;
import com.sap.engine.lib.descriptors.webj2eeengine.ServerComponentRefType;
import com.sap.engine.lib.descriptors.webj2eeengine.WebJ2EeEngineType;
import com.sap.engine.lib.descriptors5.javaee.DescriptionType;
import com.sap.engine.lib.descriptors5.javaee.DisplayNameType;
import com.sap.engine.lib.descriptors5.javaee.EjbLocalRefType;
import com.sap.engine.lib.descriptors5.javaee.EjbRefType;
import com.sap.engine.lib.descriptors5.javaee.EnvEntryType;
import com.sap.engine.lib.descriptors5.javaee.ListenerType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationRefType;
import com.sap.engine.lib.descriptors5.javaee.MessageDestinationType;
import com.sap.engine.lib.descriptors5.javaee.ParamValueType;
import com.sap.engine.lib.descriptors5.javaee.PathType;
import com.sap.engine.lib.descriptors5.javaee.ResourceEnvRefType;
import com.sap.engine.lib.descriptors5.javaee.ResourceRefType;
import com.sap.engine.lib.descriptors5.javaee.UrlPatternType;
import com.sap.engine.lib.descriptors5.javaee.XsdIntegerType;
import com.sap.engine.lib.descriptors5.javaee.XsdStringType;
import com.sap.engine.lib.descriptors5.web.DispatcherType;
import com.sap.engine.lib.descriptors5.web.ErrorPageType;
import com.sap.engine.lib.descriptors5.web.FilterMappingType;
import com.sap.engine.lib.descriptors5.web.FilterType;
import com.sap.engine.lib.descriptors5.web.JspConfigType;
import com.sap.engine.lib.descriptors5.web.JspPropertyGroupType;
import com.sap.engine.lib.descriptors5.web.LocaleEncodingMappingListType;
import com.sap.engine.lib.descriptors5.web.LocaleEncodingMappingType;
import com.sap.engine.lib.descriptors5.web.MimeMappingType;
import com.sap.engine.lib.descriptors5.web.ServletMappingType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.SessionConfigType;
import com.sap.engine.lib.descriptors5.web.TaglibType;
import com.sap.engine.lib.descriptors5.web.WelcomeFileListType;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.DeployContext;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.WebApplicationConfig;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.RuntimeChangesAction;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.tc.logging.Location;

/**
 * Disclaimer: This class was written in a hurry, and by more than one person. It is expected to be fixed some day.
 *
 * @author Petar Petrov (I030687)
 * @author Vera Buchkova
 * 
 * @version 7.2	30 Aug 2006
 */
public class ITSAMWebModule extends SAP_ITSAMJ2eeWebModule_Adapter {

  // constants //

  public static final String DEFAULT_MODIFICATION_FLAG = "-1";
  public static final String GLOBAL_DD_WEB_MODULE = "SAP_WebContainer_GlobalDD_WebModule";
  public static final String GLOBAL_DD_APPLICATION = "SAP_WebContainer_GlobalDD_Application";
  
  private static Location traceLocation = LogContext.getLocationWebadmin();
  private static Location currentLocation = Location.getLocation(ITSAMWebModule.class);
  // static fields //
  
  // fields //
  private WebContainerLazyMBeanProvider provider = null;
  
  private String appName;
  private String alias;
  private String webAliasDir;
  private ObjectName objectName = null;
  //private String webXmlFileName = null; // Used to load the descriptor //Removed because will use the DD from configuration
  private String deploymentDescriptor = null; // Lazy init to save memory
  private SAP_ITSAMJ2eeWebModuleSettings localCompositeData = null;
  private SAP_ITSAMJ2eeWebModuleSettings globalCompositeData = null;
  private Object compositeDataLocker = null;
  private boolean thisIsModelForTheGlobalDD = false;
  private String caption;
  private String internalName;
  private ObjectName[] servlets = null; //not servlet's MBeans, because of lazy loading
  private ObjectName[] javaMailSessionResources = null;
  private ObjectName[] urlResources = null;

  // public static methods //

  // constructors //
  
  /**
   * Constructs a new mbean object for this web module.
   * @param objectName	full object name of this web module
   * @param appName	application name of this web module
   * @param alias	web alias of the web module; same as in the objectName
   * @param provider	the already initialized provider; it's needed: 
   * 	1. To obtain ManagementModelManager for getJavaVMs() and getServer() methods; 
   *  2. To obtain the WebContainer singleton object, where the globalcomposite 
   *  data is stored and shared among all web module's mbeans;
   *  3. To construct ObjectName objects for the servlets (and URL and JavaMail resources),
   *  for which the initialized provider uses static query parts. 
   * @throws InstanceAlreadyExistsException
   * @throws MBeanRegistrationException
   * @throws NotCompliantMBeanException
   */
  public ITSAMWebModule(ObjectName objectName, String appName, String alias, WebContainerLazyMBeanProvider provider) 
  																	throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    // super: (String internalName, String caption, ManagementModelManager mmManager)

    ensureValidApplication(appName);
    this.appName = appName;

    ensureValidAlias(alias);
    this.alias = alias;
    
    internalName = JSR77ObjectNameFactory.getWEBModuleName(alias, appName);
    this.caption = alias;
    
    this.provider = provider;

    compositeDataLocker = new Object();
    
    init();

    this.objectName = objectName;
  }

  // public methods //

  // JSR77 methods

  public String getName() {
    return internalName;
  }

  public String getCaption() {
    return caption;
  }


  public String[] getJavaVMs() {
		List<String> result = new ArrayList<String>();
		
		Set<ObjectName> jvmNames = Collections.EMPTY_SET;
		
		try {
			jvmNames = provider.getMBS().queryNames(
					new ObjectName("*:j2eeType=JVM,*"),(QueryExp)null);
		} catch (MalformedObjectNameException e) { //$JL-EXC$
			//Excluded because thrown by new ObjectName() when the parameter does not have the right format.
		}
		
		// filter all names into the result
		for (ObjectName jvm:jvmNames) {
			result.add(jvm.toString());
		}
		return result.toArray(new String[0]);
	}
	

	public String getServer() {
		ObjectName name = getServerObjectName();
		if (name != null) {
			return name.toString();
		} else {
			return null;
		}
	}
	

	private ObjectName getServerObjectName() {
		Set<ObjectName> serverNames = Collections.EMPTY_SET;
		
		try {
			serverNames = provider.getMBS().queryNames(
					new ObjectName("*:cimclass=SAP_ITSAMJ2eeCluster,*"),(QueryExp)null);
		} catch (MalformedObjectNameException e) { //$JL-EXC$
			//Excluded because thrown by new ObjectName() when the parameter is null.
		} catch (NullPointerException e) { //$JL-EXC$
			//Excluded because thrown by new ObjectName() when the parameter does not have the right format.
		}
		for (ObjectName server:serverNames) {
			return server;
		}
		return null;
	}

  static String[] onSet2StringArray(Set set) {
    int size = set.size();
    String names[] = new String[size];
    int index = 0;
    for (Iterator i = set.iterator(); i.hasNext();) {
      names[index++] = ((ObjectName) i.next()).toString();
    }
    return names;
  }

  /**
   * Returns an array of servlet names contained in the deployed WAR module. 
   * The Servlet's mbeans are not loaded here because of the lazy loading mechanism;
   * they will be loaded when requested.
   *
   * @return an array of the servlets' names of this web module
   */
  public String[] getServlets() {
  	List<String> result = new LinkedList<String>();

    // Iterate over the list and get all Servlet object names
    for (int i = 0; i < servlets.length; i++) {
    	result.add(servlets[i].toString());
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

  public String getdeploymentDescriptor() {
    return getDeploymentDescriptor();
  }

  /**
   * @see com.sap.engine.admin.model.jsr77.J2EEDeployedObject#getdeploymentDescriptor()
   */
  public String getDeploymentDescriptor() {
    if (deploymentDescriptor == null) {
      loadDeploymentDescriptor();
    }
    return deploymentDescriptor;
  }

  // GeneralOperations implementation

  public SAP_ITSAMJ2eeWebModuleSettings getLocalSettings() {
    return localCompositeData; // TODO make it return compositeData.deepClone();
  }

  public SAP_ITSAMJ2eeWebModuleSettings getGlobalSettings() {
    return globalCompositeData; // TODO make it return compositeData.deepClone();
  }

  public SAP_ITSAMJ2eeActionStatus ApplyChanges(SAP_ITSAMJ2eeWebModuleSettings settings) {
    SAP_ITSAMJ2eeActionStatus status = new SAP_ITSAMJ2eeActionStatus();
    status.setCode(SAP_ITSAMJ2eeActionStatus.OK_CODE);
    Properties changes = null;
    try {
      if (settings == null) {
        status.setCode(SAP_ITSAMJ2eeActionStatus.ERROR_CODE);
        status.setMessageId("CD is null");
        return status;
      }

      //update configuration
      WebDeploymentDescriptor webDeploymentDescriptor = getDescriptorFromCD(settings);
      synchronized (this) {
        changes = RuntimeChangesAction.makeRuntimeChanges(appName, webDeploymentDescriptor, DEFAULT_MODIFICATION_FLAG, alias, true);
        //update the model on this server node:
        setSAP_ITSAMJ2eeWebModuleSettingsForCData(settings);
      }
    } catch (WebDeploymentException ex) {
      if (WebDeploymentException.MODEL_ALREADY_CHANGED.equals(ex.getMessage())) {
        status.setCode(SAP_ITSAMJ2eeActionStatus.ERROR_CODE);  //TODO status dirty, need_refresh, ?
        status.setMessageId("Concurrent model modification");
        return status;
      } else {
        status.setCode(SAP_ITSAMJ2eeActionStatus.ERROR_CODE);
        //TODO:Polly type:ok
        logError("ASJ.web.000229", "Web Deployment error occurred when saving application properties.", ex);
      }
    } catch (Exception ex) {
      status.setCode(SAP_ITSAMJ2eeActionStatus.ERROR_CODE);
      //TODO:Polly type:ok
      logError("ASJ.web.000230", "Error occurred when saving application properties.", ex);
      //TODO throw exception: ex
    }
    
		//Logs changed properties in the change log!!!
    if (changes != null && !changes.isEmpty()) {
			Enumeration propertyNames = changes.propertyNames();
			while (propertyNames.hasMoreElements()) {
				String name = (String) propertyNames.nextElement();
				String value = changes.getProperty(name);
				LogContext.getCategory(LogContext.CATEGORY_CHANGE_LOG_PROPERTIES).logInfo(currentLocation, "ASJ.web.000596",
						"Property [{0}] for [{1}] web application was changed. Old version [{2}], new version [{3}].", 
						new Object[]{name, alias, value.substring(0, value.indexOf(",")), value.substring(value.indexOf(",") + 1)}, null, null);
			}
		}
    
		return status;
  }

  // Other public methods
  
  public String getApplicationName() {
  	return appName;
  }

  public String getAlias() {
    return alias;
  }

  public String getRegisteredObjectName() {
    return objectName == null ? null : objectName.toString();
  }

  // package methods //

  // protected methods //

  // private static methods //

  private static void ensureValidApplication(String appName) {
    if (appName == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Method_called_with_null_value_name,
        new Object[]{"ensureValidApplication(appName is null)"});
    }
  }

  private static void ensureValidAlias(String alias) {
    if (alias == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Method_called_with_null_value_name,
        new Object[]{"ensureValidAlias(alias is null)"});
    }
  }

  private static String loadJ2EEDescription(DescriptionType[] descriptions) {
    String description = null;
    if (descriptions != null && descriptions.length > 0) {
      description = descriptions[0].get_value(); // TODO fix i18n
    }

    return description;
  }

  private static String loadJ2EEDisplayName(DisplayNameType[] displayNames) {
    String displayName = null;
    if (displayNames != null && displayNames.length > 0) {
      displayName = displayNames[0].get_value(); // TODO fix i18n
    }

    return displayName;
  }

  // private methods //

  private void init() {
    thisIsModelForTheGlobalDD = appName.equals(GLOBAL_DD_APPLICATION) && alias.equals(GLOBAL_DD_WEB_MODULE);

    ServiceContext serviceContext = ServiceContext.getServiceContext();
    DeployContext deployContext = serviceContext.getDeployContext();
    DeployCommunicator dc = deployContext.getDeployCommunicator();
    Configuration appConfig = null;
    Configuration servlet_jspConfig = null;

    // These are the web-module's deployment descriptors. They might remain null if thisIsModelForTheGlobalDD.
    WebDeploymentDescriptor localDesc = null;
    WebJ2EeEngineType localDesc2 = null;

    try {
      if (!thisIsModelForTheGlobalDD) {
      	appConfig = dc.getAppConfigurationForReadAccess(appName);
        servlet_jspConfig = appConfig.getSubConfiguration(Constants.CONTAINER_NAME);
        String appWorkDir = dc.getMyWorkDirectory(appName);
        // Escape any special characters in the dir name, have in mind the
        // '/' alias to replace it
        webAliasDir = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias)); 
        localDesc = ActionBase.loadWebDDObjectFromDBase(servlet_jspConfig, webAliasDir);
        localDesc2 = localDesc.getWebJ2EEEngine();
      }

      WebDeploymentDescriptor globalDesc = deployContext.getGlobalDD();
      WebJ2EeEngineType globalDesc2 = globalDesc.getWebJ2EEEngine();
      boolean forceReadOnly = thisIsModelForTheGlobalDD || localDesc.hasSubstitutionVariables(); //TODO: removed why?

      // Load the modificationFlagFromConfiguration from configuration
      String modificationFlagFromConfiguration = null;
      if (!thisIsModelForTheGlobalDD) {        
        //Check config entries added in RuntimeChangesAction.makeRuntimeChanges(...)
        if (servlet_jspConfig.existsSubConfiguration(Constants.ADMIN)) {
          Configuration adminConfig = servlet_jspConfig.getSubConfiguration(Constants.ADMIN);
          if (adminConfig.existsConfigEntry(Constants.MODIFICATION_FLAG)) {
            modificationFlagFromConfiguration = (String) adminConfig.getConfigEntry(Constants.MODIFICATION_FLAG);
          }
        }
      }

      // Make sure we don't leave with a null lastModified flag.
      // If no flag is found from the configuration it means
      // that (1) thisIsModelForTheGlobalDD or
      // (2) the web-module was deployed and never edited or updated
      if (modificationFlagFromConfiguration == null || modificationFlagFromConfiguration.length() == 0) {
        modificationFlagFromConfiguration = DEFAULT_MODIFICATION_FLAG;
      }

      // JSR77 - TODO: check whether the servlets and resources must be loaded here or lazy only!!!
      servlets = loadServlets(localDesc, globalDesc); //former loadServlets
      //loadJsr77ResourcesNames(localDesc); //deprecated since lazy beans resources are loaded lazy

      // WebAdmin
      // Load local settings
      String webModuleDislayName = localDesc == null ? null : loadJ2EEDisplayName(localDesc.getDisplayNames());
      boolean webModuleDistributable = localDesc == null ? globalDesc.getDistributable() : localDesc.getDistributable();
      String failoverMessage = null;
      //TODO: check - This is never NULL?! and then they will always be loaded, because WebDeploymentDescriptor gets initialized with the default values
      if (localDesc2 != null && localDesc2.getFailOverAlert() != null) {
        // Try load from local
        failoverMessage = localDesc2.getFailOverAlert().getMessage();
      } else {
        failoverMessage = WebDeploymentDescriptor.FAILOVER_MESSAGE_DEFAULT;
      }
      int failoverTimeout;
      if (localDesc2 != null && localDesc2.getFailOverAlert() != null) {
        // Try load from local
        failoverTimeout = localDesc2.getFailOverAlert().getTimeout().intValue();
      } else {
        failoverTimeout = WebDeploymentDescriptor.FAILOVER_TIMEOUT_DEFAULT;
      }

      Integer sessionTimeout = null;
      Boolean urlSessionTracking = null;
      Integer maxSessions = null;

      if (!thisIsModelForTheGlobalDD) {
        localCompositeData = new SAP_ITSAMJ2eeWebModuleSettings(
          webModuleDislayName,
          webModuleDistributable,
          failoverMessage,
          failoverTimeout,
          loadJ2EEEJBRemoteReferences(localDesc),
          loadJ2EEEnvironmentEntries(localDesc),
          loadJ2EEMessageDestinations(localDesc),
          loadJ2EEMessageDestinationReferences(localDesc),
          loadJ2EEResourceEnvironmentReferences(localDesc),
          loadJ2EEResourceReferences(localDesc),
          loadJ2EEServerComponentReferences(localDesc2),
          loadWelcomeFiles(localDesc),
          loadParams(localDesc.getContextParams()),
          loadJ2EEEJBLocalReferences(localDesc),
          loadFilters(localDesc),
          loadFilterMappings(localDesc),
          loadListeners(localDesc),
          loadServletsData(localDesc),
          loadServletMappings(localDesc),
          loadMimeMappings(localDesc),
          loadErrorPages(localDesc),
          loadTaglibs(localDesc),
          loadJspPropertyGroups(localDesc),
          loadLocaleEncodingMappings(localDesc),
          loadResponseStatuses(localDesc2),
          null,
          localDesc == null ? null : loadJ2EEDescription(localDesc.getDescriptions()),
          null);

        if (localDesc != null && localDesc.getSessionConfig() != null && localDesc.getSessionConfig().getSessionTimeout() != null) {
          // Get sessionTimeout from local
          // TODO clarify here if BigInteger --> Integer is okay
          sessionTimeout = new Integer(localDesc.getSessionConfig().getSessionTimeout().get_value().intValue());
        }
        localCompositeData.setSessionTimeout(sessionTimeout == null ? WebApplicationConfig.DEFAULT_SESSTION_TIMEOUT : sessionTimeout.intValue());

        // Load urlSessionTracking
        if (localDesc2 != null) {
          // Try load from local
          urlSessionTracking = localDesc2.getUrlSessionTracking();
        }
        localCompositeData.setURLSessionTracking(urlSessionTracking == null ? WebDeploymentDescriptor.URL_SESSION_TRACKING_DEFAULT : urlSessionTracking.booleanValue());

        // Load maxSessions
        if (localDesc2 != null) {
          // Try load from local
          maxSessions = localDesc2.getMaxSessions();
        }
        //TODO: check this: it used to be 0, but the default value for failover timeout in xsd is WebDeploymentDescriptor.MAX_SESSIONS_DEFAULT
        localCompositeData.setMaxSessions(maxSessions == null ? WebDeploymentDescriptor.MAX_SESSIONS_DEFAULT : maxSessions.intValue());

        localCompositeData.setCookies(loadCookies(localDesc2));

        //Distributable and failover-timeout are not editable but we will load just the information if they are present in the local or not:
        localCompositeData.setHasDistributableValueAssigned(localDesc != null && localDesc.getDistributable() ? true : false);
        localCompositeData.setHasFailoverTimeoutValueAssigned((localDesc2 != null && localDesc2.getFailOverAlert() != null) ? true : false);
        
        localCompositeData.setHasSessionTimeoutValueAssigned(sessionTimeout == null ? false : true);
        localCompositeData.setHasURLSessionTrackingValueAssigned(urlSessionTracking == null ? false : true);
        localCompositeData.setHasMaxSessionsValueAssigned(maxSessions == null ? false : true);
      } //endif (!thisIsModelForTheGlobalDD)

      
      synchronized (provider) {
        globalCompositeData = provider.getGlobalCompositeData();
        if (globalCompositeData == null) {
          initGlobalCompositeData(globalDesc, globalDesc2);
          provider.setGlobalCompositeData(globalCompositeData);
        }
      }

    } catch (Exception e) {
    	//TODO:Polly type:ok
    	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000231", 
    	  "Error while creating JMX WebModule [{0}] for deployed application [{1}].", new Object[]{alias, appName}, e, null, null);
    } finally {
      if (appConfig != null) {
        try {
          appConfig.close();
        } catch (ConfigurationException e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000268", 
            "Cannot close configuration (open in read-only mode) for application [{0}].", new Object[]{appName}, e, null, null);
        }
      }
    }
  }

  private void initGlobalCompositeData(WebDeploymentDescriptor globalDesc, WebJ2EeEngineType globalDesc2) 
                  throws OpenDataException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    //  Load global settings
    String webModuleDislayName = loadJ2EEDisplayName(globalDesc.getDisplayNames());
    boolean webModuleDistributable = globalDesc.getDistributable();
    String failoverMessage = null;
    if (globalDesc2 != null && globalDesc2.getFailOverAlert() != null) {
      failoverMessage = globalDesc2.getFailOverAlert().getMessage();
    } else {
      failoverMessage = WebDeploymentDescriptor.FAILOVER_MESSAGE_DEFAULT;
    }
    int failoverTimeout;
    if (globalDesc2 != null && globalDesc2.getFailOverAlert() != null) {
      failoverTimeout = globalDesc2.getFailOverAlert().getTimeout().intValue();
    } else {
      failoverTimeout = WebDeploymentDescriptor.FAILOVER_TIMEOUT_DEFAULT;
    }
    
    Integer sessionTimeout;
    Boolean urlSessionTracking;
    Integer maxSessions;
    globalCompositeData = new SAP_ITSAMJ2eeWebModuleSettings(
        webModuleDislayName,
        webModuleDistributable,
        failoverMessage,
        failoverTimeout,
        loadJ2EEEJBRemoteReferences(globalDesc),
        loadJ2EEEnvironmentEntries(globalDesc),
        loadJ2EEMessageDestinations(globalDesc),
        loadJ2EEMessageDestinationReferences(globalDesc),
        loadJ2EEResourceEnvironmentReferences(globalDesc),
        loadJ2EEResourceReferences(globalDesc),
        loadJ2EEServerComponentReferences(globalDesc2),
        loadWelcomeFiles(globalDesc),
        loadParams(globalDesc.getContextParams()),
        loadJ2EEEJBLocalReferences(globalDesc),
        loadFilters(globalDesc),
        loadFilterMappings(globalDesc),
        loadListeners(globalDesc),
        loadServletsData(globalDesc),
        loadServletMappings(globalDesc),
        loadMimeMappings(globalDesc),
        loadErrorPages(globalDesc),
        loadTaglibs(globalDesc),
        loadJspPropertyGroups(globalDesc),
        loadLocaleEncodingMappings(globalDesc),
        loadResponseStatuses(globalDesc2),
        null,
        globalDesc == null ? null : loadJ2EEDescription(globalDesc.getDescriptions()),
        null);
    sessionTimeout = null;
    if (globalDesc.getSessionConfig() != null && globalDesc.getSessionConfig().getSessionTimeout() != null) {
      // Get sessionTimeout from global
      sessionTimeout = new Integer(globalDesc.getSessionConfig().getSessionTimeout().get_value().intValue());
    }
    globalCompositeData.setSessionTimeout(sessionTimeout == null ? WebApplicationConfig.DEFAULT_SESSTION_TIMEOUT : sessionTimeout.intValue());

    urlSessionTracking = null;
    // Try load from global
    if (globalDesc2 != null) {
      urlSessionTracking = globalDesc2.getUrlSessionTracking();
    }
    globalCompositeData.setURLSessionTracking(urlSessionTracking == null ? WebDeploymentDescriptor.URL_SESSION_TRACKING_DEFAULT : urlSessionTracking.booleanValue());

    maxSessions = null;
    if (globalDesc2 != null) {
      maxSessions = globalDesc2.getMaxSessions();
    }
    globalCompositeData.setMaxSessions(maxSessions == null ? WebDeploymentDescriptor.MAX_SESSIONS_DEFAULT : maxSessions.intValue());

    globalCompositeData.setCookies(loadCookies(globalDesc2));

    globalCompositeData.setHasDistributableValueAssigned(globalDesc != null && globalDesc.getDistributable() ? true : false);
    globalCompositeData.setHasFailoverTimeoutValueAssigned((globalDesc2 != null && globalDesc2.getFailOverAlert() != null) ? true : false);

    globalCompositeData.setHasSessionTimeoutValueAssigned(sessionTimeout == null ? false : true);
    globalCompositeData.setHasURLSessionTrackingValueAssigned(urlSessionTracking == null ? false : true);
    globalCompositeData.setHasMaxSessionsValueAssigned(maxSessions == null ? false : true);
  }
  
  private ObjectName[] loadServlets(WebDeploymentDescriptor localDesc, WebDeploymentDescriptor globalDesc) {
    ServletType[] localServletTypes = localDesc == null ? null : localDesc.getServlets();
    ServletType[] globalServletTypes = globalDesc.getServlets();

    List<ObjectName> res = new LinkedList<ObjectName>(); // element: Servlet

    for (int i = 0; localServletTypes != null && i < localServletTypes.length; i++) {
      String servletName = localServletTypes[i].getServletName().get_value();
      
      try {
				ObjectName current = provider.constructResourceObjectName(WebContainerLazyMBeanProvider.RES_TYPE_SERVLET, appName, alias, servletName);
				res.add(current);
			} catch (MalformedObjectNameException e) {
				if (traceLocation.beDebug()) {
		    	traceDebug("Failed to get the servlet object name for local servlet [" + servletName + "] in web module [" + alias + "] of application [" + appName + "].", e);
				}
			}
      //TODO:???
    }
    for (int i = 0; globalServletTypes != null && i < globalServletTypes.length; i++) {
      String servletName = globalServletTypes[i].getServletName().get_value();
      // Check if name already used by local servlet
      boolean nameAlreadyRegistered = false;
      for (int j = 0; !nameAlreadyRegistered && j < res.size(); j++) {
      	ObjectName registeredServlet = (ObjectName) res.get(j);
        nameAlreadyRegistered = servletName.equals(registeredServlet);
      }
      if (!nameAlreadyRegistered) {
      	try {
					ObjectName current = provider.constructResourceObjectName(WebContainerLazyMBeanProvider.RES_TYPE_SERVLET, appName, alias, servletName);
					res.add(current);
				} catch (MalformedObjectNameException e) {
					if (traceLocation.beDebug()) {
			    	traceDebug("Failed to get the servlet object name for global servlet [" + servletName + "] in web module [" + alias + "] of application [" + appName + "].", e);
					}
				}
      }
    }
    return (ObjectName[]) res.toArray(new ObjectName[res.size()]);
  }

  private SAP_ITSAMJ2eeServletSettings[] loadServletsData(WebDeploymentDescriptor desc) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    ServletType[] servletTypes = desc == null ? null : desc.getServlets();

    List<SAP_ITSAMJ2eeServletSettings> res = new LinkedList<SAP_ITSAMJ2eeServletSettings>(); // element: Servlet

    for (int i = 0; servletTypes != null && i < servletTypes.length; i++) {
      String servletName = servletTypes[i].getServletName().get_value();

      List<SAP_ITSAMJ2eeProperty> p = new LinkedList<SAP_ITSAMJ2eeProperty>(); // element:  params
      ParamValueType[] params = servletTypes[i].getInitParam();
      for (int j = 0; params != null && j < params.length; j++) {
        SAP_ITSAMJ2eeProperty prop = new SAP_ITSAMJ2eeProperty(
          params[j].getParamName().get_value(),
          params[j].getParamValue().get_value(),
          null,
          null,
          null
        );
        p.add(prop);
      }
      SAP_ITSAMJ2eeProperty[] props = (SAP_ITSAMJ2eeProperty[]) p.toArray(new SAP_ITSAMJ2eeProperty[p.size()]);
      SAP_ITSAMJ2eeServletSettings s = new SAP_ITSAMJ2eeServletSettings(
        loadJ2EEDisplayName(servletTypes[i].getDisplayName()),
        servletTypes[i].getChoiceGroup1().getServletClass() == null ? null : servletTypes[i].getChoiceGroup1().getServletClass().get_value(),
        servletTypes[i].getChoiceGroup1().getJspFile() == null ? null : servletTypes[i].getChoiceGroup1().getJspFile().get_value(),
        servletTypes[i].getLoadOnStartup() == null ? 0 : new Integer(servletTypes[i].getLoadOnStartup().toString()).intValue(),
        props,
        null,
        loadJ2EEDescription(servletTypes[i].getDescription()),
        servletName
      );
      res.add(s);
    }

    return (SAP_ITSAMJ2eeServletSettings[]) res.toArray(new SAP_ITSAMJ2eeServletSettings[res.size()]);
  }
  
  /**
   * @deprecated
   * @param localDesc
   */
  private void loadJsr77ResourcesNames(WebDeploymentDescriptor localDesc) {
    ResourceRefType[] resourceRefTypes = localDesc == null ? null : localDesc.getResReferences();
    
    Vector javaMails = new Vector();
    Vector urls = new Vector();

    for (int i = 0; resourceRefTypes != null && i < resourceRefTypes.length; i++) {
      ResourceRefType res = resourceRefTypes[i];
      String resType = res.getResType().get_value();

      if ("javax.mail.Session".equals(resType)) {
      	String name = res.getResRefName().get_value();
      	try {
					ObjectName current = provider.constructResourceObjectName(WebContainerLazyMBeanProvider.RES_TYPE_JAVAMAIL, appName, alias, name);
					javaMails.add(current);
				} catch (MalformedObjectNameException e) {
					if (traceLocation.beDebug()) {
			    	traceDebug("Failed to get the object name for JavaMailResource [" + name + "] in web module [" + alias + "] of application [" + appName + "].", e);
					}
				}
      } else if ("java.net.URL".equals(resType)) {
      	String name = res.getResRefName().get_value();
      	try {
					ObjectName current = provider.constructResourceObjectName(WebContainerLazyMBeanProvider.RES_TYPE_JAVAMAIL, appName, alias, name);
					urls.add(current);
				} catch (MalformedObjectNameException e) {
					if (traceLocation.beDebug()) {
			    	traceDebug("Failed to get the object name for URLResource [" + name + "] in web module [" + alias + "] of application [" + appName + "].", e);
					}
				}
      }
    }
    
    urlResources = (ObjectName[]) urls.toArray(new ObjectName[urls.size()]);
    javaMailSessionResources = (ObjectName[]) javaMails.toArray(new ObjectName[javaMails.size()]);
  }

  private SAP_ITSAMJ2eeProperty[] loadParams(ParamValueType[] params) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeProperty

    for (int i = 0; params != null && i < params.length; i++) {
      ParamValueType localParam = params[i];

      String description = null;
      DescriptionType[] descriptions = localParam.getDescription();
      if (descriptions != null && descriptions.length > 0) {
        description = descriptions[0].get_value(); // TODO fix i18n
      }

      SAP_ITSAMJ2eeProperty param = new SAP_ITSAMJ2eeProperty(
        localParam.getParamName().get_value(),
        localParam.getParamValue().get_value(),
        null,
        description,
        null
      );
      res.add(param);
    }

    return (SAP_ITSAMJ2eeProperty[]) res.toArray(new SAP_ITSAMJ2eeProperty[res.size()]);
  }


  private SAP_ITSAMJ2eeWebFilterSettings[] loadFilters(WebDeploymentDescriptor desc) throws OpenDataException {
    FilterType[] filters = desc == null ? null : desc.getFilters();

    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebFilterSettings

    for (int i = 0; filters != null && i < filters.length; i++) {
      FilterType filter = filters[i];
      String filterName = filter.getFilterName().get_value();

      SAP_ITSAMJ2eeWebFilterSettings f = new SAP_ITSAMJ2eeWebFilterSettings(
        loadJ2EEDisplayName(filter.getDisplayName()),
        filter.getFilterClass().get_value(),
        loadParams(filter.getInitParam()),
        null,
        loadJ2EEDescription(filter.getDescription()),
        filterName
      );
      res.add(f);
    }

    return (SAP_ITSAMJ2eeWebFilterSettings[]) res.toArray(new SAP_ITSAMJ2eeWebFilterSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebFilterMappingSettings[] loadFilterMappings(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebFilterMappingSettings

    FilterMappingType[] mappings = desc == null ? null : desc.getFilterMappings();
    for (int i = 0; mappings != null && i < mappings.length; i++) {
      FilterMappingType mapping = mappings[i];

      FilterMappingType.Choice1[] choice1 = mapping.getChoiceGroup1();
      String servletName = null;
      String urlPattern = null;
      if (choice1 != null && choice1.length > 0 && choice1[0] != null) {
        servletName = (choice1[0].isSetServletName()) ? choice1[0].getServletName().get_value() : null;
        urlPattern = (choice1[0].isSetUrlPattern()) ? choice1[0].getUrlPattern().get_value() : null;
      }

      DispatcherType[] dispatchers = mapping.getDispatcher();
      boolean dispatcherForward = false;
      boolean dispatcherInclude = false;
      boolean dispatcherRequest = false;
      boolean dispatcherError = false;

      for (int j = 0; dispatchers != null && j < dispatchers.length; j++) {
        String dispatcherValue = dispatchers[j].get_value();

        dispatcherForward |= "FORWARD".equalsIgnoreCase(dispatcherValue);
        dispatcherInclude |= "INCLUDE".equalsIgnoreCase(dispatcherValue);
        dispatcherRequest |= "REQUEST".equalsIgnoreCase(dispatcherValue);
        dispatcherError |= "ERROR".equalsIgnoreCase(dispatcherValue);
      }

      SAP_ITSAMJ2eeWebFilterMappingSettings m = new SAP_ITSAMJ2eeWebFilterMappingSettings(
        servletName,
        urlPattern,
        mapping.getFilterName().get_value(),
        dispatcherForward,
        dispatcherInclude,
        dispatcherRequest,
        dispatcherError,
        null,
        null,
        null
      );
      res.add(m);
    }

    return (SAP_ITSAMJ2eeWebFilterMappingSettings[]) res.toArray(new SAP_ITSAMJ2eeWebFilterMappingSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebListenerSettings[] loadListeners(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeWebListenerSettings

    // Add all local listeners
    ListenerType[] listeners = desc == null ? null : desc.getListeners();
    for (int i = 0; listeners != null && i < listeners.length; i++) {
      ListenerType listener = listeners[i];
      SAP_ITSAMJ2eeWebListenerSettings l = new SAP_ITSAMJ2eeWebListenerSettings(
        loadJ2EEDisplayName(listener.getDisplayName()),
        listener.getListenerClass().get_value(),
        null,
        loadJ2EEDescription(listener.getDescription()),
        null
      );
      res.add(l);
    }

    return (SAP_ITSAMJ2eeWebListenerSettings[]) res.toArray(new SAP_ITSAMJ2eeWebListenerSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeServletMappingSettings[] loadServletMappings(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeServletMappingSettings

    ServletMappingType[] mappings = desc == null ? null : desc.getServletMapping();
    for (int i = 0; mappings != null && i < mappings.length; i++) {
      ServletMappingType mapping = mappings[i];

      SAP_ITSAMJ2eeServletMappingSettings m = new SAP_ITSAMJ2eeServletMappingSettings(
        mapping.getServletName().get_value(),
        mapping.getUrlPattern()[0].get_value(),
        null,
        null,
        null
      );
      res.add(m);
    }

    return (SAP_ITSAMJ2eeServletMappingSettings[]) res.toArray(new SAP_ITSAMJ2eeServletMappingSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebMIMEMappingSettings[] loadMimeMappings(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeWebMIMEMappingSettings

    MimeMappingType[] mappings = desc == null ? null : desc.getMIMEMapping();
    for (int i = 0; mappings != null && i < mappings.length; i++) {
      MimeMappingType mapping = mappings[i];

      SAP_ITSAMJ2eeWebMIMEMappingSettings m = new SAP_ITSAMJ2eeWebMIMEMappingSettings(
        mapping.getExtension().get_value(),
        mapping.getMimeType().get_value(),
        null,
        null,
        null
      );
      res.add(m);
    }

    return (SAP_ITSAMJ2eeWebMIMEMappingSettings[]) res.toArray(new SAP_ITSAMJ2eeWebMIMEMappingSettings[res.size()]);
  }

  private String[] loadWelcomeFiles(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: String
    WelcomeFileListType wflt = null;
    String[] welcomeFiles = null;

    if (desc != null && (wflt = desc.getWelcomeFileList()) != null && (welcomeFiles = wflt.getWelcomeFile()) != null) {
      return welcomeFiles;
    }
    return null;
  }

  private SAP_ITSAMJ2eeWebErrorPageSettings[] loadErrorPages(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeWebErrorPageSettings

    ErrorPageType[] errorPages = desc == null ? null : desc.getErrorPage();
    for (int i = 0; errorPages != null && i < errorPages.length; i++) {
      ErrorPageType errorPage = errorPages[i];
      ErrorPageType.Choice1 choice1 = errorPage.getChoiceGroup1();

      String errorCode = choice1.isSetErrorCode() ? String.valueOf(choice1.getErrorCode().get_value()) : null;  // TODO fix this: use Integer or BigInt or maybe even a dedicated type?
      String exception = choice1.isSetExceptionType() ? choice1.getExceptionType().get_value() : null;

      SAP_ITSAMJ2eeWebErrorPageSettings e = new SAP_ITSAMJ2eeWebErrorPageSettings(// TODO fix param order as in XSD: (1)error-code, (2)exception-type, (3)location
        errorPage.getLocation().get_value(),
        exception,
        errorCode,
        null,
        null,
        null
      );
      res.add(e);
    }

    return (SAP_ITSAMJ2eeWebErrorPageSettings[]) res.toArray(new SAP_ITSAMJ2eeWebErrorPageSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebTaglibSettings[] loadTaglibs(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebTaglibSettings

    JspConfigType jspConfig = null;
    TaglibType[] taglibs = null;

    if (desc != null && (jspConfig = desc.getJspConfig()) != null && (taglibs = jspConfig.getTaglib()) != null) {
      for (int i = 0; i < taglibs.length; i++) {
        TaglibType taglib = taglibs[i];

        SAP_ITSAMJ2eeWebTaglibSettings t = new SAP_ITSAMJ2eeWebTaglibSettings(
          taglib.getTaglibLocation().get_value(),
          taglib.getTaglibUri().get_value(),
          null,
          null,
          null
        );
        res.add(t);
      }
    }

    return (SAP_ITSAMJ2eeWebTaglibSettings[]) res.toArray(new SAP_ITSAMJ2eeWebTaglibSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebJSPPropertyGroupSettings[] loadJspPropertyGroups(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: WebJSPPropertyGroupCDImpl

    JspConfigType jspConfig = null;
    JspPropertyGroupType[] jspPropertyGroups = null;

    if (desc != null && (jspConfig = desc.getJspConfig()) != null && (jspPropertyGroups = jspConfig.getJspPropertyGroup()) != null) {
      for (int i = 0; i < jspPropertyGroups.length; i++) {
        JspPropertyGroupType jspPropertyGroup = jspPropertyGroups[i];

        if (jspPropertyGroup != null) {

          UrlPatternType[] urlPatterns = jspPropertyGroup.getUrlPattern();
          String[] urlPatternsRes;
          if (urlPatterns == null) {
            urlPatternsRes = new String[0];
          } else {
            urlPatternsRes = new String[urlPatterns.length];
            for (int j = 0; j < urlPatterns.length; j++) {
              if (urlPatterns[j] != null) {
                urlPatternsRes[j] = new String(urlPatterns[j].get_value());
              }
            }
          }

          PathType[] includePreludes = jspPropertyGroup.getIncludePrelude();
          String[] includePreludesRes;
          if (includePreludes == null) {
            includePreludesRes = new String[0];
          } else {
            includePreludesRes = new String[includePreludes.length];
            for (int j = 0; j < includePreludes.length; j++) {
              if (includePreludes[j] != null) {
                includePreludesRes[j] = new String(includePreludes[j].get_value());
              }
            }
          }

          PathType[] includeCodas = jspPropertyGroup.getIncludeCoda();
          String[] includeCodasRes;
          if (includeCodas == null) {
            includeCodasRes = new String[0];
          } else {
            includeCodasRes = new String[includeCodas.length];
            for (int j = 0; j < includeCodas.length; j++) {
              if (includeCodas[j] != null) {
                includeCodasRes[j] = new String(includeCodas[j].get_value());
              }
            }
          }

          SAP_ITSAMJ2eeWebJSPPropertyGroupSettings localJspPG = new SAP_ITSAMJ2eeWebJSPPropertyGroupSettings(
            loadJ2EEDisplayName(jspPropertyGroup.getDisplayName()),
            jspPropertyGroup.getPageEncoding() == null ? null : jspPropertyGroup.getPageEncoding().get_value(),
            jspPropertyGroup.getIsXml() != null && jspPropertyGroup.getIsXml().is_value(),
            jspPropertyGroup.getElIgnored() != null && jspPropertyGroup.getElIgnored().is_value(),
            jspPropertyGroup.getScriptingInvalid() != null && jspPropertyGroup.getScriptingInvalid().is_value(),
            urlPatternsRes,
            includeCodasRes,
            includePreludesRes,
            null,
            loadJ2EEDescription(jspPropertyGroup.getDescription()),
            null);
          res.add(localJspPG);
        }
      }
    }


    return (SAP_ITSAMJ2eeWebJSPPropertyGroupSettings[]) res.toArray(new SAP_ITSAMJ2eeWebJSPPropertyGroupSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeEnvironmentEntrySettings[] loadJ2EEEnvironmentEntries(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeEnvironmentEntrySettings

    // Add all local env_entries
    EnvEntryType[] envEntries = desc == null ? null : desc.getEnvEntries();
    for (int i = 0; envEntries != null && i < envEntries.length; i++) {
      EnvEntryType envEntry = envEntries[i];

      XsdStringType value = envEntry.getEnvEntryValue();
      DescriptionType[] descriptions = envEntry.getDescription();

      SAP_ITSAMJ2eeEnvironmentEntrySettings e = new SAP_ITSAMJ2eeEnvironmentEntrySettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        envEntry.getEnvEntryName().get_value(),
        null,
        envEntry.getEnvEntryType().get_value(),
        value == null ? null : value.get_value(),
        null,
        null
      );
      res.add(e);
    }

    return (SAP_ITSAMJ2eeEnvironmentEntrySettings[]) res.toArray(new SAP_ITSAMJ2eeEnvironmentEntrySettings[res.size()]);
  }

  private SAP_ITSAMJ2eeEJBRemoteReferenceSettings[] loadJ2EEEJBRemoteReferences(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeEJBRemoteReferenceSettings

    EjbRefType[] ejbRemoteRefs = desc == null ? null : desc.getEjbRefs();
    for (int i = 0; ejbRemoteRefs != null && i < ejbRemoteRefs.length; i++) {
      EjbRefType ejbRemoteRef = ejbRemoteRefs[i];

      DescriptionType[] descriptions = ejbRemoteRef.getDescription();
      com.sap.engine.lib.descriptors.webj2eeengine.EjbRefType ejbRemoteRef2 = desc.getEjbRefFromAdditional(ejbRemoteRef);
      String jndiName = ejbRemoteRef2 == null ? null : ejbRemoteRef2.getJndiName();

      SAP_ITSAMJ2eeEJBRemoteReferenceSettings e = new SAP_ITSAMJ2eeEJBRemoteReferenceSettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        null, // TODO fix -- remove this property
        jndiName,
        ejbRemoteRef.getEjbRefType() != null ? ejbRemoteRef.getEjbRefType().get_value() : null,
        ejbRemoteRef.getEjbRefName() != null ? ejbRemoteRef.getEjbRefName().get_value() : null,
        ejbRemoteRef.getRemote() != null ? ejbRemoteRef.getRemote().get_value() : null,
        ejbRemoteRef.getHome() != null ? ejbRemoteRef.getHome().get_value() : null,
        ejbRemoteRef.getEjbLink() != null ? ejbRemoteRef.getEjbLink().get_value() : null,
        null,
        null
      );
      res.add(e);
    }

    return (SAP_ITSAMJ2eeEJBRemoteReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeEJBRemoteReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeEJBLocalReferenceSettings[] loadJ2EEEJBLocalReferences(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeEJBLocalReferenceSettings

    EjbLocalRefType[] ejbLocalRefs = desc == null ? null : desc.getEjbLocalRefs();
    for (int i = 0; ejbLocalRefs != null && i < ejbLocalRefs.length; i++) {
      EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

      DescriptionType[] descriptions = ejbLocalRef.getDescription();
      com.sap.engine.lib.descriptors.webj2eeengine.EjbLocalRefType ejbLocalRef2 = desc.getEjbLocalRefFromAdditional(ejbLocalRef);
      String jndiName = ejbLocalRef2 == null ? null : ejbLocalRef2.getJndiName();

      SAP_ITSAMJ2eeEJBLocalReferenceSettings e = new SAP_ITSAMJ2eeEJBLocalReferenceSettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        ejbLocalRef.getEjbRefName() != null ? ejbLocalRef.getEjbRefName().get_value() : null,
        jndiName,
        ejbLocalRef.getEjbRefType() != null ? ejbLocalRef.getEjbRefType().get_value() : null,
        null, // TODO fix -- remove this property
        ejbLocalRef.getLocal() != null ? ejbLocalRef.getLocal().get_value() : null,
        ejbLocalRef.getLocalHome() != null ? ejbLocalRef.getLocalHome().get_value() : null,
        ejbLocalRef.getEjbLink() != null ? ejbLocalRef.getEjbLink().get_value() : null,
        null,
        null
      );
      res.add(e);
    }

    return (SAP_ITSAMJ2eeEJBLocalReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeEJBLocalReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeResourceReferenceSettings[] loadJ2EEResourceReferences(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector();  // elements: SAP_ITSAMJ2eeResourceReferenceSettings

    // Add all local resRefs
    ResourceRefType[] resRefs = desc == null ? null : desc.getResReferences();
    for (int i = 0; resRefs != null && i < resRefs.length; i++) {
      ResourceRefType resRef = resRefs[i];

      DescriptionType[] descriptions = resRef.getDescription();
      com.sap.engine.lib.descriptors.webj2eeengine.ResourceRefType resRef2 = desc.getResourceReferenceFromAdditional(resRef);
      String jndiName = resRef2 == null ? null : resRef2.getResLink();
      boolean transactional = resRef2 == null ? true : resRef2.getNonTransactional() == null;

      SAP_ITSAMJ2eeResourceReferenceSettings r = new SAP_ITSAMJ2eeResourceReferenceSettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        resRef.getResRefName() != null ? resRef.getResRefName().get_value() : null,
        jndiName,
        resRef.getResType() != null ? resRef.getResType().get_value() : null,
        resRef.getResAuth() != null ? resRef.getResAuth().get_value() : null,
        resRef.getResSharingScope() != null ? resRef.getResSharingScope().get_value() : null,
        transactional,
        null,
        null
      );
      res.add(r);
    }

    return (SAP_ITSAMJ2eeResourceReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeResourceReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[] loadJ2EEResourceEnvironmentReferences(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings

    // Add all local resEnvRefs
    ResourceEnvRefType[] resEnvRefs = desc == null ? null : desc.getResEnvReferences();
    for (int i = 0; resEnvRefs != null && i < resEnvRefs.length; i++) {
      ResourceEnvRefType resRef = resEnvRefs[i];

      DescriptionType[] descriptions = resRef.getDescription();
      com.sap.engine.lib.descriptors.webj2eeengine.ResourceEnvRefType resEnvRef2 = desc.getResEnvRefFromAdditional(resRef);
      String jndiName = resEnvRef2 == null ? null : resEnvRef2.getJndiName();

      SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings r = new SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        resRef.getResourceEnvRefName() != null ? resRef.getResourceEnvRefName().get_value() : null,
        jndiName,
        resRef.getResourceEnvRefType() != null ? resRef.getResourceEnvRefType().get_value() : null,
        null,
        null
      );
      res.add(r);
    }

    return (SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeMessageDestinationReferenceSettings[] loadJ2EEMessageDestinationReferences(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeMessageDestinationReferenceSettings

    MessageDestinationRefType[] msgDestRefs = desc == null ? null : desc.getMsgDestinationRefs();
    for (int i = 0; msgDestRefs != null && i < msgDestRefs.length; i++) {
      MessageDestinationRefType msgDestRef = msgDestRefs[i];

      DescriptionType[] descriptions = msgDestRef.getDescription();

      SAP_ITSAMJ2eeMessageDestinationReferenceSettings m = new SAP_ITSAMJ2eeMessageDestinationReferenceSettings(
        descriptions != null && descriptions.length > 0 ? descriptions[0].get_value() : null, // TODO i18n
        msgDestRef.getMessageDestinationRefName() != null ? msgDestRef.getMessageDestinationRefName().get_value() : null,
        null,
        msgDestRef.getMessageDestinationType() != null ? msgDestRef.getMessageDestinationType().get_value() : null,
        msgDestRef.getMessageDestinationUsage() != null ? msgDestRef.getMessageDestinationUsage().get_value() : null,
        msgDestRef.getMessageDestinationLink() != null ? msgDestRef.getMessageDestinationLink().get_value() : null,
        null,
        null
      );
      res.add(m);
    }

    return (SAP_ITSAMJ2eeMessageDestinationReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeMessageDestinationReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeMessageDestinationSettings[] loadJ2EEMessageDestinations(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeMessageDestinationSettings

    MessageDestinationType[] msgDestinations = desc == null ? null : desc.getMsgDestinations();
    for (int i = 0; msgDestinations != null && i < msgDestinations.length; i++) {
      MessageDestinationType msgDestination = msgDestinations[i];

      SAP_ITSAMJ2eeMessageDestinationSettings m = new SAP_ITSAMJ2eeMessageDestinationSettings(
        msgDestination.getMessageDestinationName().get_value(),
        null, // TODO fix -- jndiName property is not implemented
        null,
        loadJ2EEDescription(msgDestination.getDescription()),
        loadJ2EEDisplayName(msgDestination.getDisplayName())
      );
      res.add(m);
    }

    return (SAP_ITSAMJ2eeMessageDestinationSettings[]) res.toArray(new SAP_ITSAMJ2eeMessageDestinationSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[] loadJ2EEServerComponentReferences(WebJ2EeEngineType desc2) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings

    ServerComponentRefType[] serverComponentRefs = null;

    // Add all local server_component_refs
    if (desc2 != null && (serverComponentRefs = desc2.getServerComponentRef()) != null) {
      for (int i = 0; i < serverComponentRefs.length; i++) {
        ServerComponentRefType serverComponentRef = serverComponentRefs[i];

        SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings s = new SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings(
          serverComponentRef.getDescription(),
          serverComponentRef.getName(),
          serverComponentRef.getJndiName(),
          serverComponentRef.getType() != null ? serverComponentRef.getType().getValue() : null,
          null,
          null
        );
        res.add(s);
      }
    }
    return (SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[]) res.toArray(new SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebResponseStatusSettings[] loadResponseStatuses(WebJ2EeEngineType desc2) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebResponseStatusSettings

    ResponseStatusType[] respStatuses = null;

    if (desc2 != null && (respStatuses = desc2.getResponseStatus()) != null) {
      for (int i = 0; i < respStatuses.length; i++) {
        ResponseStatusType respStatus = respStatuses[i];
        SAP_ITSAMJ2eeWebResponseStatusSettings status = new SAP_ITSAMJ2eeWebResponseStatusSettings(
          respStatus.getStatusCode(),
          respStatus.getReasonPhrase(),
          null,
          null,
          null
        );
        res.add(status);
      }
    }

    return (SAP_ITSAMJ2eeWebResponseStatusSettings[]) res.toArray(new SAP_ITSAMJ2eeWebResponseStatusSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings[] loadLocaleEncodingMappings(WebDeploymentDescriptor desc) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings

    LocaleEncodingMappingListType list = null;
    LocaleEncodingMappingType[] mappings = null;

    if (desc != null && (list = desc.getLocaleEncodingMappings()) != null && (mappings = list.getLocaleEncodingMapping()) != null) {
      for (int i = 0; i < mappings.length; i++) {
        LocaleEncodingMappingType localeMapping = mappings[i];
        SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings localMapping = new SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings(
          localeMapping.getLocale(),
          localeMapping.getEncoding(),
          null,
          null,
          null
        );
        res.add(localMapping);
      }
    }

    return (SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings[]) res.toArray(new SAP_ITSAMJ2eeWebLocaleEncodingMappingSettings[res.size()]);
  }

  private SAP_ITSAMJ2eeWebCookieSettings[] loadCookies(WebJ2EeEngineType desc2) throws OpenDataException {
    Vector res = new Vector(); // elements: SAP_ITSAMJ2eeWebCookieSettings

    CookieConfigType cct = null;
    CookieType[] cookies = null;

    if (desc2 != null && (cct = desc2.getCookieConfig()) != null && (cookies = cct.getCookie()) != null) {
      // There are local cookies, read them
      for (int i = 0; i < cookies.length; i++) {
        CookieType cookie = cookies[i];

        Integer maxAge = cookie.getMaxAge(); // Could be null

        SAP_ITSAMJ2eeWebCookieSettings localCookie = new SAP_ITSAMJ2eeWebCookieSettings(
          cookie.getType().getValue(),
          cookie.getPath(),
          cookie.getDomain(),
          maxAge == null ? -1 : maxAge.intValue(),
          null,
          null,
          null
        );
        res.add(localCookie);
      }
    }

    return (SAP_ITSAMJ2eeWebCookieSettings[]) res.toArray(new SAP_ITSAMJ2eeWebCookieSettings[res.size()]);
  }

  private void loadDeploymentDescriptor() {
    if (thisIsModelForTheGlobalDD) {
      deploymentDescriptor = "n/a";  // TODO Fix this some day. For now just leave it non-null for the CTS.
      return;
    }
    
    ServiceContext serviceContext = ServiceContext.getServiceContext();
    DeployContext deployContext = serviceContext.getDeployContext();
    DeployCommunicator dc = deployContext.getDeployCommunicator();
    Configuration appConfig = null;
    Configuration servlet_jspConfig = null;
    
    try {
    	appConfig = dc.getAppConfigurationForReadAccess(appName);
      servlet_jspConfig = appConfig.getSubConfiguration(Constants.CONTAINER_NAME);
    	WebDeploymentDescriptor dd = ActionBase.loadWebDDObjectFromDBase(servlet_jspConfig, webAliasDir);
    	ByteArrayOutputStream webOS = new ByteArrayOutputStream();
    	dd.writeStandartDescriptorToStream(webOS);
    	deploymentDescriptor = webOS.toString();// TODO implement encoding-sensitive conversion byte[] --> char[]
    } catch (Exception e) {
    	//TODO:Polly type:ok
    	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000232", 
    	  "Cannot load deployment descriptor for webAliasDir [{0}] and application [{1}].", new Object[]{webAliasDir, appName}, e, null, null);
    } finally {
    	if (appConfig != null) {
        try {
          appConfig.close();
        } catch (ConfigurationException e) {
     	  LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000269", 
     	    "Cannot close configuration (open in read-only mode) for application [{0}].", new Object[]{appName}, e, null, null);

        }
      }
    }
  }

  private WebDeploymentDescriptor getDescriptorFromCD(SAP_ITSAMJ2eeWebModuleSettings compositeData) {
    WebDeploymentDescriptor descriptor = new WebDeploymentDescriptor(false);

    SAP_ITSAMJ2eeWebCookieSettings[] cookies = compositeData.getCookies();

    saveSessionTimeout(descriptor, compositeData);

    saveWebJ2EEngine(cookies, /* j2eeLocalRefs */null, /* j2eeRemoteRefs */null, 
        /* messageDestinations */null, /* envResourceRefs */null, /* responseStatuses */null, /* resourceRefs */null, 
        /* serverComponentReferences */null, descriptor, compositeData);

    return descriptor;
  }

  /**
   * 
   * @param cookies
   * @param j2eeLocalRefs
   * @param j2eeRemoteRefs
   * @param messageDestinations
   * @param envResourceRefs
   * @param responseStatuses
   * @param resourceRefs
   * @param serverComponentReferences
   * @param descriptor
   * @param compositeData
   */
  private void saveWebJ2EEngine(SAP_ITSAMJ2eeWebCookieSettings[] cookies, SAP_ITSAMJ2eeEJBLocalReferenceSettings[] j2eeLocalRefs, 
                                SAP_ITSAMJ2eeEJBRemoteReferenceSettings[] j2eeRemoteRefs,
                                SAP_ITSAMJ2eeMessageDestinationSettings[] messageDestinations,
                                SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[] envResourceRefs, 
                                SAP_ITSAMJ2eeWebResponseStatusSettings[] responseStatuses,
                                SAP_ITSAMJ2eeResourceReferenceSettings[] resourceRefs, 
                                SAP_ITSAMJ2eeJNDIEnvironmentReferenceSettings[] serverComponentReferences,
                                WebDeploymentDescriptor descriptor, SAP_ITSAMJ2eeWebModuleSettings compositeData) {
    WebJ2EeEngineType webJ2EeEngineType = new WebJ2EeEngineType();

    // -------------------------- Vily's changes ----------------------------
    Vector cookieTypes = new Vector();
    if (cookies != null && cookies.length > 0) {
      for (int i = 0; i < cookies.length; i++) {
        SAP_ITSAMJ2eeWebCookieSettings tmp = cookies[i];
        /* Before CIM was: tmp = new WebCookieCDImpl(cookies[i]); */
  
        if (null != tmp) {
          CookieType cookieType = new CookieType();
          cookieType.setDomain(cookies[i].getDomain());
          cookieType.setMaxAge(new Integer(cookies[i].getMaxAge()));
          cookieType.setPath(cookies[i].getPath());
          cookieType.setType(new CookieTypeType(cookies[i].getType()));
          cookieTypes.add(cookieType);
        }
      }
      CookieConfigType cookieConfigType = new CookieConfigType();
      cookieConfigType.setCookie((CookieType[]) cookieTypes.toArray(new CookieType[0]));

      webJ2EeEngineType.setCookieConfig(cookieConfigType);
    } else {
      webJ2EeEngineType.setCookieConfig(null); //here removes the cookies if cookies is null or an empty array
    }
    
    // -------------------------- End of Vily's changes ----------------------------

    Integer maxSessionsDD;
    if (compositeData.getHasMaxSessionsValueAssigned()) {
      maxSessionsDD = new Integer(compositeData.getMaxSessions());
    } else {
      maxSessionsDD = new Integer(WebDeploymentDescriptor.MAX_SESSIONS_DEFAULT);
    }
    webJ2EeEngineType.setMaxSessions(maxSessionsDD);


    Boolean urlSessionTrackingDD;
    if (compositeData.getHasURLSessionTrackingValueAssigned()) {
      urlSessionTrackingDD = new Boolean(compositeData.getURLSessionTracking());
    } else {
      urlSessionTrackingDD = new Boolean(WebDeploymentDescriptor.URL_SESSION_TRACKING_DEFAULT);
    }
    webJ2EeEngineType.setUrlSessionTracking(urlSessionTrackingDD);

    descriptor.setWebJ2EEEngine(webJ2EeEngineType);
  }

  /**
   * 
   * @param descriptor
   * @param compositeData
   */
  private void saveSessionTimeout(WebDeploymentDescriptor descriptor, SAP_ITSAMJ2eeWebModuleSettings compositeData) {
    SessionConfigType sessionConfigType = new SessionConfigType();
    XsdIntegerType xsdIntegerType = new XsdIntegerType();
    String sessionTimeoutDD;
    if (compositeData.getHasSessionTimeoutValueAssigned()) {
      sessionTimeoutDD = Integer.toString(compositeData.getSessionTimeout());
      xsdIntegerType.set_value(new BigInteger(sessionTimeoutDD));
      sessionConfigType.setSessionTimeout(xsdIntegerType);
    } else {
      sessionConfigType.setSessionTimeout(null);
    }    
    descriptor.setSessionConfig(sessionConfigType);
  }

  /**
   * @param failoverTimeout
   * @param descriptor
   */
  private void saveFailoverTimeout(int failoverTimeout, WebDeploymentDescriptor descriptor) {
    // TODO Auto-generated method stub
  }

  /**
   * @param failoverMsg
   * @param descriptor
   */
  private void saveFailoverMsg(String failoverMsg, WebDeploymentDescriptor descriptor) {
    // TODO Auto-generated method stub
  }

  /**
   * @param isDestributable
   * @param descriptor
   */
  private void saveDistributable(boolean isDestributable, WebDeploymentDescriptor descriptor) {
    // TODO Auto-generated method stub
  }

  /**
   * Updates the current mbean, because settings are separate instance.
   * Synchronization is done outside when called together with update in configuration and on FS.
   * <p/>
   * If HasXXXValueAssigned is true (except for cookies) then updates the mbean,
   * otherwise removes the value by replacing it with default value.
   *
   * @param settings contain the changed properties
   */
  public void setSAP_ITSAMJ2eeWebModuleSettingsForCData(SAP_ITSAMJ2eeWebModuleSettings settings) {
  	if (traceLocation.beDebug()) {
    	traceDebug("Updating model on this server node for application [" + appName + "] in web module [" + alias + 
    			"] with new settings [" + (settings != null ?  "maxSessions=" + settings.getMaxSessions() + 
    					",sessionTimeout=" + settings.getSessionTimeout() + ",URLSessionTracking=" + settings.getURLSessionTracking() +
    					",Cookies=[" + (settings.getCookies() != null ? settings.getCookies().length : "null") + "] " : null) + "].");
		}
    localCompositeData.setHasSessionTimeoutValueAssigned(settings.getHasSessionTimeoutValueAssigned());
    if (localCompositeData.getHasSessionTimeoutValueAssigned()) {
      localCompositeData.setSessionTimeout(settings.getSessionTimeout());
    } else {
      localCompositeData.setSessionTimeout(WebApplicationConfig.DEFAULT_SESSTION_TIMEOUT);
    }
    localCompositeData.setHasURLSessionTrackingValueAssigned(settings.getHasURLSessionTrackingValueAssigned());
    if (localCompositeData.getHasURLSessionTrackingValueAssigned()) {
      localCompositeData.setURLSessionTracking(settings.getURLSessionTracking());
    } else {
      localCompositeData.setURLSessionTracking(WebDeploymentDescriptor.URL_SESSION_TRACKING_DEFAULT);
    }
    localCompositeData.setHasMaxSessionsValueAssigned(settings.getHasMaxSessionsValueAssigned());
    if (localCompositeData.getHasMaxSessionsValueAssigned()) {
      localCompositeData.setMaxSessions(settings.getMaxSessions());
    } else {
      localCompositeData.setMaxSessions(WebDeploymentDescriptor.MAX_SESSIONS_DEFAULT);
    }
    localCompositeData.setCookies(settings.getCookies());
  }
  
  public static void traceDebug(String msg, Throwable t) {
  	traceLocation.debugT(msg + " The exception is: " + LogContext.getExceptionStackTrace(t));
  }
  
  public static void traceDebug(String msg) {
  	traceLocation.debugT(msg);
  }
  
  public static void logWarning(String msgId, String msg) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, msgId, msg, null, null);
	  }

	  public static void logWarning(String msgId, String msg, Throwable t) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, msgId, msg, t, null, null);
	  }

	  public static void logError(String msgId, String msg) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, msgId, msg, null, null);
	  }

	  public static void logError(String msgId, String msg, Throwable t) {
	    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, msgId, msg, t, null, null);
	  }

  
}
