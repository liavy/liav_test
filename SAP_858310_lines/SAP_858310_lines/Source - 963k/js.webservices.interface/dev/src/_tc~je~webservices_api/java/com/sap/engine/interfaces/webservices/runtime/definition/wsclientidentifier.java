package com.sap.engine.interfaces.webservices.runtime.definition;

import com.sap.engine.interfaces.webservices.runtime.definition.WSBaseIdentifier;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSClientIdentifier extends WSBaseIdentifier {
  public WSClientIdentifier(String applicationName, String jarName, String serviceRefName) {
    super(applicationName, jarName, serviceRefName);
  }

  public WSClientIdentifier() {
    super();
  }

  public WSClientIdentifier(WSClientIdentifier wsClientId) {
    super(wsClientId.getApplicationName(), wsClientId.getJarName(), wsClientId.getComponentName());
  }

  public String getServiceRefName() {
    return getComponentName();
  }

  public void setServiceRefName(String serviceRefName) {
    setComponentName(serviceRefName);
  }

  public String toString() {
    String nl = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer();
    buf.append(nl);
    buf.append("Application      : ");
    buf.append(getApplicationName());
    buf.append(nl);
    buf.append("Module           : ");
    buf.append(getJarName());
    buf.append(nl);
    buf.append("Service reference: ");
    buf.append(getComponentName());
    return buf.toString();
  }

  public String toStringID() {
    StringBuffer buf = new StringBuffer();
    buf.append(getApplicationName());
    buf.append("|");    
    buf.append(getJarName());    
    buf.append("|");    
    buf.append(getComponentName());    
    return buf.toString();
  }
  
  public static WSClientIdentifier fromStringID(String stringID) {
    StringTokenizer tokenizer = new StringTokenizer(stringID, "|");
    if (!tokenizer.hasMoreTokens()) {
      throw new NoSuchElementException("Application Name missing in stringID: " + stringID);
    }
    String applicationName = tokenizer.nextToken();
    if (!tokenizer.hasMoreTokens()) {
      throw new NoSuchElementException("Module Name missing in stringID: " + stringID);
    }
    String jarName = tokenizer.nextToken();
    if (!tokenizer.hasMoreTokens()) {
      throw new NoSuchElementException("Service Reference Name missing in stringID: " + stringID);
    }
    String serviceName = tokenizer.nextToken();
    return new WSClientIdentifier(applicationName, jarName, serviceName);
  }

}