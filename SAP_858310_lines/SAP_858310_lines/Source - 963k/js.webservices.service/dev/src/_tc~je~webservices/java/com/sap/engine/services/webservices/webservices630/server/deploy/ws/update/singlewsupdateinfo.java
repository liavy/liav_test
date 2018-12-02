package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.ExtArchiveLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.ExtFileLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSArchiveLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSFileLocationWrapper;

/**
 * Title: SingleWSUpdateInfo
 * Description: The class is a container for single web service update status.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class SingleWSUpdateInfo {

  public static final int INSIDE_OUT_MODE           = 0;
  public static final int OUTSIDE_IN_MODE           = 1;
  public static final int INSIDE_0UT_MIGRATION_MODE = 2;
  public static final int OUTSIDE_IN_MIGRATIONT_MODE = 3;

  public static final int NOT_CHANGED_STATUS = 0;
  public static final int CHANGED_STATUS     = 1;
  public static final int ADDED_STATUS       = 2;
  public static final int DELETED_STATUS     = 3;

  private int mode = INSIDE_OUT_MODE;
  private int visStatus = NOT_CHANGED_STATUS;
  private int wsdStatus = NOT_CHANGED_STATUS;
  private int docStatus = NOT_CHANGED_STATUS;
  private int wsDeploymentDescriptorStatus = NOT_CHANGED_STATUS;
  private int wsdlStatus = NOT_CHANGED_STATUS;
  private int javaToQNameMappingStatus = NOT_CHANGED_STATUS;

  private ExtArchiveLocationWrapper extArchiveLocationWrapper = null;
  private ExtFileLocationWrapper extFileLocationWrapper = null;

  public SingleWSUpdateInfo() {
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int mode) {
    this.mode = mode;
  }

  public int getVisStatus() {
    return visStatus;
  }

  public void setVisStatus(int visStatus) {
    this.visStatus = visStatus;
  }

  public int getWsdStatus() {
    return wsdStatus;
  }

  public void setWsdStatus(int wsdStatus) {
    this.wsdStatus = wsdStatus;
  }

  public int getDocStatus() {
    return docStatus;
  }

  public void setDocStatus(int docStatus) {
    this.docStatus = docStatus;
  }

  public int getWsDeploymentDescriptorStatus() {
    return wsDeploymentDescriptorStatus;
  }

  public void setWsDeploymentDescriptorStatus(int wsDeploymentDescriptorStatus) {
    this.wsDeploymentDescriptorStatus = wsDeploymentDescriptorStatus;
  }

  public int getWsdlStatus() {
    return wsdlStatus;
  }

  public void setWsdlStatus(int wsdlStatus) {
    this.wsdlStatus = wsdlStatus;
  }

  public int getJavaToQNameMappingStatus() {
    return javaToQNameMappingStatus;
  }

  public void setJavaToQNameMappingStatus(int javaToQNameMappingStatus) {
    this.javaToQNameMappingStatus = javaToQNameMappingStatus;
  }

  public ExtArchiveLocationWrapper getExtArchiveLocationWrapper() {
    return extArchiveLocationWrapper;
  }

  public void setExtArchiveLocationWrapper(ExtArchiveLocationWrapper extArchiveLocationWrapper) {
    this.extArchiveLocationWrapper = extArchiveLocationWrapper;
  }

  public ExtFileLocationWrapper getExtFileLocationWrapper() {
    return extFileLocationWrapper;
  }

  public void setExtFileLocationWrapper(ExtFileLocationWrapper extFileLocationWrapper) {
    this.extFileLocationWrapper = extFileLocationWrapper;
  }

  public boolean isWSChanged() {
    if(visStatus != NOT_CHANGED_STATUS) {
      return true;
    }
    if(wsdStatus != NOT_CHANGED_STATUS) {
      return true;
    }
    if(docStatus != NOT_CHANGED_STATUS) {
      return true;
    }
    if(wsDeploymentDescriptorStatus!= NOT_CHANGED_STATUS) {
      return true;
    }
    if(wsdlStatus != NOT_CHANGED_STATUS) {
      return true;
    }
    if(javaToQNameMappingStatus != NOT_CHANGED_STATUS) {
      return true;
    }

    return false;
  }
    
  public String toString() {
    String nl = System.getProperty("line.separator");
    StringBuffer strBuffer = new StringBuffer();

    strBuffer.append("Status VI: " + getStatusString(visStatus) + nl);
    strBuffer.append("Status WSD: " + getStatusString(wsdStatus) + nl);
    strBuffer.append("Status documentation: " + getStatusString(docStatus) + nl);
    strBuffer.append("Status WSDD: " + getStatusString(wsDeploymentDescriptorStatus) + nl);
    strBuffer.append("Status WSDL: " + getStatusString(wsdlStatus) + nl);
    strBuffer.append("Status java to qname mapping: " + getStatusString(javaToQNameMappingStatus) + nl);

    return strBuffer.toString();
  }

  private String getStatusString(int status) {
    String statusString = "";
    switch(status) {
      case 0: {
        statusString = "NOT_CHANGED";
        break;
      }
      case 1: {
        statusString = "CHANGED";
        break;
      }
       case 2: {
        statusString = "ADDED";
        break;
      }
      case 3: {
        statusString = "DELETED";
        break;
      }
      default: {
        statusString = "UNRECOGNISED";
        break;
      }
    }

    return statusString;
  }

}
