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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.services.deploy.container.rtgen.GenerationException;
import com.sap.engine.services.deploy.ear.J2EEModule;
import com.sap.engine.services.deploy.ear.Module;
import com.sap.engine.services.deploy.ear.modules.Web;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfo;
import com.sap.engine.services.webservices.server.deploy.preprocess.preprocess630.WebInfoCreatorImpl;
import com.sap.engine.services.deploy.container.rtgen.AnnotationsSupportingGenerator;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WSDeployProcess
 * Description: WSDeployProcess 
 * 
 * @author Luchesar Cekov
 * @author Aneta Angova
 * @version
 */

public class WebServicesGenerator extends AnnotationsSupportingGenerator {
  
  private Set<String> modulesToRemove;
  
  public WebServicesGenerator() {
    	  
  }
 
  public Module[] generate(File tempDir, String moduleRelativeFileUri, ReadResult parsedAnnotations) throws GenerationException {
    ArrayList result = new ArrayList(1);
    try {
      //File file = new File(tempDir + File.separator + moduleRelativeFileUri);
      createSingleModule(tempDir, moduleRelativeFileUri, result, parsedAnnotations);      
    } catch (GenerationException rtge) {
      throw rtge;
    } catch (Exception e) {
      throw new GenerationException(e.getMessage(), e);
    }

    return (Module[])result.toArray(new Module[] {});
  }
  
  private void createSingleModule(File tempDir, String moduleRelativeFileUri, ArrayList result, ReadResult parsedAnnotations) throws GenerationException {
	File entry = new File(tempDir, moduleRelativeFileUri); 
	String tempCreatorDir = new File(tempDir, "webService" + System.currentTimeMillis()).getAbsolutePath();
    try {
      WebServicesSupportHandlerImpl creator = new WebServicesSupportHandlerImpl();
      WebInfo[] webInfo = creator.generateWebSupport(tempCreatorDir, entry, parsedAnnotations);
      
      if (entry.getName().endsWith(".wsar")) {
        ZipEntry webServices630Entry; 
        JarFile archiveJarFile = new JarFile(entry);     
        try {
          for(int i = 0; i < WebInfoCreatorImpl.WEBSERVICES_630_ENTRY.length; i++) {
            webServices630Entry = archiveJarFile.getEntry(WebInfoCreatorImpl.WEBSERVICES_630_ENTRY[i]);
            if(webServices630Entry != null) {
              getModulesToRemove().add(moduleRelativeFileUri);
            }
          }
        } finally {
          archiveJarFile.close();
        }
      }      
      
      File warFile = null;
      if(webInfo != null) {
        for (int i = 0; i < webInfo.length; i++) {
          warFile = new File(webInfo[i].getWarModulePath());
          File resultFile = new File(tempDir, warFile.getName());
          if (!resultFile.exists()) {
            FileUtils.copyFile(warFile, resultFile);          
          }
          J2EEModule webModule = new Web(tempDir, (new File(webInfo[i].getWarModulePath())).getName(), webInfo[i].getContextRoot());
          result.add(webModule);
          result.add(new Module(tempDir, (new File(webInfo[i].getWarModulePath())).getName(), WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME)); 
        }
      }
    } catch (Exception e) {
      throw new GenerationException(e.getMessage(), e);
    } finally {
      FileUtils.deleteDirectory(new File(tempCreatorDir));
    }
  }

  public boolean supportsFile(String moduleRelativeFileUri) {
    return moduleRelativeFileUri.endsWith(".jar") || moduleRelativeFileUri.endsWith(".war") || moduleRelativeFileUri.endsWith(".par") || moduleRelativeFileUri.endsWith(".wsar");
  }
  
  public boolean removeModule(String moduleRelativeFileUri){
    return getModulesToRemove().remove(moduleRelativeFileUri);	  
  }
  
  private synchronized Set<String> getModulesToRemove() {
    if(modulesToRemove == null) {
      modulesToRemove = new HashSet<String>(); 	
    }  
	
    return modulesToRemove; 	  
  }
  
}
