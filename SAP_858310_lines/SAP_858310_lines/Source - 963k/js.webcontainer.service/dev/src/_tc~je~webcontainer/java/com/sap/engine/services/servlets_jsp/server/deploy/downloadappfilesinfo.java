package com.sap.engine.services.servlets_jsp.server.deploy;

import com.sap.engine.lib.io.hash.FolderCompareResult;

/**
 * 
 * @author I026706
 * @version 7.20
 */
public class DownloadAppFilesInfo {
  private String applicationName = "";
  private boolean downloadAll = false;
  private boolean downloadPartially = false;
  private String[] allAliases = null;
  private FolderCompareResult folderCompareResult = null;
  
  public DownloadAppFilesInfo(String applicationName, FolderCompareResult folderCompareResult, boolean downloadAll, boolean downloadPartially) {
    this.applicationName = applicationName;
    this.folderCompareResult = folderCompareResult;
    this.downloadAll = downloadAll;
    this.downloadPartially = downloadPartially;
  }//end of constructor

  public String getApplicationName() {
    return applicationName;
  }//end of getApplicationName()

  public FolderCompareResult getFolderCompareResult() {
    return folderCompareResult;
  }//end of getFolderCompareResult()

  public boolean isDownloadAll() {
    return downloadAll;
  }//end of isDownloadAll()

  public boolean isDownloadPartially() {
    return downloadPartially;
  }//end of isDownloadPartially()

  public String[] getAllAliases() {
    return allAliases;
  }//end of getAllAliases()

  public void setAllAliases(String[] allAliases) {
    this.allAliases = allAliases;
  }//end of setAllAliases(String[] aliasesCanonicalized) 
  
}//end of class
