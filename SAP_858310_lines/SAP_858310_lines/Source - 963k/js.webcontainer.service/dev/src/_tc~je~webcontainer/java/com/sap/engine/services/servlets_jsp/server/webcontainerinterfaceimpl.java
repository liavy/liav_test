/**
 * Copyright (c) 2002-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.state.ManagementListener;
import com.sap.engine.lib.descriptors5.javaee.ListenerType;
import com.sap.engine.lib.descriptors5.web.FilterType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.op.util.Status;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.HttpSessionDebugListener;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.WebContainerRuntimeCallback;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebRemoteException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.tc.logging.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author
 * @version 1.0
 */
public class WebContainerInterfaceImpl extends javax.rmi.PortableRemoteObject implements WebContainerInterface {
  private static Location currentLocation = Location.getLocation(WebContainerInterfaceImpl.class);
  private WebContainerRuntimeCallback admin = null;

  public WebContainerInterfaceImpl() throws RemoteException {
  }//end of constructor

  /**
   * Lists servlets of specified web application. If the application is in stopped mode, this
   * method returns only those servlets that are specified in deployment descriptor
   *
   * @param aliasName name of web application
   * @return vector with all servlets
   */
  public Vector listServlets(String aliasName) throws java.rmi.RemoteException {
    Vector servlets = new Vector();

    ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasName.getBytes()));
    if (scf == null) {
      String applicationName = getApplicationName(aliasName);

      WebDeploymentDescriptor descriptor = getDescriptor(aliasName, applicationName);
      if (descriptor == null) {
        return null;
      }

      ServletType serlvetDescriptors[] = descriptor.getServlets();
      for (int i = 0; serlvetDescriptors != null && i < serlvetDescriptors.length; i++) {
        if (!serlvetDescriptors[i].getChoiceGroup1().isSetJspFile()) {
          servlets.add(serlvetDescriptors[i].getServletName().get_value());
        }
      }

      return servlets;
    } else {
      Enumeration servletNames = scf.getWebComponents().getServletNames();
      while (servletNames.hasMoreElements()) {
        String nextServlet = (String) servletNames.nextElement();
        servlets.add(nextServlet);
      }
    }
    return servlets;
  }

  /**
   * Lists jsp files of specified web application. If the application is in stopped mode
   * this method returns null
   *
   * @param aliasName name of  web application
   * @return vector with all jsp
   */
  public Vector<String> listJsp(String aliasName) throws java.rmi.RemoteException {
    Vector<String> jsp = new Vector<String>();

    ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasName.getBytes()));
    if (scf != null) {
      Set<String> jspNames = scf.getWebComponents().getJspNames();
      jsp.addAll(jspNames);
      
      Vector<String> jspServletNames = scf.getWebComponents().getJspServletsNames();
      if( jspServletNames != null ) {
        jsp.addAll(jspServletNames);
      }
    } else {
      String applicationName = getApplicationName(aliasName);

      WebDeploymentDescriptor descriptor = getDescriptor(aliasName, applicationName);
      if (descriptor == null) {
        return null;
      }

      ServletType serlvetDescriptors[] = descriptor.getServlets();
      for (int i = 0; serlvetDescriptors != null && i < serlvetDescriptors.length; i++) {
        if (serlvetDescriptors[i].getChoiceGroup1().isSetJspFile()) {
          jsp.add(serlvetDescriptors[i].getChoiceGroup1().getJspFile().get_value());
        }
      }
    }

    return jsp;
  }

  /**
   * Lists filters of specified web application
   *
   * @param aliasName name of  web application
   * @return vector with all filters
   */
  public Vector listFilters(String aliasName) throws java.rmi.RemoteException {
    Vector filters = new Vector();

    ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasName.getBytes()));
    if (scf == null) {
      String applicationName = getApplicationName(aliasName);

      WebDeploymentDescriptor descriptor = getDescriptor(aliasName, applicationName);
      if (descriptor == null) {
        return null;
      }

      FilterType filterDesciptors[] = descriptor.getFilters();
      if (filterDesciptors == null) {
        return null;
      }

      for (int i = 0; i < filterDesciptors.length; i++) {
        filters.add(filterDesciptors[i].getFilterName().get_value());
      }
    } else {
      Set<String> filetrNamesSet = scf.getWebComponents().getFiltersNames();
      filters.addAll(filetrNamesSet);
    }

    return filters;
  }

  /**
   * Lists listeners of specified web application
   *
   * @param aliasName name of  web application
   * @return vector with all listeners
   */
  public Vector listListeners(String aliasName) throws java.rmi.RemoteException {
    Vector listeners = new Vector();

    ApplicationContext scf = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasName.getBytes()));
    if (scf == null) {
      String applicationName = getApplicationName(aliasName);

      WebDeploymentDescriptor descriptor = getDescriptor(aliasName, applicationName);
      if (descriptor == null) {
        return null;
      }

      ListenerType listenersDescriptor[] = descriptor.getListeners();
      if (listenersDescriptor == null || listenersDescriptor.length == 0) {
        return null;
      }

      for (int i = 0; i < listenersDescriptor.length; i++) {
        listeners.add(listenersDescriptor[i].getListenerClass().get_value());
      }
    } else {
      Enumeration en = scf.getWebComponents().getListenersNames();
      while (en.hasMoreElements()) {
        listeners.add(en.nextElement());
      }
    }
    return listeners;
  }

  /**
   * Lists name of all applications deployed on web container
   *
   * @return vector with the applications
   */
  public Vector getAllApplications() throws java.rmi.RemoteException, DeploymentException {
    Vector allAppls = new Vector();

    String[] allApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getDeployedApplications();
    for (int i = 0; i < allApplications.length; i++) {
      String applicationName = allApplications[i];
      String[] aliases = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAliases(applicationName);
      if (aliases != null && aliases.length != 0) {
        allAppls.add(applicationName);
      }
    }

    return allAppls;
  }

  private String getApplicationName(String aliasName) throws java.rmi.RemoteException {
    String appls[] = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getDeployedApplications();
    if (appls == null) {
      throw new WebRemoteException(WebRemoteException.CANNOT_FIND_APPLICATION_NAME_FOR_ALIAS, new Object[]{aliasName});
    }

    for (int i = 0; i < appls.length; i++) {
      try {
        String aliases[] = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAliases(appls[i]);
        for (int j = 0; j < aliases.length; j++) {
          if (aliases[j].equals(aliasName)) {
            return appls[i];
          }
        }
      } catch (DeploymentException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000225",
          "Cannot get the name of the Java EE application that has a web application with alias " +
          "[{0}]. The error occurred while listing all aliases of the Java EE application [{1}].", 
          new Object[]{aliasName, appls[i]}, e, null, null);
      }
    }

    throw new WebRemoteException(WebRemoteException.CANNOT_FIND_APPLICATION_NAME_FOR_ALIAS, new Object[]{aliasName});
  }

  public Vector getAliases(String applicationName) throws java.rmi.RemoteException {
    try {
      String[] allAliases = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getAliases(applicationName);
      if (allAliases == null) {
        return null;
      } else {
        return new Vector(Arrays.asList(allAliases));
      }
    } catch (DeploymentException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000164",
        "Cannot list the web applications of the Java EE application [{0}].", new Object[]{applicationName}, e, null, null);
      throw new WebRemoteException(WebRemoteException.CANNOT_FIND_ALIAS_NAME_FOR_APPLICATION, new Object[]{applicationName}, e);
    }
  }


  /**
   * Save changes
   */
  public boolean save(String aliasName, WebDeploymentDescriptor descriptor) throws RemoteException {
    String applicationName = getApplicationName(aliasName);

    if (descriptor == null) {
      return false;
    }

    if (descriptor.equals(getDescriptor(aliasName, applicationName))) {
      return true;
    }

    String xmlFile = getDescriptorPath(aliasName) + "web.xml";
    String additionalXmlFile = getDescriptorPath(aliasName) + "web-j2ee-engine.xml";

    FileOutputStream standartFos = null;
    try {
      standartFos = new FileOutputStream(xmlFile);
      descriptor.writeStandartDescriptorToStream(standartFos);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000165",
        "Cannot store the modified web descriptor of the web application " +
        "[{0}]. Cannot write the xml file [{1}]. Probably the changes will be lost.", new Object[]{aliasName, xmlFile}, e, null, null);
      return false;
    } finally {
      try {
        standartFos.close();
      } catch (IOException ex) {
        if (LogContext.getLocationDeploy().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000570", "Cannot close FileOutputStream of file: {0}", new Object[]{xmlFile}, null, null);
				}
      }
    }

    FileOutputStream additionalFos = null;
    try {
      additionalFos = new FileOutputStream(additionalXmlFile);
      descriptor.writeStandartDescriptorToStream(additionalFos);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000166",
        "Cannot store the modified web descriptor of the web application " +
        "[{0}]. Cannot write the xml file [{1}]. Probably the changes will be lost.", new Object[]{aliasName, additionalXmlFile}, e, null, null);
      return false;
    } finally {
      if (additionalFos != null) {
        try {
          additionalFos.close();
        } catch (IOException ex) {
          if (LogContext.getLocationDeploy().beWarning()) { 
						LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceWarning("ASJ.web.000571", "Cannot close FileOutputStream of file: {0}", new Object[]{additionalXmlFile}, null, null);
					}
        }
      }
    }

    try {
      Configuration config = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().startRuntimeChanges(applicationName);
      Configuration servletConfig = config.getSubConfiguration(Constants.CONTAINER_NAME);
      Configuration updateConfig = servletConfig.getSubConfiguration(Constants.UPDATE);

      store2DBase(new File(xmlFile), aliasName + "/root/WEB-INF/web.xml", applicationName, updateConfig);

      File addXmlFile = new File(additionalXmlFile);
      if (addXmlFile.exists()) {
        store2DBase(addXmlFile, aliasName + "/root/WEB-INF/web-j2ee-engine.xml", applicationName, updateConfig);
      }

      ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().makeRuntimeChanges(applicationName, true);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000167",
        "Cannot store the modified web descriptor of the web application " +
        "[{0}]. Cannot write to database or cannot execute runtime changes for the updated application " +
        "[{1}]. Probably the changes will be lost.", new Object[]{aliasName, applicationName}, e, null, null);
      ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().rollbackRuntimeChanges(applicationName);
      return false;
    }
    return true;
  }

  private void store2DBase(File file, String entry, String applicationName, Configuration config) throws ConfigurationException, DeploymentException {
    long fileId = -1;
    Map fileEntries = config.getAllFileEntries();
    if (!fileEntries.isEmpty()) {
      Set keySet = fileEntries.keySet();
      Iterator iter = keySet.iterator();
      while (iter.hasNext()) {
        String nextFilename = (String) iter.next();
        String path = null;
        try {
          String fsId = nextFilename.substring(1);
          path = (String) config.getConfigEntry(fsId);
          if (entry.equals(path)) {
            try {
              fileId = Long.parseLong(fsId);
            } catch (NumberFormatException e) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000168",
                "Cannot parse fileId [{0}] to long value in order to make runtime changes.", new Object[]{fsId}, e, null, null);
            }
            break;
          }
        } catch (ConfigurationException e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000169",
            "Configuration error while making runtime changes.", e, null, null);
        }
      }
    }

    if (fileId == -1) {
      String fileCounter = (String) config.getConfigEntry(Constants.FILE_COUNTER);
      fileId = Long.parseLong(fileCounter);
      config.modifyConfigEntry(Constants.FILE_COUNTER, "" + (fileId + 1));
    }

    ConfigurationUtils.addConfigEntry(config, "" + fileId, entry, applicationName, true, true);

    ConfigurationUtils.addFileEntryByKey(config, "#" + fileId, file, applicationName, true, true);
  }

  private String getDescriptorPath(String aliasName) throws RemoteException {
    String applicationName = getApplicationName(aliasName);
    String aliasDir = aliasName;
    if (aliasName.equals("/")) {
      aliasDir = Constants.defaultAliasDir;
    }

    try {
      return ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyWorkDirectory(applicationName) + File.separator + aliasDir.replace('/', File.separatorChar) + File.separator + "root" + File.separator + "WEB-INF" + File.separator;
    } catch (IOException ioe) {
      throw new WebRemoteException(WebRemoteException.CANNOT_GET_PATH_TO_DESCRIPTOR_FOR_WEB_APPLICATION, new Object[]{aliasName}, ioe);
    }
  }

  public WebDeploymentDescriptor getDescriptor(String aliasName, String applicationName) throws RemoteException {
    WebDeploymentDescriptor webDesc = null;

    File standartXmlFile = new File(getDescriptorPath(aliasName) + "web.xml");
    File additionalXmlFile = new File(getDescriptorPath(aliasName) + "web-j2ee-engine.xml");
    try {
      FileInputStream fis = new FileInputStream(standartXmlFile);

      FileInputStream addfis = null;
      if (additionalXmlFile.exists()) {
        addfis = new FileInputStream(additionalXmlFile);
      }

      webDesc = XmlUtils.parseXml(fis, addfis, aliasName, standartXmlFile.getName(), additionalXmlFile.getName(), false);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000226",
        "Cannot load descriptor for one of the files: {0}, {1}.", new Object[]{standartXmlFile, additionalXmlFile}, e, null, null);
      return null;
    }

    return webDesc;
  }

  public boolean isStopped(String applicationName) throws RemoteException {
    return (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStatus(applicationName) == Status.STOPPED.getId().byteValue()) ||
      (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStatus(applicationName) == Status.IMPLICIT_STOPPED.getId().byteValue());
  }

  public WebDeploymentDescriptor getGlobalWebXml() throws RemoteException {
    return ServiceContext.getServiceContext().getDeployContext().getGlobalDD();
  }

  public void update(String appName) throws RemoteException {
    if (admin != null) {
      admin.update(appName, this);
    }
  }

  public void register(WebContainerRuntimeCallback admin) {
    this.admin = admin;
  }

  public void removeApp(String applicationName) throws RemoteException {
    if (admin != null) {
      admin.removeApp(applicationName, this);
    }
  }

  public void appStarted(String applicationName) throws RemoteException {
    if (admin != null) {
      admin.appStarted(applicationName, this);
    }
  }

  public void appStopped(String applicationName) throws RemoteException {
    if (admin != null) {
      admin.appStopped(applicationName, this);
    }
  }

  public void registerHttpSessionDebugListener(HttpSessionDebugListener listener, String debugParamName) {
    if (listener == null || debugParamName == null || debugParamName.trim().length() == 0) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.CANNOT_REGISTER_SESSION_DEBUG_LISTENER);
    }

    ServiceContext.getServiceContext().setHttpSessionDebugListener(listener);
    ServiceContext.getServiceContext().setDebugRequestParameterName(debugParamName.trim());
  }

  public void unregisterHttpSessionDebugListener() {
    ServiceContext.getServiceContext().setHttpSessionDebugListener(null);
    ServiceContext.getServiceContext().setDebugRequestParameterName(null);
  }

  public void registerManagementListener(ManagementListener managementListener) {
    //todo registerManagmentListener
  }

  // -------------------- METHODS USED FOR MONITORING --------------------

  public int getCurrentHttpSessions() {
    return ServiceContext.getServiceContext().getWebMonitoring().getCurrentHttpSessions();
  }

  public long getHttpSessionsInvalidatedByApplication() {
    return ServiceContext.getServiceContext().getWebMonitoring().getHttpSessionsInvalidatedByApplication();
  }

  public long getTimedOutHttpSessions() {
    return ServiceContext.getServiceContext().getWebMonitoring().getTimedOutHttpSessions();
  }

  public int getCurrentSecuritySessions() {
    return ServiceContext.getServiceContext().getWebMonitoring().getCurrentSecuritySessions();
  }

  public long getSecuritySessionsInvalidatedByApplication() {
    return ServiceContext.getServiceContext().getWebMonitoring().getSecuritySessionsInvalidatedByApplication();
  }

  public long getTimedOutSecuritySessions() {
    return ServiceContext.getServiceContext().getWebMonitoring().getTimedOutSecuritySessions();
  }

  public long getAllRequestsCount() {
    return ServiceContext.getServiceContext().getWebMonitoring().getAllRequestsCount();
  }

  public long getError500Count() {
	    return ServiceContext.getServiceContext().getWebMonitoring().getError500Count();
  }
  
  public Serializable[][] getError500CategorizationEntries(){
	  return ServiceContext.getServiceContext().getWebMonitoring().getError500CategorizationEntries();
  }
  
  public long getAllResponsesCount() {
    return ServiceContext.getServiceContext().getWebMonitoring().getAllResponsesCount();
  }

  public long getTotalResponseTime() {
    return ServiceContext.getServiceContext().getWebMonitoring().getTotalResponseTime();
  }

}
