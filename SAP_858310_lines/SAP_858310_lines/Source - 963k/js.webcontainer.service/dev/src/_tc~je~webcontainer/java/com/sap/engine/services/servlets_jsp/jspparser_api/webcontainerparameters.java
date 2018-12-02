/*
 * Copyright (c) 2002-2006 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

/**
 * 
 * Holds some of the web container parameters and some other service parameters like server ID and service classloader.
 * @author Todor Mollov, Bojidar Kadrev 
 * DEV_tc_je_webcontainer
 * 2005-4-22
 * 
 */
public class WebContainerParameters {
  
	private boolean internalCompiler = false;
	
	private  String externalCompiler = "javac";
	
	private  boolean jspDebugSupport = true;
	
	private  int serverID = 0;
	
	private ClassLoader serviceClassLoader;
	
	private String javaEncoding = "UTF-8";
	
	private boolean extendedJspImports = false;
	
	private boolean productionMode = false;	

  public boolean isJspDebugSupport() {
    return jspDebugSupport;
  }
  public void setJspDebugSupport(boolean eclipseSupport) {
    this.jspDebugSupport = eclipseSupport;
  }
  public String getExternalCompiler() {
    return externalCompiler;
  }
  public void setExternalCompiler(String externalCompiler) {
    this.externalCompiler = externalCompiler;
  }
  public boolean isInternalCompiler() {
    return internalCompiler;
  }
  public void setInternalCompiler(boolean internalCompiler) {
    this.internalCompiler = internalCompiler;
  }
  public int getServerID() {
    return serverID;
  }
  
  /**
   * the ID of this server node
   * @param serverID
   */
  public void setServerID(int serverID) {
    this.serverID = serverID;
  }
  
  /**
   * The classloder of the web container service 
   * @return
   */
  public ClassLoader getServiceClassLoader() {
    return serviceClassLoader;
  }

  public void setServiceClassLoader(ClassLoader serviceClassLoader) {
    this.serviceClassLoader = serviceClassLoader;
  }

  /**
   * The java file encoding for generated java files for JSPs
   * @return String with encoding value
   */
  public String getJavaEncoding() {
    return javaEncoding;
  }

  public void setJavaEncoding(String javaEncoding) {
    this.javaEncoding = javaEncoding;
  }
  
  /**
   * Property of web container service that indicates if JSP 2.1 imports will be generated in the java file for the processed JSP.
   * @deprecated - property does not exist by default.
   * @return - false - old imports, true - limited imports (JSP 2.1). The default value is true. 
   */
  public boolean isExtendedJspImports() {
    if( extendedJspImports ){
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(Location.getLocation(this.getClass()), "ASJ.web.000284", 
        "Extended imports should not be used.", null, null);
    }
    return extendedJspImports;
  }
  
  /**
   * Set the value for the property of web container service that indicates if JSP 2.1 imports will be generated in the java file for the processed JSP.
   * @deprecated - property does not exist by default.
   * @param extendedJspImports
   */
  public void setExtendedJspImports(boolean extendedJspImports) {
    this.extendedJspImports = extendedJspImports;
  }
  
  /**
   * Production mode concerns JSP preocessing. When a class file is already compiled and production mode is true, 
   * then no check if the JSP file is modified is performed.  
   * @return - 
   */
  public boolean isProductionMode() {
    return productionMode;
  }
  
  /**
   * Default value is false.
   * @param mode - if true, no checks will be performed.
   */
  public void setProductionMode(boolean mode){
    this.productionMode = mode;
  }
}
