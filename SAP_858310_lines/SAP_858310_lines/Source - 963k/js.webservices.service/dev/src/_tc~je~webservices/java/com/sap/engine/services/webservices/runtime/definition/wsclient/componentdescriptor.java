package com.sap.engine.services.webservices.runtime.definition.wsclient;

import com.sap.engine.services.webservices.webservices630.server.deploy.common.WSBaseConstants;

import java.io.Serializable;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ComponentDescriptor implements Serializable {
  private String name = null;
  private String jndiName = null;

  public ComponentDescriptor() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasName() {
    return name != null;
  }

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }

  public boolean hasJndiName() {
    return jndiName != null;
  }

  public String toString() {
    String nl = WSBaseConstants.LINE_SEPARATOR;
    String result = "";
    if (hasName()) {
      result += "Name:      " + name + nl;
    }
    if (hasJndiName()) {
      result += "Jndi name: " + jndiName + nl;
    }
    return result;
  }

}
