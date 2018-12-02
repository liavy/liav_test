/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.security.policy;

import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;

public class PolicyDomain { 
  private String name = null;  
  private ArrayObject applicationNames = new ArrayObject(2, 2);
  
  private ConcurrentHashMapObjectObject securitySessions = new ConcurrentHashMapObjectObject();

  protected PolicyDomain(String name, String appName) {
    this.name = name;    
    applicationNames.add(appName);
  }

  public String getName() {
    return name;
  }

  //use in the package (WebContainerPolicy)
  synchronized void addApplication(String appName) {
    if (!applicationNames.contains(appName)) {
      applicationNames.add(appName);
    }
  }

  public boolean removeMe(ApplicationSession appSession) {
    // TODO: Do it the right way
    if (true) return false;
    return false;
  }

  //TODO - the method is used in WebMonitoring and it is used only the size of the object
  //which is 0; it should be removed (more research)
  public ConcurrentHashMapObjectObject getSecuritySessions() {
    return securitySessions;
  }
  
  synchronized boolean removeApplicationName(String appName) {
    return applicationNames.remove(appName);
  }

  synchronized String dump(String tab, boolean full, String nl) {
    StringBuffer result = new StringBuffer();
    result.append(nl);
    result.append(tab + "[" + name + "]");
    result.append(nl);
    if (full) {
      if (applicationNames.size() > 0) {
        result.append(tab + "Web Applications: ");
        boolean first = true;
        for (int i = 0; i < applicationNames.size(); i++) {
          if (((String)applicationNames.elementAt(i)).length() != 0) {
            if (first) {
              result.append(applicationNames.elementAt(i));
              first = false;
            } else {
              result.append(", " + applicationNames.elementAt(i));
            }
          }
        }
        result.append(nl);
      }
      result.append(tab + "Security Sessions: " + securitySessions.size());
      result.append(nl);
    }

    return result.toString();
  }

  boolean isEmpty() {
    if (applicationNames.size() > 1 || applicationNames.size() == 1 && ((String)applicationNames.elementAt(0)).length() != 0) {
      return false;
    }

    return true;
  }
  
  synchronized String dumpApplications(String tab, String nl){
    StringBuffer result=new StringBuffer();
    if (applicationNames.size() > 0) {
      result.append(tab + "Web Applications: ");
      boolean first = true;
      for (int i = 0; i < applicationNames.size(); i++) {
        if (((String)applicationNames.elementAt(i)).length() != 0) {
          if (first) {
            result.append(applicationNames.elementAt(i));
            first = false;
          } else {
            result.append(", " + applicationNames.elementAt(i));
          }
        }
      }
      result.append(nl);
    }
    result.append(tab + "Security Sessions: " + securitySessions.size());
    result.append(nl);
    
    return result.toString();
    
  }
  
}
