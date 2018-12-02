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
import java.util.jar.JarFile;

import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfoCreatorImpl;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WebServicesSupportHandler
 * Description: WebServicesSupportHandlerImpl 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesSupportHandlerImpl implements WebServicesSupportHandler {
 
  private WebServicesSupportHandler[] webServicesSupportHandlers;   
  
  public WebServicesSupportHandlerImpl() throws Exception {  
    this.webServicesSupportHandlers = new WebServicesSupportHandler[]{new WebInfoCreatorImpl(), new WebServicesSupportHandler710Impl(), new WebServicesAltSupportHandler(), new JEEModuleGenerator()}; 
  }  
    
  public String[] getWebServicesEntry() {
    if(webServicesSupportHandlers == null || webServicesSupportHandlers.length == 0) {
      return new String[0];
    }
    
    String[] webServicesEntries = new String[0];
    String[] currentWebServicesEntries; 
    String[] newWebServicesEntries;
    for(int i = 0; i < webServicesSupportHandlers.length; i++) {
      currentWebServicesEntries = webServicesSupportHandlers[i].getWebServicesEntry(); 
      newWebServicesEntries = new String[webServicesEntries.length + currentWebServicesEntries.length];
      System.arraycopy(webServicesEntries, 0, newWebServicesEntries, 0, webServicesEntries.length);
      System.arraycopy(currentWebServicesEntries, 0, newWebServicesEntries, webServicesEntries.length, currentWebServicesEntries.length);
      webServicesEntries = newWebServicesEntries;  
    }
    
    return webServicesEntries; 
  }
  
  /** 
   * @param  archiveFiles - the archive files that are being deployed
   * @return              - an array of web module specific information   
   */
  public WebInfo[] generateWebSupport(String workingDir, File[] archiveFiles, ReadResult parsedAnnotations) throws Exception {
    if(archiveFiles == null || archiveFiles.length == 0) {
      return new WebInfo[0];
    }
   
    WebInfo[] webModuleInfoes = new WebInfo[0];
    for (int i = 0; i < archiveFiles.length; i++) {
      webModuleInfoes = mergeWebInfo(new WebInfo[][]{webModuleInfoes, generateWebSupport(workingDir, archiveFiles[i], parsedAnnotations)});       
    }
      
    return webModuleInfoes;
  }  
  
  public WebInfo[] generateWebSupport(String workingDir, File archiveFile, ReadResult parsedAnnotations) throws Exception {        
    JarFile archiveJarFile = null;
    
    WebInfo[] webModuleInfoes; 
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
    if(webServicesSupportHandlers == null || webServicesSupportHandlers.length == 0) {
      return new WebInfo[0];
    } 
    
    WebInfo[] webModuleInfoes = new WebInfo[0];
    for (int i = 0; i < webServicesSupportHandlers.length; i++) {
        webModuleInfoes = mergeWebInfo(new WebInfo[][]{webModuleInfoes, webServicesSupportHandlers[i].generateWebSupport(workingDir, archiveJarFile, parsedAnnotations)});
    }        
    
    return webModuleInfoes;
  }
  
  private WebInfo[] mergeWebInfo(WebInfo[][] webModuleInfoes) {
    if(webModuleInfoes == null || webModuleInfoes.length == 0) {
      return new WebInfo[0];
    }
    
    WebInfo[] webModuleInfoesAll = new WebInfo[0]; 
    WebInfo[] currentWebModuleInfoes; 
    WebInfo[] newWebModuleInfoes;
    for (int i = 0; i < webModuleInfoes.length; i++) {
      currentWebModuleInfoes = webModuleInfoes[i];
      if(currentWebModuleInfoes != null && currentWebModuleInfoes.length != 0) {      
        newWebModuleInfoes = new WebInfo[webModuleInfoesAll.length + currentWebModuleInfoes.length];
        System.arraycopy(webModuleInfoesAll, 0, newWebModuleInfoes, 0, webModuleInfoesAll.length);
        System.arraycopy(currentWebModuleInfoes, 0, newWebModuleInfoes, webModuleInfoesAll.length, currentWebModuleInfoes.length);
        webModuleInfoesAll = newWebModuleInfoes;
      }
    }
    
   return webModuleInfoesAll; 
  }
 
}
