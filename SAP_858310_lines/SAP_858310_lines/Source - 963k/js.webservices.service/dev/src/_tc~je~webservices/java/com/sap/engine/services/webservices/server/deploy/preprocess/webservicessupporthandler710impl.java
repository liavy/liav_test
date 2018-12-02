/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.webservices.server.deploy.preprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfoCreator;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WebServicesWebSupportHandler
 * Description: WebServicesWebSupportHandler provides web support for web services.  
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesSupportHandler710Impl extends WebServicesAbstractSupportHandler implements WebServicesSupportHandler, WebInfoCreator {
    
  public static final String WEBSERVICES_J2EE_ENGINE_ENTRY[] = new String[]{"META-INF/webservices-j2ee-engine.xml", "meta-inf/webservices-j2ee-engine.xml"};

  private static final String CONFIGURATION_FILE = "configuration-file";
       
  private static final String RT_CONFIG = "RTConfig";
  private static final String SERVICE = "Service";
  private static final String SERVICE_DATA = "ServiceData";  
  private static final String CONTEXT_ROOT = "contextRoot";; 
  private static final String BINDING_DATA = "BindingData";
  private static final String BINDING_DATA_URL = "url";  
     
  private JarUtils jarUtils; 
  
  public WebServicesSupportHandler710Impl() throws Exception {
    this.documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
    this.domSerializer = new DOMSerializer();
    this.loader = this.getClass().getClassLoader(); 
    this.jarUtils = new JarUtils();  
  }
  
  public String[] getWebServicesEntry() {
    return WEBSERVICES_J2EE_ENGINE_ENTRY;
  }  
  
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception { 
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new WebInfo[0];
    }
           
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    WebInfo[] currentWebModuleInfoes; 
    WebInfo[] newWebModuleInfoes;
    for(int i = 0; i < archiveFiles.length; i++) {
      currentWebModuleInfoes = generateWebSupport(workingDir, archiveFiles[i], parsedAnnotations);
      newWebModuleInfoes = new WebInfo[webModuleInfoes.length + currentWebModuleInfoes.length];
      System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
      System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
      webModuleInfoes = newWebModuleInfoes;       
    }
            
    return webModuleInfoes;       
  }

  public WebInfo[] generateWebSupport(String workingDir, File archiveFile, ReadResult parsedAnnotations) throws Exception {
    JarFile archiveJarFile = null;    
    WebInfo[] webModuleInfoes = new WebInfo[0];        
    
    try {
      archiveJarFile = new JarFile(archiveFile);
      webModuleInfoes = generateWebSupport(workingDir, archiveJarFile, parsedAnnotations);    
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e;
    } finally {
      if(archiveJarFile != null) {
        try {
          archiveJarFile.close();
        } catch(IOException e) {   
          // $JL-EXC$       
        } 
      }      
    }
    
    return webModuleInfoes;
  }
  
  public WebInfo[] generateWebSupport(String workingDir, JarFile archiveJarFile, ReadResult parsedAnnotations) throws Exception {
    WebInfo[] webModuleInfoes = new WebInfo[0];        
    
    try {     
      ZipEntry webServicesJ2EEEngineEntry; 
      InputStream webServicesJ2EEEngineDescriptorIn;  
      Element webServicesJ2EEEngineDescriptorElement;
      NodeList configurationFileElements; 
      JarEntry configurationFileEntry;      
      WebInfo[] currentWebModuleInfoes; 
      WebInfo[] newWebModuleInfoes; 
      for(int i = 0; i < WEBSERVICES_J2EE_ENGINE_ENTRY.length; i++) {
        webServicesJ2EEEngineEntry = archiveJarFile.getEntry(WEBSERVICES_J2EE_ENGINE_ENTRY[i]);
        if(webServicesJ2EEEngineEntry != null) {
          webServicesJ2EEEngineDescriptorIn = archiveJarFile.getInputStream(webServicesJ2EEEngineEntry);
          webServicesJ2EEEngineDescriptorElement = documentBuilder.parse(webServicesJ2EEEngineDescriptorIn).getDocumentElement();          
          configurationFileElements = webServicesJ2EEEngineDescriptorElement.getElementsByTagName(CONFIGURATION_FILE);
          if(configurationFileElements.getLength() != 1) {
            //TODO - throw exception as alternative
            continue; 
          } 
          configurationFileEntry = archiveJarFile.getJarEntry(configurationFileElements.item(i).getFirstChild().getNodeValue());
          if(configurationFileEntry == null) {
            //TODO - throw exception as alternative
            continue; 
          }
          currentWebModuleInfoes = generateWebSupport(workingDir, archiveJarFile.getInputStream(configurationFileEntry));
          newWebModuleInfoes = new WebInfo[webModuleInfoes.length + currentWebModuleInfoes.length];
          System.arraycopy(webModuleInfoes, 0, newWebModuleInfoes, 0, webModuleInfoes.length);
          System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoes.length, currentWebModuleInfoes.length);
          webModuleInfoes = newWebModuleInfoes;          
        }      
      }    
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e;
    }
    
    return webModuleInfoes; 
  }
    
  public WebInfo[] createSingleWebInfo(String workingDir, InputStream configurationDescriptorIn) throws Exception {
    return generateWebSupport(workingDir, documentBuilder.parse(configurationDescriptorIn));
  }
  
  public WebInfo[] createSingleWebInfo(String workingDir, String configurationDescriptorPath) throws Exception {
    WebInfo[] webModuleInfoes;
    FileInputStream configurationDescriptorIn = null;     
    try {
      configurationDescriptorIn = new FileInputStream(configurationDescriptorPath);
      webModuleInfoes = createSingleWebInfo(workingDir, configurationDescriptorIn);
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e; 
    } finally {
      if(configurationDescriptorIn != null) {
        try {
          configurationDescriptorIn.close(); 
        } catch(Exception e) {
          // $JL-EXC$          
        }       
      }
    }
    
    return webModuleInfoes; 
  }

  public WebInfo[] createWebInfo(String workingDir, String[] wsDeploymentDescriptorPaths) throws Exception {
    return new WebInfo[0];
  }
     
  public WebInfo[] generateWebSupport(String workingDir, InputStream configurationDescriptorIn) throws Exception {   
    return generateWebSupport(workingDir, documentBuilder.parse(configurationDescriptorIn));  
  }
     
  public WebInfo[] generateWebSupport(String workingDir, Document configurationDescriptor) throws Exception {         
    Element configurationRootElement = configurationDescriptor.getDocumentElement();
    NodeList rtConfigElements = configurationRootElement.getElementsByTagName(RT_CONFIG);
    
    if(rtConfigElements == null || rtConfigElements.getLength() == 0) {
      return new WebInfo[0];
    }   
    
    Element rtConfig = (Element)rtConfigElements.item(0);
    NodeList serviceElements = rtConfig.getElementsByTagName(SERVICE);
    if(serviceElements == null || serviceElements.getLength() == 0) {
      return new WebInfo[0];
    } 
      
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    WebInfo[] currentWebInfoes; 
    WebInfo[] newWebInfoes; 
    for(int i = 0; i < serviceElements.getLength(); i++) {      
      try {
        Element serviceElement = (Element)serviceElements.item(i);      
        currentWebInfoes = generateWebSupportPerService(workingDir,  serviceElement);
        newWebInfoes = new WebInfo[webModuleInfoes.length + currentWebInfoes.length];
        System.arraycopy(webModuleInfoes, 0, newWebInfoes, 0, webModuleInfoes.length);
        System.arraycopy(currentWebInfoes, 0, newWebInfoes, webModuleInfoes.length, currentWebInfoes.length);
        webModuleInfoes = newWebInfoes;
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      }       
    }   
        
    return webModuleInfoes;
  }
  
  private WebInfo[] generateWebSupportPerService(String workingDir, Element serviceElement) throws Exception { 
    NodeList serviceDataElements = serviceElement.getElementsByTagName(SERVICE_DATA);
    if(serviceDataElements == null || serviceDataElements.getLength() == 0) {      
      //TODO - throw exception as alternative  
      return new WebInfo[0];
    }
    
    WebInfo[] webModuleInfos = new WebInfo[0];
    String serviceName = serviceElement.getAttribute(NAME);
    Element serviceDataElement = (Element)serviceDataElements.item(0);        
    String serviceContextRoot = serviceDataElement.getAttribute(CONTEXT_ROOT);          
    NodeList bindingDataElements = serviceDataElement.getElementsByTagName(BINDING_DATA);
    //TODO - check
    if(serviceContextRoot.equals("")) {      
      try {      
        webModuleInfos = generateWebSupportPerService(workingDir, serviceName, serviceContextRoot, bindingDataElements, true);        
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      } 
    } else {
      try {      
        webModuleInfos = generateWebSupportPerService(workingDir, serviceName, serviceContextRoot, bindingDataElements, false);
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      }
    }
       
    return webModuleInfos;
  }
    
  private WebInfo[] generateWebSupportPerService(String workingDir, String serviceName, String serviceContextRoot, NodeList bindingDataElements, boolean isSingleMode) throws Exception {
    WebInfo[] webModuleInfoes = new WebInfo[0]; 
    
    if(isSingleMode) {
      try {      
        webModuleInfoes = generateWebSupportPerServiceSingleMode(workingDir, serviceName, bindingDataElements);
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      }      
    } else {
      try {     
        WebInfo webModuleInfo = generateWebSupportPerServiceMultipleMode(workingDir, serviceName, serviceContextRoot, bindingDataElements);
        webModuleInfoes = new WebInfo[]{webModuleInfo};
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      }
    }
    
    return webModuleInfoes;    
  }  
  
  private WebInfo generateWebSupportPerServiceMultipleMode(String workingDir, String serviceName, String serviceContextRoot, NodeList bindingDataElements) throws Exception {    
    String serviceWorkingDir = workingDir + "/" + serviceName;
    String warFilePath = serviceWorkingDir + "/" + serviceName + ".war";    
    try {    
      generateWar(serviceWorkingDir , warFilePath, createWebDescriptorMultipleMode(serviceName, bindingDataElements), false);
    } catch(Exception e) {
      //TODO 
      e.printStackTrace();
      throw e; 
    } 
    
    return new WebInfo(serviceContextRoot, warFilePath);      
  }
  
  private WebInfo[] generateWebSupportPerServiceSingleMode(String workingDir, String serviceName, NodeList bindingDataElements) throws Exception {
    if(bindingDataElements == null || bindingDataElements.getLength() == 0) {
      return new WebInfo[0];  
    }
    
    WebInfo[] webModuleInfoes = new WebInfo[bindingDataElements.getLength()];
    Element bindingDataElement;
    String bindingDataName; 
    String bindingDataUrl; 
    String bindingDataWorkingDir; 
    String warFilePath; 
    for(int i = 0; i < bindingDataElements.getLength(); i++) {
      bindingDataElement = (Element)bindingDataElements.item(i);
      bindingDataName = bindingDataElement.getAttribute(NAME);
      bindingDataUrl = bindingDataElement.getAttribute(BINDING_DATA_URL); 
      bindingDataWorkingDir = workingDir + "/" + serviceName + "_" + bindingDataName;
      warFilePath = bindingDataWorkingDir + "/" + serviceName + "_" + bindingDataName + ".war";
      
      try {      
        generateWar(bindingDataWorkingDir, warFilePath, createWebDescriptorSingleMode(serviceName, bindingDataElement), false);      
      } catch(Exception e) {
        //TODO 
        e.printStackTrace();
        throw e; 
      }
      webModuleInfoes[i] = new WebInfo(bindingDataUrl, warFilePath);      
    }
    
    return webModuleInfoes;
 }  
      
  private Document[] createWebDescriptorsSingleMode(String serviceName, NodeList bindingDataElements) {
    if(bindingDataElements == null || bindingDataElements.getLength() == 0) {
      return new Document[0];
    }
    
    Document[] webDescriptorDocuments = new Document[bindingDataElements.getLength()];        
    for(int i = 0; i < bindingDataElements.getLength(); i++) {      
      webDescriptorDocuments[i] = createWebDescriptorSingleMode(serviceName, (Element)bindingDataElements.item(i));       
    }    
    
    return webDescriptorDocuments;
  }    
    
  private Document createWebDescriptorSingleMode(String serviceName, Element bindingDataElement) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementSingleMode(webDescriptorDocument, serviceName, bindingDataElement));

    return webDescriptorDocument;
  }
  
  private Element createWebAppElementSingleMode(Document webDescriptorDocument, String serviceName, Element bindingDataElement) {
    Element webAppElement = webDescriptorDocument.createElement(WEB_APP);    
    
    String bindingDataName = bindingDataElement.getAttribute(NAME);   
    String servletName = serviceName + "_" + bindingDataName; 
    
    webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, servletName));    
    webAppElement.appendChild(createServletElement(webDescriptorDocument, servletName, servletName, SOAP_SERVLET_CLASS_NAME, LOAD_ON_STARTUP_VALUE));
    webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, servletName, "/*"));      
    webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument, SESSION_TIMEOUT_VALUE));
    return webAppElement;
  }    
    
  private Document createWebDescriptorMultipleMode(String serviceName, NodeList bindingDataElements) {
    Document webDescriptorDocument = documentBuilder.newDocument(); 
    webDescriptorDocument.appendChild(createWebAppElementMulipleMode(webDescriptorDocument, serviceName, bindingDataElements));
    
    return webDescriptorDocument;
  }     
  
  private Element createWebAppElementMulipleMode(Document webDescriptorDocument, String serviceName, NodeList bindingDataElements) {           
    Element webAppElement = webDescriptorDocument.createElement(WEB_APP);    
    
    webAppElement.appendChild(createTextChildElement(webDescriptorDocument, DISPLAY_NAME, serviceName));    
    setServletElements(webDescriptorDocument, webAppElement, bindingDataElements);
    setServletMappingElements(webDescriptorDocument, webAppElement, bindingDataElements);   
    webAppElement.appendChild(createSessionConfigElement(webDescriptorDocument, SESSION_TIMEOUT_VALUE));
    return webAppElement;
  }  
 
  private void setServletElements(Document webDescriptorDocument, Element webAppElement, NodeList bindingDataElements) {       
    if(bindingDataElements == null || bindingDataElements.getLength() == 0) {
      return;
    }
    
    Element bindingDataElement;
    String bindingDataName; 
    for(int i = 0; i < bindingDataElements.getLength(); i++) {
      bindingDataElement = (Element)bindingDataElements.item(i);
      bindingDataName = bindingDataElement.getAttribute(NAME);
      webAppElement.appendChild(createServletElement(webDescriptorDocument, bindingDataName, bindingDataName, SOAP_SERVLET_CLASS_NAME, 0));
    }    
  }
  
  private void setServletMappingElements(Document webDescriptorDocument, Element webAppElement, NodeList bindingDataElements) {         
    if(bindingDataElements == null || bindingDataElements.getLength() == 0) {
      return;
    }
  
    Element bindingDataElement;
    String bindingDataName; 
    String bindingDataUrl; 
    for(int i = 0; i < bindingDataElements.getLength(); i++) {
      bindingDataElement = (Element)bindingDataElements.item(i);
      bindingDataName = bindingDataElement.getAttribute(NAME);
      bindingDataUrl = bindingDataElement.getAttribute(BINDING_DATA_URL);
      webAppElement.appendChild(createServletMappingElement(webDescriptorDocument, bindingDataName, bindingDataUrl));
    }    
  } 
  
}
