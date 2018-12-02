package com.sap.engine.services.webservices.server;

import com.sap.engine.services.webservices.webservices630.server.deploy.WSDeployer;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.jar.JarUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSRuntimeActivator;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.interfaces.webservices.server.WSContainerInterface;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.frame.core.load.ResourceLoader;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.frame.core.load.res.Resource;
import com.sap.engine.frame.core.load.res.JarResource;
import com.sap.engine.frame.core.load.res.MultipleResource;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.lib.security.domain.ProtectionDomainFactory;
import com.sap.engine.lib.util.Set;
import com.sap.tc.logging.Location;

import java.security.CodeSource;
import java.io.File;
import java.io.IOException;import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;
import java.net.URL;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSContainerInterfaceImpl implements WSContainerInterface, WebServicesConstants {

  public static final String SEPARATOR = File.separator;

  private static Set externalDeployedWebServices = new Set();

  private WSDeployer wsDeployer = null;
  private ApplicationServiceContext serviceContext = null;

  private JarUtil jarUtil = null;

  public WSContainerInterfaceImpl() {
    this.wsDeployer = WSContainer.getWSDeployer();
    this.serviceContext = WSContainer.getServiceContext();

    this.jarUtil = new JarUtil();
  }

  public boolean isExernalWSComponent(String applicationName) {
    return externalDeployedWebServices.contains(applicationName);
  }

  public String registerWebServices(String archiveFile) throws WSDeploymentException, WarningException {
    String wsApplicationName = getWSApplicationName();
    extractWSFiles(wsApplicationName, archiveFile);
    registerWSAppClassLoader(wsApplicationName);

    registerWebServices0(wsApplicationName);

    return wsApplicationName;
  }

  public String registerWebServices(String archiveFile, Vector warnings) throws WSDeploymentException {
    String wsApplicationName = getWSApplicationName();
    extractWSFiles(wsApplicationName, archiveFile);
    registerWSAppClassLoader(wsApplicationName);
    try {
      registerWebServices0(wsApplicationName);
    } catch(WarningException e) {
      addToVector(warnings, e.getWarnings());
    }

    return wsApplicationName;
  }

  public void registerWebServices0(String wsApplicationName) throws WSDeploymentException, WarningException {
    Vector warnings = new Vector();
    try {
      String wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(wsApplicationName, WS_EXTERNAL_COMPONENT);
      String webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
      (new WSRuntimeActivator()).start(wsApplicationName, webServicesDir, null);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      Object args[] = new String[] {"Unable to get wsContainer or webservices directory", "application name: " + wsApplicationName};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } catch(WSDeploymentException e) {
      try {
        wsDeployer.rollbackStart(wsApplicationName);
      } catch(WarningException wExc) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching("Warning! ", wExc);
      }
      throw e;
    }

    try {
      wsDeployer.commitStart(wsApplicationName);
    } catch(WarningException e) {
      addToVector(warnings, e.getWarnings());
    }

    externalDeployedWebServices.add(wsApplicationName);

    if(warnings.size() != 0) {
      WarningException e = new WarningException();
      e.setWarnings(warnings);
      throw e;
    }
  }

  public boolean isServiceEndpointRegistered(String transportAddress) {
    return WSContainer.getRuntimeRegistry().contains(transportAddress);
  }

  public void stop() throws WSDeploymentException, WarningException {
    unregisterExternalWebServices();
  }

  private void unregisterExternalWebServices() throws WSDeploymentException, WarningException {
    Enumeration enum1 = externalDeployedWebServices.elements();

    Vector warnings = new Vector();
    while(enum1.hasMoreElements()) {
      String wsApplicationName = (String)enum1.nextElement();
      try {
        unregisterWebServices(wsApplicationName);
      } catch(WarningException e) {
        addToVector(warnings, e.getWarnings());
      }
    }

    if(warnings.size() != 0) {
      WarningException e = new WarningException();
      e.setWarnings(warnings);
      throw e;
    }
  }

  public void unregisterWebServices(String wsApplicationName) throws WSDeploymentException, WarningException {
    String wsContainerDir = null;
    String webServicesDir = null;

    Vector warnings = new Vector();
    try {
      wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(wsApplicationName, 1);
      webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      Object args[] = new String[]{"Unable to get wsContainer or webservices directory", "application name: " + wsApplicationName};
      throw new WSDeploymentException("webservices_5040", args, e);
    }

    deleteWebServicesDir(webServicesDir);
    unregisterAppLoader(wsApplicationName);
    try {
      unregisterWebServices0(wsApplicationName);
    } catch(WarningException e) {
      addToVector(warnings, e.getWarnings());
    }

    externalDeployedWebServices.remove(wsApplicationName);

    if(warnings.size() != 0) {
      WarningException e = new WarningException();
      e.setWarnings(warnings);
      throw e;
    }
  }

  private void unregisterWebServices0(String wsApplicationName) throws WSDeploymentException, WarningException {
    Vector warnings = new Vector();
    try {
      wsDeployer.prepareStop(wsApplicationName, null);
    } catch(WSDeploymentException e) {
      try {
        wsDeployer.rollbackStop(wsApplicationName);
      } catch(WarningException wExc) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(wExc);
      }

      throw e;
    } catch(WarningException e) {
      addToVector(warnings, e.getWarnings());
    }

    try {
      wsDeployer.commitStop(wsApplicationName);
    } catch(WarningException e) {
      addToVector(warnings, e.getWarnings());
    }

    if(warnings.size() != 0) {
      WarningException e = new WarningException();
      e.setWarnings(warnings);
      throw e;
    }
  }

  private String getWSApplicationName() {
    return  "externalWS" + System.currentTimeMillis();
  }

  private void extractWSFiles(String applicationName, String archiveFile) throws WSDeploymentException {
    try {
      String wsContainerDir = wsDeployer.getWSContainerDir(applicationName, WS_EXTERNAL_COMPONENT);
      jarUtil.extractJar(archiveFile, wsContainerDir);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to get wsContainer or webservices directory", "application name: " + applicationName};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to extract webservices files from the archive", "application name: " + applicationName};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private void registerWSAppClassLoader(String applicationName) throws WSDeploymentException {
    ResourceLoader appLoader = null;
    String wsContainerDir = null;

    try {
      wsContainerDir = WSContainer.getWSDeployer().getWSContainerDir(applicationName, 1);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      Object args[] = new String[]{"Unable to get wsContainer directory, application name: " + applicationName};
      throw new WSDeploymentException("webservices_5040", args, e);
    }

    String webServicesDir = WSDirsHandler.getWebServicesDir(wsContainerDir);
    String wsFilesForClassLoader[] = getWSFilesForClassLoader(webServicesDir);

    try {
      ClassLoader serviceContextLoader = serviceContext.getClass().getClassLoader();
      Class tmpClass = serviceContextLoader.loadClass("com.sap.engine.frame.ApplicationServiceContext");
      ClassLoader frameLoader = tmpClass.getClassLoader();
      
      java.security.cert.Certificate[] certs = null;
      java.security.ProtectionDomain protectionDomain = ProtectionDomainFactory.getFactory().registerProtectionDomain(applicationName, new CodeSource(new URL("file:/" + wsContainerDir + SEPARATOR), certs));
      MultipleResource multipleResource = getMultipleResource(wsFilesForClassLoader);
      appLoader = new ResourceLoader(applicationName, multipleResource, frameLoader, protectionDomain);
      serviceContext.getCoreContext().getLoadContext().register(appLoader);
      String wsReferences[] = wsDeployer.getWSReferences();
      makeReferences(appLoader.getName(), wsReferences);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);
      Object args[] = new String[]{"Unable to define application loader", "application name: " + applicationName};
      throw new WSDeploymentException("webservices_5040", args, e);
    }
  }

  private void unregisterAppLoader(String appLoaderName) throws WSDeploymentException {
    LoadContext loadContext = serviceContext.getCoreContext().getLoadContext();

    String[] wsReferences = wsDeployer.getWSReferences();
    destroyWSReferences(appLoaderName, wsReferences);

    ResourceLoader appLoader = (ResourceLoader)loadContext.getClassLoader(appLoaderName);
    try {
      loadContext.unregister(appLoader);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to unregister application loader ", "application loader name: " + appLoader.getName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

    private String[] getWSFilesForClassLoader(String webservicesDir) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect web services jars for classloader from directory " + webservicesDir + ". ";

    String wsDirs[] = (new File(webservicesDir)).list();
    if(wsDirs == null) {
        return new String[0];
    }

    String wsFilesForClassLoader[] = new String[0];
    for(int i = 0; i < wsDirs.length; i++) {
      try {
        String currentWSDir = wsDirs[i];
        File currentWSDirFile = new File(currentWSDir);
        if((new File(currentWSDir)).getName().equals("app_jars")) {
          wsFilesForClassLoader = WSUtil.unifyStrings(new String[][]{wsFilesForClassLoader, currentWSDirFile.list()});
        } else {
          String singleWSJars[] = getSingleWSFilesForClassLoader(currentWSDir);
          wsFilesForClassLoader = WSUtil.unifyStrings(new String[][]{wsFilesForClassLoader, singleWSJars});
        }
      } catch(WSDeploymentException e) {
        Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
        wsDeployLocation.catching(e);
        Object args[] = new String[]{excMsg, "none"};
        throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
      }
    }

    return wsFilesForClassLoader;
  }

  private String[] getSingleWSFilesForClassLoader(String wsDirectory) throws WSDeploymentException {
    String excMsg = "Error occurred, trying to collect jars for classloader for a web service with directory " + wsDirectory + ". ";
    String wsJarsForClassLoader[] = new String[0];
    try {
      Properties mappings = WSRuntimeActivator.loadMappings(wsDirectory);
      String jarsDir = WSDirsHandler.getJarsDir(wsDirectory, mappings);
      wsJarsForClassLoader = (new File(jarsDir)).list();
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);
      Object args[] = new String[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
    return wsJarsForClassLoader;
  }

  private MultipleResource getMultipleResource(String[] files) {
    MultipleResource multipleResource = new MultipleResource();

    int filesSize = files.length;
    for (int i = 0; i < filesSize; i++) {
      String file = files[i];
      Resource resource = new JarResource(file);
      multipleResource.addResource(resource);
    }
    return multipleResource;
  }

  private void makeReferences(String fromLoader, String[] references) {
    LoadContext loadContext = serviceContext.getCoreContext().getLoadContext();
    int size = references.length;

    for (int i = 0; i < size; i++) {
      String reference = references[i];
      String toLoaderName = reference.substring(reference.indexOf(":") + 1).trim();
      loadContext.registerReference(fromLoader,  toLoaderName);
    }
  }

  private void destroyWSReferences(String fromLoader, String[] references) {
    LoadContext loadContext = serviceContext.getCoreContext().getLoadContext();
    int size = references.length;

    for (int i = 0; i < size; i++) {
      String reference = references[i];
      String toLoaderName = reference.substring(reference.indexOf(":") + 1).trim();
      loadContext.registerReference(fromLoader,  toLoaderName);
    }
  }

  private void deleteWebServicesDir(String webServicesDir) throws WSDeploymentException {
    try {
      IOUtil.deleteDir(webServicesDir);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to delete webservices directory", "application name: " + webServicesDir};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }

  }

  private void addToVector(Vector vector, Object[] array) {
    if(vector == null || array == null) {
      return;
    }

    for (int i = 0; i< array.length; i++) {
      Object obj = array[i];
      if (obj != null) {
        vector.add(obj);
      }
    }
  }

}