package com.sap.engine.interfaces.webservices.server;

/**
 * Title: WSClientStructure
 * Description: Holds the ws client unique information
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientStructure {

  private String applicationName = null;
  private String archiveName = null;
  private String proxyName = null;


  public WSClientStructure() {

  }

  /**
   * @return String  The application name of the ws client
   */

  public String getApplicationName() {
    return applicationName;
  }

  /**
   * Set the application name of the ws client
   * @param applicationName
   */

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * @return String  The archive name of the ws client
   */

  public String getArchiveName() {
    return archiveName;
  }

  /**
   * Set the archive name of the ws client
   * @param archiveName
   */

  public void setArchiveName(String archiveName) {
    this.archiveName = archiveName;
  }

  /**
   * @return String  The proxy name of the ws client, the name under which it is binded into JNDI
   */

  public String getProxyName() {
    return proxyName;
  }

  /**
   * Set the proxy name of the ws client
   * @param proxyName
   */

  public void setProxyName(String proxyName) {
    this.proxyName = proxyName;
  }

}
