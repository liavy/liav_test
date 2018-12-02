package com.sap.engine.services.webservices.runtime.definition;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class OutsideInDefinition {

  private String wsdlRelPath = null;
  private String javaQNameMappingFile = null;

  public OutsideInDefinition() {
  }

  public String getWsdlRelPath() {
    return wsdlRelPath;
  }

  public void setWsdlRelPath(String wsdlRelPath) {
    this.wsdlRelPath = wsdlRelPath;
  }

  public String getJavaQNameMappingFile() {
    return javaQNameMappingFile;
  }

  public void setJavaQNameMappingFile(String javaQNameMappingFile) {
    this.javaQNameMappingFile = javaQNameMappingFile;
  }

}
