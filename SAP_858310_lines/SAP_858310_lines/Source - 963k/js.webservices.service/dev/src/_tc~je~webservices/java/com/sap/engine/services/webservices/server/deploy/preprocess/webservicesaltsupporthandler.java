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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.sap.engine.lib.xml.util.DOMSerializer;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebServicesJ2EEEngineAltFactory;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.PortComponentType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebserviceDescriptionType;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.alt.WebservicesType;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.guid.GUID;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WebServicesAltSupportHandler
 * Description: WebServicesAltSupportHandler 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesAltSupportHandler extends WebServicesAbstractSupportHandler implements WebServicesSupportHandler {
  
  public static final String[] WEBSERVICES_J2EE_ENGINE_ALT_DESCRIPTOR_ENTRY_NAMES = new String[]{"META-INF/webservices-j2ee-engine-alt.xml"};
  
  public WebServicesAltSupportHandler() throws ParserConfigurationException {
    this(DocumentBuilderFactory.newInstance().newDocumentBuilder(), new DOMSerializer(), WebServicesAltSupportHandler.class.getClassLoader()); 	  
  }
  
  public WebServicesAltSupportHandler(DocumentBuilder documentBuilder, DOMSerializer domSerializer, ClassLoader loader) {
    super(documentBuilder, domSerializer, loader);  
  }
  
  public String[] getWebServicesEntry() {
	return WEBSERVICES_J2EE_ENGINE_ALT_DESCRIPTOR_ENTRY_NAMES;		
  }  
	
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception {
    return null;
  }
  
  public WebInfo[] generateWebSupport(String workingDir, JarFile archiveJarFile, ReadResult parsedAnnotations) throws Exception {  
	JarEntry webServicesJ2EEEngineAltDescriptorEntry;
	InputStream webServicesJ2EEEngineAltDescriptorIn = null; 
	WebInfo[] webInfoes = null; 
	try { 
      for(String webServicesJ2EEEngineAltDescriptorEntryName: WEBSERVICES_J2EE_ENGINE_ALT_DESCRIPTOR_ENTRY_NAMES) {
	    webServicesJ2EEEngineAltDescriptorEntry = archiveJarFile.getJarEntry(webServicesJ2EEEngineAltDescriptorEntryName); 
	    if(webServicesJ2EEEngineAltDescriptorEntry != null) {
	      webServicesJ2EEEngineAltDescriptorIn = archiveJarFile.getInputStream(webServicesJ2EEEngineAltDescriptorEntry);
	      WebservicesType webServicesJ2EEEngineDescriptor = WebServicesJ2EEEngineAltFactory.load(webServicesJ2EEEngineAltDescriptorIn);
	      webInfoes = generateWebModules(workingDir, webServicesJ2EEEngineDescriptor.getWebserviceDescription());
	      break; 	  
	    }
	  }
	} finally {
	  try {
	    if(webServicesJ2EEEngineAltDescriptorIn != null) {
	      webServicesJ2EEEngineAltDescriptorIn.close(); 	
	    }  	  
	  }	catch(IOException e) {
        // $JL-EXC$	  
	  }
	}
	
	return webInfoes; 
  }
  
  private WebInfo[] generateWebModules(String workingDir, WebserviceDescriptionType[] serviceAltDescriptors) throws Exception {
    if(serviceAltDescriptors == null || serviceAltDescriptors.length == 0) {
      return new WebInfo[0];	
    }
    
    ArrayList<WebInfo> webInfoes = new ArrayList<WebInfo>(); 
    for(WebserviceDescriptionType serviceAltDescriptor: serviceAltDescriptors) {
      generateWebModules(workingDir, serviceAltDescriptor.getWebserviceName(), serviceAltDescriptor.getPortComponent(), webInfoes);  	
    }
    
    return webInfoes.toArray(new WebInfo[webInfoes.size()]);     
  }  
  
  private void generateWebModules(String workingDir, String serviceName, PortComponentType[] portComponents, ArrayList<WebInfo> webInfoes) throws Exception {
    if(portComponents == null || portComponents.length == 0) {
      return; 	
    }  
    
    for(PortComponentType portComponent: portComponents) {
      webInfoes.add(generateWebModule(workingDir, serviceName, portComponent));	
    }    
  } 
  
  private WebInfo generateWebModule(String workingDir, String serviceName, PortComponentType portComponent) throws Exception {
    String servletName = serviceName + "_" +  portComponent.getPortName();
       
    Document webDescriptor = createWebDescriptorSingleMode(servletName, servletName, servletName);     
    String portComponentWorkingDir = workingDir + "/" + System.currentTimeMillis();     
    String warFilePath = workingDir + "/ws" + new GUID().toHexString() + ".war";     
    generateWar(portComponentWorkingDir, warFilePath, webDescriptor, domSerializer, loader, false);
    return new WebInfo(portComponent.getUrl(), warFilePath); 
  }   
  
}