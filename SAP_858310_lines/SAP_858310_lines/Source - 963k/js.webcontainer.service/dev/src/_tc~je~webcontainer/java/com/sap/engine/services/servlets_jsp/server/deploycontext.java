/*
* Copyright (c) 2004-2008 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.state.PersistentContainer;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieConfigType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieType;
import com.sap.engine.lib.descriptors.webj2eeengine.CookieTypeType;
import com.sap.engine.lib.descriptors5.web.MimeMappingType;
import com.sap.engine.lib.processor.SchemaProcessor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.lib.util.ConcurrentReadHashMap;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.converter.TldConverter;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.converter.WebConverter;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class DeployContext {

  private static Location currentLocation;
  private static Location traceLocation = LogContext.getLocationService();

  private DeployCommunicator deployCommunicator = null;
  private PersistentContainer persistentContainer = null;
  private static ConcurrentReadHashMap startedWebApplications = new ConcurrentReadHashMap();
  private WebDeploymentDescriptor globalDD = null;

  private SchemaProcessor webSchemaProcessor;
  private SchemaProcessor webJ2eeSchemaProcessor;
  private SchemaProcessor webJspTldSchemaProcessor;

  private WebConverter webConverter = null;
  private TldConverter tldConverter = null;
  /**
   * This map contains all the mime types defined in the global-web.xml.
   * The table is instantiated during global-web.xml parsing and filled with the mime types found there.
   * No other mime types could/should be added there.
   */
  private HashMap<String, String> globalMimes = null;

  public DeployContext(PersistentContainer persistentContainer) {
    long time = System.currentTimeMillis();
    long newtime = time;

    currentLocation = Location.getLocation(DeployContext.class);

    this.persistentContainer = persistentContainer;
    if (traceLocation.beDebug()) {
      newtime = System.currentTimeMillis();
			traceLocation.debugT("this.persistentContainer = persistentContainer >>> " + (newtime - time));
		}
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    try {
      webSchemaProcessor = SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEB5);
      webJspTldSchemaProcessor = SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEBJSPTLD5);
      webJ2eeSchemaProcessor = SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WEBJ2EE);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
    if (traceLocation.beDebug()) {
			traceLocation.debugT("SchemaProcessors >>> " + (System.currentTimeMillis() - newtime));
			traceLocation.debugT("Whole time >>> " + (System.currentTimeMillis() - time));
		}
  }//end of constructor

  public DeployCommunicator getDeployCommunicator() {
    return deployCommunicator;
  }//end of getDeployCommunicator()

  public InputStream getGlobalWebDescriptor() throws ServiceException {
    return persistentContainer.getPersistentEntryStream(Constants.globalWebXmlPath, true);
  }//end of getGlobalWebDescriptor()

  public InputStream getGlobalAdditionalWebDescriptor() throws ServiceException {
    return persistentContainer.getPersistentEntryStream(Constants.globalAddXmlPath, true);
  }//end of getGlobalAdditionalWebDescriptor()

  /**
   * The returned WebDeploymentDescriptor instance is shared and should not be modified.
   * For internal use only - do not pass the result outside of container or to applications.
   */
  public WebDeploymentDescriptor getGlobalDD() {
    return globalDD;
  }//end of getGlobalDD()

  public void applicationStarted(MessageBytes alias, ApplicationContext applicationContext) {
    startedWebApplications.put(alias, applicationContext);
  }//end of applicationStarted(MessageBytes alias, ApplicationContext applicationContext)

  public void removeStartedApplication(MessageBytes alias) {
    startedWebApplications.remove(alias);
  }//end of removeStartedApplication(MessageBytes alias)

  public Enumeration getStartedWebApplications() {
    return startedWebApplications.elements();
  }//end of getStartedWebApplications()

  public Enumeration getStartedWebApplicationsNames() {
    return startedWebApplications.keys();
  }//end of getStartedWebApplicationsNames()

  public int getStartedApplicationsCount() {
    return startedWebApplications.size();
  }//end of getStartedApplicationsCount()

  public ApplicationContext getStartedWebApplicationContext(MessageBytes alias) {
    return (ApplicationContext) startedWebApplications.get(alias);
  }//end of getStartedWebApplicationContext(MessageBytes alias)

  public boolean isWebApplicationStarted(MessageBytes alias) {
    return startedWebApplications.containsKey(alias);
  }//end of isWebApplicationStarted(MessageBytes alias)

  public String[] getAllMyApplications() throws DeploymentException {
    Vector myAppl = new Vector();

    String[] allAppl = getDeployCommunicator().getDeployedApplications();
    for (int i = 0; i < allAppl.length; i++) {
      if (getDeployCommunicator().getAliases(allAppl[i]) != null && getDeployCommunicator().getAliases(allAppl[i]).length != 0) {
        myAppl.add(allAppl[i]);
      }
    }

    return (String[]) myAppl.toArray(new String[myAppl.size()]);
  }//end of getAllMyApplications()

  /**
   * Returns all web applications names corresponding to the specified application name. In case DeploymentException is
   * thrown during getting aliases from deploy service - <code>null</code> will be returned.
   *
   * @param applicationName
   *          specifies the application name
   * @return String array with all web applications names corresponding to this application name
   */
  public String[] getAliases(String applicationName) {
    try {
      return getDeployCommunicator().getAliases(applicationName);
    } catch (DeploymentException de) {
      if (LogContext.getLocationDeploy().beError()) {
        LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceError("ASJ.web.000441",
          "Cannot get aliases for [{0}] application, because this application is not deployed.",
          new Object[]{applicationName}, de, null, null);
      }
      return null;
    }
  }// end of getAliases(String applicationName)

  /**
   * Returns all web applications names corresponding to the specified application name.
   * In case DeploymentException is thrown during getting aliases from deploy service - <code>null</code> will be returned.
   * The names are canonicalized.
   *
   * @param applicationName specifies the application name.
   * @return String array with all web applications names corresponding to this application name. The names are canonicalized.
   */
  public String[] getAliasesCanonicalized(String applicationName) {
    String[] aliases = getAliases(applicationName);

    String[] aliasesCanonicalized = null;
    if (aliases != null) {
      aliasesCanonicalized = new String[aliases.length];
      for (int i = 0; i < aliases.length; i++) {
        aliasesCanonicalized[i] = ParseUtils.convertAlias(aliases[i]);
      }
    }

    return aliasesCanonicalized;
  }//end of getAliasesCanonicalized(String applicationName)

  /**
   * Returns all web applications names corresponding to the specified application name. In case DeploymentException is
   * thrown during getting aliases from deploy service - then this exception will be propagated above. The names are
   * canonicalized.
   * USED ONLY IN WCE SCENARIO!
   *
   * @param applicationName
   *          specifies the application name.
   * @return String array with all web applications names corresponding to this application name. The names are
   *         canonicalized.
   * @throws DeploymentException if there are problems during getting aliases from deploy service.
   */
  public String[] getAliasesCanonicalizedIfExists(String applicationName) throws DeploymentException {
    String[] aliases = getDeployCommunicator().getAliases(applicationName);

    String[] aliasesCanonicalized = null;
    if (aliases != null) {
      aliasesCanonicalized = new String[aliases.length];
      for (int i = 0; i < aliases.length; i++) {
        aliasesCanonicalized[i] = ParseUtils.convertAlias(aliases[i]);
      }
    }

    return aliasesCanonicalized;
  }// end of getAliasesCanonicalized(String applicationName)

  /**
   * Call this method only once during service startup and make sure the configurationHandlerFactory in ServiceContext
   * is ready available!
   *
   * @throws ServiceException
   */
  public void loadGlobalDD() throws ServiceException {
    long time = System.currentTimeMillis();
    long newtime = time;
    long newtime1 = time;

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    boolean beDebug = traceLocation.beDebug();
    try {
      // Always validate the global DD -- no need for "backward compatibility" for it
      InputStream global = getGlobalWebDescriptor();
      if (beDebug) {
        newtime = System.currentTimeMillis();
				traceLocation.debugT("getGlobalWebDescriptor() >>> " + (newtime - time));
			}
			InputStream addGlobal = getGlobalAdditionalWebDescriptor();
      if (beDebug) {
        newtime1 = System.currentTimeMillis();
				traceLocation.debugT("getGlobalAdditionalWebDescriptor() >>> " + (newtime1 - newtime));
			}
			globalDD = XmlUtils.parseXml(global, addGlobal, "", Constants.globalWebXmlPath, Constants.globalAddXmlPath, true);
      initGlobalMimeTypes(globalDD);
      initSessionTimeoutFromProps(globalDD);
      initMaxSessionsFromProps(globalDD);
      initCookieConfigsFromProps(globalDD);

      //TODO : Vily G : da se zashtitim tuka ako nqkoi e mahnal error handler-a ni ot global-web.xml da si go slojim

      if (beDebug) {
        newtime = System.currentTimeMillis();
				traceLocation.debugT("XmlUtils.parseXml() >>> " + (newtime - newtime1));
			}
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable t) {
    	//TODO:polly check
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000311",
        "Error loading global web deployment descriptors - global-web.xml.", t, null, null);
      throw new ServiceException(t);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }

    if (beDebug) {
			traceLocation.debugT("Whole time >>> " + (System.currentTimeMillis() - time));
		}
  }//end of loadGlobalDD()

  protected void setDeployCommunicator(DeployCommunicator deployCommunicator) {
    this.deployCommunicator = deployCommunicator;
    if (deployCommunicator != null) {
      sendAppAliasesToHttp();
    }
  }//end of setDeployCommunicator(DeployCommunicator deployCommunicator)

  private void sendAppAliasesToHttp() {
    String allAppls[] = getDeployCommunicator().getDeployedApplications();
    if (allAppls == null) {
      return;
    }

    for (int j = 0; j < allAppls.length; j++) {
      try {
        String aliases[] = getAliases(allAppls[j]);
        if (aliases == null) {
          continue;
        }
        for (int k = 0; k < aliases.length; k++) {
          if (aliases[k].equals("/") || aliases[k].equals("\\")) {
            removeStartedApplication(com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.defaultAliasMB);
          } else {
            removeStartedApplication(new MessageBytes(ParseUtils.separatorsToSlash(aliases[k])));
          }
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  //TODO:Polly ok ? ask Villy
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation,  "ASJ.web.000131",
          "Cannot remove application aliases from the list of started applications.", e, null, null);
      }
    }
  }//end of sendAppAliasesToHttp()

  /**
   * Returns SchemaProcessor for SAP additional web deployment descriptor.
   *
   * @return SchemaProcessor for SAP additional web deployment descriptor.
   */
  public SchemaProcessor getWebJ2eeSchemaProcessor() {
    return webJ2eeSchemaProcessor;
  }//end of getWebJ2eeSchemaProcessor()

  /**
   * Returns SchemaProcessor for tag library descriptor.
   *
   * @return SchemaProcessor for tag library descriptor.
   */
  public SchemaProcessor getWebJspTldSchemaProcessor() {
    return webJspTldSchemaProcessor;
  }//end of getWebJspTldSchemaProcessor()

  /**
   * Returns SchemaProcessor for web deployment descriptor.
   *
   * @return SchemaProcessor for web deployment descriptor.
   */
  public SchemaProcessor getWebSchemaProcessor() {
    return webSchemaProcessor;
  }//end of getWebSchemaProcessor()

  /**
   * Returns WebConverter for old web deployment descriptors.
   *
   * @return Converter for old web deployment descriptors.
   */
  public synchronized WebConverter getWebConverter() {
    if (webConverter == null) {
      webConverter = new WebConverter();
    }
    return webConverter;
  }//end of getWebConverter()

  /**
   * Returns TldConverter for old tag library descriptors.
   *
   * @return Converter for old tag library descriptors.
   */
  public synchronized TldConverter getTldConverter() {
    if (tldConverter == null) {
      tldConverter = new TldConverter();
    }
    return tldConverter;
  }//end of getTldConverter()

  /**
   * This method returns ApplicationContext that is representation of the started web application.
   * When the web application is in STOPPED or IMPLICIT_STOPPED mode, its startup mode is analyzed
   * and depending on this web container will try to start web application which is with "Lazy" startup mode.
   *
   * @param moduleName the name of the web application.
   * @return ApplicationContext that is representation of the started web application.
   */
  public ApplicationContext startLazyApplication(MessageBytes moduleName) {
    ApplicationContext applicationContext = getStartedWebApplicationContext(moduleName);
    if (applicationContext != null && applicationContext.isStarted()) {
      //ok the application is started!
      return applicationContext;
    }

    WebContainerProvider webContainerProvider = (WebContainerProvider) ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider();
    WebModule webModule = (WebModule) webContainerProvider.getDeployedAppl(moduleName.toString());
    if (webModule == null) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000213",
        "Web module [{0}] cannot be invoked for 'Lazy' startup.", new Object[]{moduleName.toString()}, null, null);
      return null;
    }

    try {
      webContainerProvider.getApplicationManager().analyseAppStatusMode(webModule, false);
    } catch (Exception e) {
    	//TODO:Polly ok ?
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000132",
        "Error occurred during retrieving web module context needed for 'Lazy' startup of [{0}] web module. ",
        new Object[]{moduleName.toString()}, e, null, null);
      return null;
    }

    return getStartedWebApplicationContext(moduleName);
  }//end of startLazyApplication(MessageBytes moduleName)

  /**
   * Initializes HashMap with all mime types defined in the global-web.xml.
   * @param global
   */
  private void initGlobalMimeTypes(WebDeploymentDescriptor global) {
    MimeMappingType[] allMimeTypes = global.getMIMEMapping();
    if( allMimeTypes == null || allMimeTypes.length == 0) {
      // almost never as there are 131 mime types currently in global-web.xml
      globalMimes = new HashMap<String, String>(0);
      return;
    }
    globalMimes = new HashMap<String, String>(allMimeTypes.length);
    for (int i = 0; i < allMimeTypes.length; i++) {
      MimeMappingType type = allMimeTypes[i];
      globalMimes.put(type.getExtension().get_value(), type.getMimeType().get_value());
    }
  }

  /**
   * Initializes the sessionTimeout in the global deployment descriptor with the custom settings from the service property if there are any
   */
  private void initSessionTimeoutFromProps(WebDeploymentDescriptor globalDD){
	  WebContainerProperties webProps = ServiceContext.getServiceContext().getWebContainerProperties();
	  if (!webProps.getSessionTimeout().equals("")){
		  com.sap.engine.lib.descriptors5.web.SessionConfigType sessCfg = new com.sap.engine.lib.descriptors5.web.SessionConfigType();
		  com.sap.engine.lib.descriptors5.javaee.XsdIntegerType xsdIntegerType = new com.sap.engine.lib.descriptors5.javaee.XsdIntegerType();
		  xsdIntegerType.set_value(new BigInteger(webProps.getSessionTimeout()));
		  sessCfg.setSessionTimeout(xsdIntegerType);
		  globalDD.setSessionConfig(sessCfg);
	  }
  }

  private void initMaxSessionsFromProps(WebDeploymentDescriptor globalDD){
	  WebContainerProperties webProps = ServiceContext.getServiceContext().getWebContainerProperties();
	  String maxSessionsFromProps = webProps.getMaxSessions();
	  if(!webProps.getMaxSessions().equals("")){
		  globalDD.getWebJ2EEEngine().setMaxSessions(new Integer(Integer.parseInt(maxSessionsFromProps)));
	  }
  }

  private void initCookieConfigsFromProps(WebDeploymentDescriptor globalDD){
	  	boolean isSessCookieConfiguredWithProps = false;
	  	boolean isAppCookieConfiguredWithProps = false;
	  	CookieConfigType cookieConfig = globalDD.getWebJ2EEEngine().getCookieConfig();
	  	WebContainerProperties webProps = ServiceContext.getServiceContext().getWebContainerProperties();
	  	//get current cookie configurations from the global descriptor and afterwards update them with the custom configurations from properties
	  	CookieType updatedSessionCookie = new CookieType();
	  	CookieType updatedAppCookie =  new CookieType();
	  	for (int i=0;cookieConfig != null && cookieConfig.getCookie()!=null &&  i<cookieConfig.getCookie().length; i++){
	  		if (CookieTypeType.APPLICATION.equals(cookieConfig.getCookie()[i].getType())){
	  			updatedAppCookie = cookieConfig.getCookie()[i];
	  		}else{
	  			updatedSessionCookie = cookieConfig.getCookie()[i];
	  		}
	  	}

	  	//get session cookie configurations from Service Properties and apply them if not empty strings
	  	String sessionCookiePathFromProps = webProps.getSessionCookiePath();
	  	if (!sessionCookiePathFromProps.equals("")){
	  		updatedSessionCookie.setPath(sessionCookiePathFromProps);
	  		isSessCookieConfiguredWithProps = true;
	  	}

	  	String sessionCookieDomainFromProps = webProps.getSessionCookieDomain();
	  	if (!sessionCookieDomainFromProps.equals("")){
	  		updatedSessionCookie.setDomain(sessionCookieDomainFromProps);
	  		isSessCookieConfiguredWithProps = true;
	  	}

	  	String sessionCookieMaxAgeFromProps = webProps.getSessionCookieMaxAge();
	  	if(!sessionCookieMaxAgeFromProps.equals("")){
	  		updatedSessionCookie.setMaxAge(Integer.parseInt(sessionCookieMaxAgeFromProps));
	  		isSessCookieConfiguredWithProps = true;
	  	}

	  	//get application cookie configurations from Service Properties and apply them if not empty strings
	  	String appCookiePathFromProps = webProps.getAppCookiePath();
		if (!appCookiePathFromProps.equals("")){
	  		updatedAppCookie.setPath(appCookiePathFromProps);
	  		isAppCookieConfiguredWithProps = true;
	  	}

	  	String appCookieDomainFromProps = webProps.getAppCookieDomain();
	  	if (!appCookieDomainFromProps.equals("")){
	  		updatedAppCookie.setDomain(appCookieDomainFromProps);
	  		isAppCookieConfiguredWithProps = true;
	  	}

	  	String appCookieMaxAgeFromProps = webProps.getAppCookieMaxAge();
	  	if(!appCookieMaxAgeFromProps.equals("")){
	  		updatedAppCookie.setMaxAge(Integer.parseInt(appCookieMaxAgeFromProps));
	  		isAppCookieConfiguredWithProps = true;
	  	}

	  	//array that stores cookies to be updated in the global descriptor's configurations
	  	ArrayList<CookieType> updatedCookies = new ArrayList<CookieType>();
	  	if (isSessCookieConfiguredWithProps){
	  		updatedSessionCookie.setType(CookieTypeType.SESSION);
	  		updatedCookies.add(updatedSessionCookie);
	  	}
	  	if (isAppCookieConfiguredWithProps){
	  		updatedAppCookie.setType(CookieTypeType.APPLICATION);
	  		updatedCookies.add(updatedAppCookie);
	  	}

	  	//if there are any cookies for update set only these cookies
	  	int numberOfCookies = updatedCookies.size();
	  	if(numberOfCookies > 0){
	  		if (cookieConfig != null){
	  			cookieConfig.setCookie(updatedCookies.toArray(new CookieType[numberOfCookies]));
	  		}else{
	  			CookieConfigType config = new CookieConfigType();
	  			config.setCookie(updatedCookies.toArray(new CookieType[numberOfCookies]));
	  			globalDD.getWebJ2EEEngine().setCookieConfig(config);
	  		}
	  	}
  }


  /**
   * Returns HashMap with all mime types defined in the global-web.xml.
   * @return
   */
  public HashMap<String, String> getGlobalMimeTypes(){
    return globalMimes;
  }

}//end of class