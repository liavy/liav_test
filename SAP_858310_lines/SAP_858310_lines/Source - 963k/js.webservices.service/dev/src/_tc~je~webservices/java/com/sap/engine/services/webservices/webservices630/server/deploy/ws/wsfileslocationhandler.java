package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import java.io.InputStream;
import java.io.IOException;

/**
 * Title: WSFilesLocationHandler
 * Description: The interface specifies methods, that provides access to web services deploy files.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WSFilesLocationHandler {  

  public String getWSDeploymentDescriptorPath();

  public InputStream getWSDeploymentDescriptorInputStream() throws IOException;

  public String getWSDeploymentDescriptorLocationMsg();

  public String getViPath(String viRelPath);

  public InputStream getViInputStream(String viRelPath) throws IOException;

  public String getViLocationMsg(String viRelPath);

  //public InputStream getViInputStream(String viRelPath, WSDeploymentInfo wsDeploymentInfo) throws IOException;

  public String getWsdPath(String wsdRelPath);

  public InputStream getWsdInputStream(String wsdRelPath) throws IOException;

  public String getWsdLocationMsg(String wsdRelPath);

  //public InputStream getWsdInputStream(String wsdRelPath, WSDeploymentInfo wsDeploymentInfo) throws IOException;

  public String findDocRelPath(String[] templates) throws IOException;

  public String getDocPath(String docRelPath);

  public InputStream getDocInputStream(String docRelPath) throws IOException;

  public String getDocLocationMsg(String docRelPath);

  //public InputStream getDocInputStream(String docrelPath, WSDeploymentInfo wsDeploymentInfo) throws IOException;

  public String getOutsideInWsdlPath(String wsdlRelPath) throws IOException;

  public InputStream getOutsideInWsdlInputStream(String wsdlRelPath) throws IOException;

  public String getOutsideInWsdlLocationMsg(String wsdlrelPath);

  //public InputStream getOutsideInWsdlInputStream(String wsdlRelPath, WSDeploymentInfo wsDeploymentInfo) throws IOException;

  public String getOutsideInJavaToQNameMapppingPath(String javaToQNameMappingRelPath) throws IOException;

  public InputStream getOutsideInJavaToQNameMappingStream(String javaToQNameMappingRelPath) throws IOException;

  public String getOutsideInJavaToQNameMappingLocationMsg(String javaToQNameMappingRelPath);

  //public InputStream getOutsideInJavaToQNameMappingStream(String javaToQNameMappingRelPath, WSDeploymentInfo wsDeploymentInfo) throws IOException;

  public String getLocationMsg();

}
