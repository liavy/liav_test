package com.sap.engine.services.webservices.webservices630.server.deploy.ws.update;

import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSArchiveLocationWrapper;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.preprocess.WSFileLocationWrapper;

import java.util.Hashtable;

/**
 * Title: WSUpdateInfo
 * Description: The class is a container for  deleted, newly deployed and updated web services.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSUpdateInfo {

  private WSFileLocationWrapper[] wsForDeleteFileLocationWrappers = new WSFileLocationWrapper[0];
  private WSArchiveLocationWrapper[] wsForDeployArchiveLocationWrappers = new WSArchiveLocationWrapper[0];
  private Hashtable wsForUpdateArchiveLocationWrappers = new Hashtable();
  private Hashtable wsForUpdateFileLocationWrappers = new Hashtable();

  public WSUpdateInfo() {
  }

  public WSFileLocationWrapper[] getWsForDeleteFileLocationWrappers() {
    return wsForDeleteFileLocationWrappers;
  }

  public void setWsForDeleteFileLocationWrappers(WSFileLocationWrapper[] wsForDeleteFileLocationWrappers) {
    this.wsForDeleteFileLocationWrappers = wsForDeleteFileLocationWrappers;
  }

  public WSArchiveLocationWrapper[] getWsForDeployArchiveLocationWrappers() {
    return wsForDeployArchiveLocationWrappers;
  }

  public void setWsForDeployArchiveLocationWrappers(WSArchiveLocationWrapper[] wsForDeployArchiveLocationWrappers) {
    this.wsForDeployArchiveLocationWrappers = wsForDeployArchiveLocationWrappers;
  }

  public Hashtable getWsForUpdateArchiveLocationWrappers() {
    return wsForUpdateArchiveLocationWrappers;
  }

  public void setWsForUpdateArchiveLocationWrappers(Hashtable wsForUpdateArchiveLocationWrappers) {
    this.wsForUpdateArchiveLocationWrappers = wsForUpdateArchiveLocationWrappers;
  }

  public Hashtable getWsForUpdateFileLocationWrappers() {
    return wsForUpdateFileLocationWrappers;
  }

  public void setWsForUpdateFileLocationWrappers(Hashtable wsForUpdateFilesLocationWrappers) {
    this.wsForUpdateFileLocationWrappers = wsForUpdateFilesLocationWrappers;
  }

}
