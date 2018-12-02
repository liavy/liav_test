package com.sap.engine.interfaces.webservices.server;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WebServiceStructure {
  private String applicationName = null;
  private String archiveName = null;
  private String webServiceName = null;

  public WebServiceStructure() {
  }

  /**
   * @return String  The application name of the web service
   */

  public String getApplicationName() {
    return applicationName;
  }

  /**
   * Set the application name of the web service
   * @param applicationName
   */

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * @return String  The archive name of the web service
   */

  public String getArchiveName() {
    return archiveName;
  }

  /**
   * Set the archive name of the web service
   * @param archiveName
   */

  public void setArchiveName(String archiveName) {
    this.archiveName = archiveName;
  }

  /**
   * @return String  The proxy name of the web service, the name under which it is binded into JNDI
   */

  public String getWebServiceName() {
    return webServiceName;
  }

  /**
   * Set the proxy name of the web service
   * @param webServiceName
   */

  public void setWebServiceName(String webServiceName) {
    this.webServiceName = webServiceName;
  }

}
