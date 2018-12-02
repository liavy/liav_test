package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import javax.xml.transform.dom.DOMSource;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class VISchemasInfo {

  String viPath = null;
  String viPackage = null;
  String viName = null;
  DOMSource[] literalSchemas = null;
  DOMSource[] encodedSchemas = null;

  public VISchemasInfo() {
  }

  public String getViPath() {
    return viPath;
  }

  public void setViPath(String viPath) {
    this.viPath = viPath;
  }

  public String getViPackage() {
    return viPackage;
  }

  public void setViPackage(String viPackage) {
    this.viPackage = viPackage;
  }

  public String getViName() {
    return viName;
  }

  public void setViName(String viName) {
    this.viName = viName;
  }

  public DOMSource[] getLiteralSchemas() {
    return literalSchemas;
  }

  public void setLiteralSchemas(DOMSource[] literalSchemas) {
    this.literalSchemas = literalSchemas;
  }

  public DOMSource[] getEncodedSchemas() {
    return encodedSchemas;
  }

  public void setEncodedSchemas(DOMSource[] encodedSchemas) {
    this.encodedSchemas = encodedSchemas;
  }

}