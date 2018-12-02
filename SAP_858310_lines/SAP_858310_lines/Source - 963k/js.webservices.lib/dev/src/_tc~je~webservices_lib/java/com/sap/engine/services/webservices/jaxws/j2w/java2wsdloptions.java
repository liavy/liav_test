/*
 * Copyright (c) 2004 by SAP AG, Walldorf., http://www.sap.com All rights
 * reserved. This software is the confidential and proprietary information of
 * SAP AG, Walldorf. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you
 * entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */

public class Java2WsdlOptions {

  private String      SEIClass;
  private String      serviceName;
  private String      portName;
  private String      genSourceDir;
  private String      genClassDir;
  private String      genWSDLDir;
  private ClassLoader classLoader;
  private String      serverClassPath;
  boolean hasWSDL = false;

  public Java2WsdlOptions(String SEIClass, String genSourceDir, String genClassDir) {
    if (SEIClass == null || genSourceDir == null || genClassDir == null) {
      throw new IllegalArgumentException("Must supply a SEI class, a dir where to plass generated sources and a dir where to place generated classes!");

    }     

    this.SEIClass = SEIClass;   
    this.genSourceDir = genSourceDir;
    this.genClassDir = genClassDir;
    
  }
  
  public boolean isHasWSDL() {
    return hasWSDL;
  }



  public void setHasWSDL(boolean hasWSDL) {
    this.hasWSDL = hasWSDL;
  }



  /**
   * @return Returns the classLoader.
   */
  public ClassLoader getClassLoader() {
    if (this.classLoader == null) {
      return Thread.currentThread().getContextClassLoader();
    } else {
      return this.classLoader;
    }
  }


  /**
   * @param classLoader The classLoader to set.
   */
  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }


  /**
   * @return Returns the genWSDLDir.
   */
  public String getGenWSDLDir() {
    return genWSDLDir;
  }


  /**
   * @param genWSDLDir The genWSDLDir to set.
   */
  public void setGenWSDLDir(String genWSDLDir) {
    this.genWSDLDir = genWSDLDir;
  }


  /**
   * @return Returns the portName.
   */
  public String getPortName() {
    return portName;
  }


  /**
   * @param portName The portName to set.
   */
  public void setPortName(String portName) {
    this.portName = portName;
  }


  /**
   * @return Returns the serviceName.
   */
  public String getServiceName() {
    return serviceName;
  }


  /**
   * @param serviceName The serviceName to set.
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }


  /**
   * @return Returns the genClassDir.
   */
  public String getGenClassDir() {
    return genClassDir;
  }


  /**
   * @return Returns the genSourceDir.
   */
  public String getGenSourceDir() {
    return genSourceDir;
  }


  /**
   * @return Returns the sEIClass.
   */
  public String getSEIClass() {
    return SEIClass;
  }

  /**
   * @return Returns the serverClassPath.
   */
  public String getServerClassPath() {
    return serverClassPath;
  }

  /**
   * @param serverClassPath The serverClassPath to set.
   */
  public void setServerClassPath(String serverClassPath) {
    this.serverClassPath = serverClassPath;
  }


}
