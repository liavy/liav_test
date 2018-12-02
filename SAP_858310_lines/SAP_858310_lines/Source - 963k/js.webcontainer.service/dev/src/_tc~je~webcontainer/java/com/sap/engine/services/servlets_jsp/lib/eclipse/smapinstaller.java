/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.lib.eclipse;

import java.io.File;

import com.sap.engine.library.bytecode.cf.CFFactory;
import com.sap.engine.library.bytecode.cf.CFParser;
import com.sap.engine.library.bytecode.cf.CFSerializer;
import com.sap.engine.library.bytecode.cf.ClassFile;
import com.sap.engine.library.bytecode.cf.SourceDebugExtensionAttribute;
import com.sap.engine.services.servlets_jsp.server.LogContext;

/**
 * The <code>SmapInstaller</code> class is used for injecting the content of
 * the SMAP into the SourceDebugExtension attribute of the generated class file.
 * 
 * @author Diyan Yordanov
 */

public class SmapInstaller {
	
  /**
   * Name of the class file to be injected.
   */
  private String classFileName;
  
  /**
   * Creates new <code>SmapInstaller</code> object to perform the injection of 
   * SourceDebugExtension attribute into the specified class.
   * @param classFileName name of the class to be modified.
   */
  public SmapInstaller(String classFileName) {
    this.classFileName = classFileName;
  }
  
  /**
   * Injects the SMAP as a value of the SourceDebugExtension attribute.
   * @param smap a string representation of the SMAP.
   */
  public void injectSmap(String smap) {
    long start = 0;
    long end =0;
    if (LogContext.getLocationJspParser().beDebug()) {
      start = System.nanoTime();      
    }
    try {
      CFFactory factory = CFFactory.getThreadLocalInstance();
      CFParser parser = factory.createCFParser();
      ClassFile cf = parser.parse(classFileName, null);
      SourceDebugExtensionAttribute attr = new SourceDebugExtensionAttribute(cf, smap);
      cf.addAttribute(attr);
      CFSerializer ser = factory.createCFSerializer();
      ser.serialize(new File(classFileName), cf, null);
    } catch (Exception e) {
      LogContext.getLocation(LogContext.LOCATION_JSP_PARSER).
        traceError("ASJ.web.000412","The byte code modification for class[{0}] throw exception." , new Object[]{classFileName}, e, null, null);
    }
    if (LogContext.getLocationJspParser().beDebug()) {
      end = System.nanoTime();
      LogContext.getLocationJspParser().debugT("classFileName:["+classFileName+"] injected for " + (end-start)/1000000+" milliseconds.");
    }
  }

}
