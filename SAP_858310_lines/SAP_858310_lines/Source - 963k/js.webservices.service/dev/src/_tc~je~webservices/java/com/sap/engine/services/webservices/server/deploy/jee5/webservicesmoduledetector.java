package com.sap.engine.services.webservices.server.deploy.jee5;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.sap.engine.services.deploy.container.rtgen.AnnotationsSupportingModuleDetector;
import com.sap.engine.services.deploy.container.rtgen.GenerationException;
import com.sap.engine.services.deploy.ear.Module;
import com.sap.engine.services.webservices.espbase.mappings.EJBImplementationLink;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.lib.javalang.annotation.AnnotationRecord;
import com.sap.lib.javalang.annotation.impl.AnnotationNamedMember;
import com.sap.lib.javalang.annotation.impl.AnnotationRecordImpl;
import com.sap.lib.javalang.element.ClassInfo;
import com.sap.lib.javalang.element.FieldInfo;
import com.sap.lib.javalang.element.impl.ClassInfoImpl;
import com.sap.lib.javalang.file.FileInfo;
import com.sap.lib.javalang.tool.ReadResult;

/**
 * Title: WebServicesModuleDetector
 * Description: The class manages ReadResult object.
 * Copyright: Copyright (c) 2006
 * Company: Sap Labs Sofia
 * @author Krasimir Atanasov
 * @version 7.10
 */

public class WebServicesModuleDetector extends AnnotationsSupportingModuleDetector {
	  
  private static final long serialVersionUID = 1L;
  //public  static Hashtable  resultAnnotations = null;
  //public static ReadResult parsedAnnotations;
    
  public Module detectModule(File tempDir, String moduleRelativeFileUri, ReadResult parsedAnnotations) throws GenerationException{
    //WebServicesModuleDetector.parsedAnnotations = parsedAnnotations;       
		
//	if (WebServicesModuleDetector.resultAnnotations == null) {
//      WebServicesModuleDetector.resultAnnotations = new Hashtable();
//    }
//        
//    if(parsedAnnotations != null) {
//      WebServicesModuleDetector.resultAnnotations.put(moduleRelativeFileUri, parsedAnnotations);
//    }
    
	return new Module(tempDir, moduleRelativeFileUri, WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME);		
  }
  
}
