/*
 * Copyright (c) 2004-2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;


public class AppMetaData implements Serializable {

  private static final long serialVersionUID = 4829293420049917533L;
  private HashMap<String, Boolean> urlSessionTrackingPerModule = new HashMap<String, Boolean>();
  private Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule = new Hashtable<String, Hashtable<String, Vector<String>>>();
  private Hashtable<String, Vector<String>> wceInDeployPerModule = new Hashtable<String, Vector<String>>();
  
  public AppMetaData() {
  }

  public Hashtable<String, Hashtable<String, Vector<String>>> getResourcesPerModule() {
    return resourcesPerModule;
  }

  public void setResourcesPerModule(Hashtable<String, Hashtable<String, Vector<String>>> resourcesPerModule) {
    this.resourcesPerModule = resourcesPerModule;
  }

  public HashMap<String, Boolean> getUrlSessionTrackingPerModule() {
    return urlSessionTrackingPerModule;
  }

  public void setUrlSessionTrackingPerModule(HashMap<String, Boolean> urlSessionTrackingPerModule) {
    this.urlSessionTrackingPerModule = urlSessionTrackingPerModule;
  }

  public Hashtable<String, Vector<String>> getWceInDeployPerModule() {
    return wceInDeployPerModule;
  }

  public void setWceInDeployPerModule(Hashtable<String, Vector<String>> wceInDeployPerModule) {
    this.wceInDeployPerModule = wceInDeployPerModule;
  }

}
