/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.runtime.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.NameNotFoundException;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.interfaces.webservices.runtime.ServletsHelper;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.descriptors.WSDLAbstractGeneratorImpl;
import com.sap.tc.logging.Location;

public class ServletsHelperImpl implements ServletsHelper {
  public static final String ROOT_CONFIGURATION_NAME = "WebServicesAddOn";

  private static ConfigurationHandler handler = null;
  private static Configuration rootConfiguration = null;

  public static ConfigurationHandler getHandler() throws ConfigurationException {
    if (handler == null) {
      handler = WSContainer.getServiceContext().getCoreContext().getConfigurationHandlerFactory().getConfigurationHandler();
    }

    return handler;
  } 

  /***
  private static Configuration getConfiguration(ConfigurationHandler handler, int access) throws ConfigurationException {
    Configuration configuration;
    String[] names = handler.getAllRootNames();
    boolean rootExists = false;
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(ROOT_CONFIGURATION_NAME)) {
        rootExists = true;
      }
    }
    if (!rootExists) {
      if (access == ConfigurationHandler.WRITE_ACCESS) {
        configuration = handler.createRootConfiguration(ROOT_CONFIGURATION_NAME);
      } else {
        return null;
      }
    } else {
      if (access == ConfigurationHandler.WRITE_ACCESS) {
        configuration = handler.openConfiguration(ROOT_CONFIGURATION_NAME, ConfigurationHandler.WRITE_ACCESS);
      } else {
        configuration = handler.openConfiguration(ROOT_CONFIGURATION_NAME, ConfigurationHandler.READ_ACCESS);
      }
    }
    return configuration;
  }
  /***/

  public static Configuration getSubConfiguration(ConfigurationHandler handler, String subConfigName, int access) throws ConfigurationException {
    Configuration configuration = getConfiguration(handler, access);
    if (configuration == null) {
      return null;
    }

    Configuration subConfiguration;
    if (!configuration.existsSubConfiguration(subConfigName)) {
      if (access == ConfigurationHandler.READ_ACCESS) {
        return null;
      }
      subConfiguration = configuration.createSubConfiguration(subConfigName);
    } else {
      subConfiguration = configuration.getSubConfiguration(subConfigName);
    }

    return subConfiguration;
  }

  private static Configuration getConfiguration(ConfigurationHandler handler, int access) throws ConfigurationException {
    if (rootConfiguration == null || !rootConfiguration.isValid()) {
      rootConfiguration = null;

      try {
        rootConfiguration = handler.openConfiguration(ROOT_CONFIGURATION_NAME, access);
      } catch (NameNotFoundException ex) {
        Location.getLocation(WSLogging.RUNTIME_LOCATION).catching("Cannot find configuration " + ROOT_CONFIGURATION_NAME, ex);
        if (access == ConfigurationHandler.WRITE_ACCESS) {
          rootConfiguration = handler.createRootConfiguration(ROOT_CONFIGURATION_NAME);
        }
      }
    }

    return rootConfiguration;
  }

  public static Configuration openSubConfiguration(ConfigurationHandler handler, String subConfigName, int access) throws ConfigurationException {
    Configuration subConfiguration = null, configuration = getConfiguration(handler, access);

    if (configuration == null) {
      return null;
    }    

    try {
      subConfiguration = configuration.getSubConfiguration(subConfigName);
    } catch (NameNotFoundException ex) {
      Location.getLocation(WSLogging.RUNTIME_LOCATION).catching("Cannot find sub-configuration " + subConfigName, ex);
      if (access == ConfigurationHandler.WRITE_ACCESS) {
        subConfiguration = configuration.createSubConfiguration(subConfigName);
      }    
    }

    return subConfiguration;
  }

  public void sendFile(String name, String type, byte[] data) throws Exception {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    try {
      ConfigurationHandler handler = getHandler();
      Configuration configuration = getSubConfiguration(handler, type, ConfigurationHandler.WRITE_ACCESS);
      configuration.updateFileAsStream(name, new ByteArrayInputStream(data), true);
      handler.commit();
      handler.closeAllConfigurations();
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
    }
  }

//  public boolean fileExists(String name, String type) throws Exception {
//    ClassLoader loader = Thread.currentThread().getContextClassLoader();
//    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
//    try {
//      ConfigurationHandler handler = getHandler();
//      Configuration configuration = getSubConfiguration(handler, type, ConfigurationHandler.READ_ACCESS);
//      if (configuration == null) {
//        return false;
//      }
//      boolean exists = configuration.existsFile(name);
//      handler.closeAllConfigurations();
//
//      return exists;
//    } finally {
//      Thread.currentThread().setContextClassLoader(loader);
//    }
//  }

  private byte[] getFile(String name, String type) throws Exception {
    ConfigurationHandler handler = getHandler();
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    try {
      Configuration configuration = getSubConfiguration(handler, type, ConfigurationHandler.READ_ACCESS);
      if (configuration == null || !configuration.existsFile(name)) {
        throw new Exception("Such a file does not exist: type = " + type + ", name = " + name);
      }
      InputStream in = configuration.getFile(name);
      byte[] buf = new byte[10];
      int l;
      int offset = 0;
      while ((l = in.read(buf, offset, buf.length - offset)) != -1) {
        offset += l;
        if (offset == buf.length) {
          byte[] temp = new byte[buf.length * 2];
          System.arraycopy(buf, 0, temp, 0, buf.length);
          buf = temp;
        }
      }

      byte[] file = new byte[offset];
      System.arraycopy(buf, 0, file, 0, offset);
      return file;
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
      handler.closeAllConfigurations();
    }
  }

//  public UDDIRegistry[] getUDDIRegistries() throws Exception {
//    WSManagementInterface mngInt = WSManagementInterfaceImpl.getManagementInterface();
//    UDDIRegistry[] registries = mngInt.getUDDIRegistries();
////    Vector[] registriesVector = new Vector[registries.length];
////    for (int i = 0; i < registries.length; i++) {
////      Vector reg = new Vector();
////      UDDIRegistry registry = registries[i];
////      reg.addElement(registry.getName());
////      reg.addElement(registry.getInquiryURL());
////      reg.addElement(registry.getPublishURL());
////
////      registriesVector[i] = reg;
////    }
////    return registriesVector;
//    return registries;
//  }
//
//  public void addUDDIRegistry(UDDIRegistry registry) throws Exception {
//    WSManagementInterface mngInt = WSManagementInterfaceImpl.getManagementInterface();
////    String name = registry.elementAt(0).toString();
////    String inquireURL = registry.elementAt(1).toString();
////    String publishURL = registry.elementAt(2).toString();
////    UDDIRegistry reg = new UDDIRegistry(name, inquireURL, publishURL);
//
////    mngInt.addUDDIRegistry(reg);
//    mngInt.addUDDIRegistry(registry);
//  }

  public void generateWSDLFromWSD(String wsdID, OutputStream result) throws Exception {
    byte[] wsd = getFile(wsdID, "wsdGUID");
    
    
/*    File dumpFile = new File("C:\\testWSD.xml");   
    FileOutputStream test = new FileOutputStream(dumpFile);
    test.write(wsd);
    test.flush();
    test.close();*/

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    SAXParser parser = factory.newSAXParser();
    VIHandler handler = new VIHandler();
    InputStream in = new ByteArrayInputStream(wsd);
    parser.parse(in, handler, wsdID);
    in.close();
    String[] viIds = handler.getVIIds();
    InputStream[] vis = new InputStream[viIds.length];
    String[] rpcLocations = new String[viIds.length];
    String[] docLocations = new String[viIds.length];
    String wsdName = handler.getWSDName();
    String wsd2WSDLServletPath = "/wsd2wsdl?wsd=" + URLEncoder.encode(wsdName); //$JL-I18N$
    for (int i = 0; i < viIds.length; i++) {
      vis[i] = new ByteArrayInputStream(getFile(viIds[i], "viGUID"));
      rpcLocations[i] = wsd2WSDLServletPath + "&vi=" + viIds[i] + "&style=rpc";
      docLocations[i] = wsd2WSDLServletPath + "&vi=" + viIds[i] + "&style=document";
    }
    String wsdDocumentation = null;
    try {
      String docID = wsdID + "_en";
      byte[] wsdDoc = getFile(docID, "wsdGUID");
      factory = SAXParserFactory.newInstance();
      parser = factory.newSAXParser();
      DocumentationHandler documentationHandler = new DocumentationHandler();
      in = new ByteArrayInputStream(wsdDoc);
      parser.parse(in, documentationHandler);
      in.close();
      wsdDocumentation = documentationHandler.getDocumentation();
    } catch (Exception e) {
      Location.getLocation(WSLogging.RUNTIME_LOCATION).catching("English documentation file cannot be found. Probably it is not uploaded.", e);
    }
    WSDLAbstractGeneratorImpl.generatePortTypesWSDL(wsdName, wsdDocumentation, vis, rpcLocations, docLocations, result);
  }

  public void generateWSDLFromVI(String viID, String wsdName, String style, OutputStream result) throws Exception {
    byte[] vi = getFile(viID, "viGUID");
    InputStream viStream = new ByteArrayInputStream(vi);
    try {
      if ("rpc".equals(style)) {
        WSDLAbstractGeneratorImpl.generateRPCPortType(wsdName, viStream, result);
      } else if ("document".equals(style)) {
        WSDLAbstractGeneratorImpl.generateDocumentPortType(wsdName, viStream, result);
      } else {
        throw new Exception("Unknown style passed for generating PortTypes of the WSD WSDL: " + style);
      }
    } finally {
      viStream.close();
    }
  }

//  public String[] getAddressPointsForWS(String hostName, int port, String serviceName, String applicationName, String jarName) throws Exception {
//    WSRuntimeDefinition webService = getWS(serviceName, applicationName, jarName);
//    if (webService != null) {
//      ServiceEndpointDefinition[] ports = webService.getServiceEndpointDefinitions();
//      String[] addressPoints = new String[ports.length];
//      for (int j = 0; j < ports.length; j++) {
//        addressPoints[j] = ServiceGenerator.generateHTTPLocationAttrValue("http://" + hostName + ":" + port, ports[j], null, true);
//      }
//      return addressPoints;
//    }
//    throw new Exception("The specified web service cannot be found: serviceName = " + serviceName + ", applicationName = " + applicationName + ", jarName = " + jarName);
//  }
//
////  private WSRuntimeDefinition getWS(String serviceName, String applicationName, String jarName) throws RemoteException  {
////    WSManagementInterface mngInt = WSManagementInterfaceImpl.getManagementInterface();
////    WSRuntimeDefinition[] webServices = mngInt.listWebServices();
////    WSIdentifier wsId = new WSIdentifier(applicationName, jarName, serviceName);
////    for (int i = 0; i < webServices.length; i++) {
////      WSRuntimeDefinition webService = webServices[i];
////      if (wsId.equals(webService.getWSIdentifier())) {
////        return webService;
////      }
////    }
////    return null;
////  }
//
//  public String getUDDIKeyForWS(String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception {
//    WSRuntimeDefinition webService = getWS(serviceName, applicationName, jarName);
//    if (webService != null) {
//      UDDIPublication[] publications = webService.getUddiPublications();
//      if (publications == null) {
//        return webService.getUddiKey();
//      }
//      String regInquiryURL = registry.getInquiryURL();
//      String regPublishURL = registry.getPublishURL();
//      for (int i = 0; i < publications.length; i++) {
//        UDDIPublication publication = publications[i]; 
//        String inquiryURL = publication.getInquiryURL();
//        String publishURL = publication.getPublishURL();
//        if (regInquiryURL.equals(inquiryURL) && regPublishURL.equals(publishURL)) {
//          return publication.getServiceKey();
//        }
//      }
//      return webService.getUddiKey();
//    } else {
//      throw new Exception("The specified web service cannot be found: serviceName = " + serviceName + ", applicationName = " + applicationName + ", jarName = " + jarName);
//    }
//  }
//
//  public String[] getWSDRefereneces(String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception {
//    WSRuntimeDefinition webService = getWS(serviceName, applicationName, jarName);
//    if (webService != null) {
//      UDDIPublication[] publications = webService.getWsdUDDIPublications();
//      if (publications == null) {
//        return new String[0];
//      }
//      Vector keysVector = new Vector();
//      String regInquiryURL = registry.getInquiryURL();
//      String regPublishURL = registry.getPublishURL();
//      for (int i = 0; i < publications.length; i++) {
//        UDDIPublication publication = publications[i]; 
//        String inquiryURL = publication.getInquiryURL();
//        String publishURL = publication.getPublishURL();
//        if (inquiryURL == null || publishURL == null || (inquiryURL.equals(regInquiryURL) && publishURL.equals(regPublishURL))) {
//          keysVector.addElement(publications[i].getServiceKey());
//        }
//      }
//      String[] keys = new String[keysVector.size()];
//      keysVector.copyInto(keys);
//      return keys;
//    } else {
//      throw new Exception("The specified web service cannot be found: serviceName = " + serviceName + ", applicationName = " + applicationName + ", jarName = " + jarName);
//    }
//  }
//
//  public void setUDDIKeyToWS(String uddiKey, String serviceName, String applicationName, String jarName, UDDIRegistry registry) throws Exception {
//    WSRuntimeDefinition webService = getWS(serviceName, applicationName, jarName);
//    if (webService != null) {
//      UDDIPublication[] publications = webService.getUddiPublications();
//      if (publications == null) {
//        publications = new UDDIPublication[1];
//      } else {
//        for (int i = 0; i < publications.length; i++) {
//          UDDIPublication publication = publications[i];
//          if (publication.getServiceKey().equals(uddiKey) &&
//              publication.getInquiryURL().equals(registry.getInquiryURL()) &&
//              publication.getPublishURL().equals(registry.getPublishURL())) {
//            return;
//          }
//        }
//        UDDIPublication[] temp = new UDDIPublication[publications.length + 1];
//        System.arraycopy(publications, 0, temp, 0, publications.length);
//        publications = temp;
//      }
//      UDDIPublication newPublication = new UDDIPublication();
//      newPublication.setInquiryURL(registry.getInquiryURL());
//      newPublication.setPublishURL(registry.getPublishURL());
//      newPublication.setServiceKey(uddiKey);
//      publications[publications.length - 1] = newPublication;
//
//      UDDIPublicationEvent uddiPublicationEvent = new UDDIPublicationEvent(webService.getWSIdentifier().getApplicationName(), webService.getWSIdentifier().getServiceName());
//      uddiPublicationEvent.setUddiPublications(publications);
//
//      WSContainer.getEventContext().getEventHandler().handle(uddiPublicationEvent);
//    } else {
//      throw new Exception("The specified web service cannot be found: serviceName = " + serviceName + ", applicationName = " + applicationName + ", jarName = " + jarName);
//    }
//  }
//
////  public WebServiceExt[] getAllWebServices() {
////    WSManagementInterface mngInt = WSManagementInterfaceImpl.getManagementInterface();
////    WSRuntimeDefinition[] webServices = mngInt.listWebServices();
////    WebServiceExt[] webServicesExt = new WebServiceExt[webServices.length];
////    for(int i = 0; i < webServices.length; i++) {
////      WSRuntimeDefinition def = webServices[i];
////      webServicesExt[i] = definitionToExternal(def);
////    }
////    return webServicesExt;
////  }
//  
//  private WebServiceExt definitionToExternal(WSRuntimeDefinition def) {
//    WebServiceExt ws = new WebServiceExt();    
//    ws.setNameURI(def.getWsQName().getNamespaceURI());
//    ws.setLocalName(def.getWsQName().getLocalPart());
//    UDDIPublication[] publications = def.getUddiPublications();
//    if (publications != null) {
//      UDDIPublicationExt[] publicationsExt = new UDDIPublicationExt[publications.length];
//      for (int i = 0; i < publications.length; i++) {
//        UDDIPublicationExt pubExt = new UDDIPublicationExt();
//        UDDIPublication pub = publications[i];
//        pubExt.setInquiryURL(pub.getInquiryURL());
//        pubExt.setPublishURL(pub.getPublishURL());
//        pubExt.setServiceKey(pub.getServiceKey());
//        publicationsExt[i] = pubExt;
//      }
//      ws.setUDDIPublication(publicationsExt); 
//    }
//    ws.setDescription(def.getWsdDocumentation());
//    ws.setWSDLStyles(def.getWsdlSupportedStyles());
//    ServiceEndpointDefinition[] endpoints = def.getServiceEndpointDefinitions();
//    String[] uris = new String[endpoints.length];
//    for (int j = 0; j < uris.length; j++) {
//      uris[j] = endpoints[j].getServiceEndpointId();
//    }
//    ws.setEndpointURIs(uris);
//    return ws;
//  }
//  
////  public WebServiceExt getWebService(String serviceName, String appName, String jarName) {
////    WSManagementInterface mngInt = WSManagementInterfaceImpl.getManagementInterface();
////    WSRuntimeDefinition[] webServices = mngInt.listWebServices();
////    for (int i = 0; i < webServices.length; i++) {
////      WSRuntimeDefinition def = webServices[i];
////      WSIdentifier wsID = def.getWSIdentifier();
////      if (wsID.getServiceName().equals(serviceName) && wsID.getApplicationName().equals(appName) && wsID.getJarName().equals(jarName)) {
////        return definitionToExternal(def);
////      }
////    }
////    return null;
////  }
//  
//  /* (non-Javadoc)
//   * @see com.sap.engine.interfaces.webservices.runtime.ServletsHelper#getProxyJars()
//   */
//  public String getProxyJars() {
//    return WSDeployer.getJarsPath();
//  }
  
  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.runtime.ServletsHelper#getHTTPProxyResolver()
   */
  public HTTPProxyResolver getHTTPProxyResolver() {
    return WSContainer.getHTTPProxyResolver();
  }
}
