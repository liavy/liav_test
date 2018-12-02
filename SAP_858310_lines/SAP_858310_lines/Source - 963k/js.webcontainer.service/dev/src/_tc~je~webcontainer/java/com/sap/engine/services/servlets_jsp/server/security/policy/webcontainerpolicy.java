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

import java.util.HashMap;

import com.sap.engine.services.servlets_jsp.server.exceptions.SecuritySessionException;

public class WebContainerPolicy {  
  private HashMap<String, PolicyDomain> policyDomains= new HashMap<String, PolicyDomain>();
  //private ServiceContext serviceContext=null;
  
//  public WebContainerPolicy(ServiceContext serviceContext) {
//        
//  }

  public WebContainerPolicy() {
  
  }
    
  public synchronized PolicyDomain createDomain(String domainName, String appName) {    
    PolicyDomain policyDomain = policyDomains.get(domainName);
    if (policyDomain == null) {
      policyDomain = new PolicyDomain(domainName, appName);
      //add the created policy domain to policy domain collection      
      policyDomains.put(domainName, policyDomain);
    }
    else {
      
      policyDomain.addApplication(appName);
    }
    return policyDomain;
  }

  public synchronized void destroyDomain(String domainName, String appName) throws SecuritySessionException {
 
    PolicyDomain domain = policyDomains.get(domainName);
    if (domain == null) {
      throw new SecuritySessionException(
          SecuritySessionException.CANNOT_DESTROY_NOT_EXISTING_DOMAIN,
          new Object[] { domainName });
    }
    domain.removeApplicationName(appName);
    
    if (domain.isEmpty()) {      
      policyDomains.remove(domainName);
    }
  
  }

  public synchronized String dump(boolean full, String nl) {
    
    StringBuffer result = new StringBuffer();
    result.append("Web Security Policy Domains:" + nl);
    result.append(dump("", full, nl));
    return result.toString();
  }
  
  private String dump(String tab, boolean full, String nl) {
    StringBuffer result = new StringBuffer();
    for(String currKey : policyDomains.keySet()) {
      PolicyDomain policyDomain = policyDomains.get(currKey);
      result.append(nl);
      result.append(tab + "[" + policyDomain.getName() + "]");
      result.append(nl);
      if (full) {
        result.append(policyDomain.dumpApplications(tab, nl));
      }
    }
    
    return result.toString();
  }
  
}
