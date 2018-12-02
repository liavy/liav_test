/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.server.deploy.preprocess;

/**
 * Title: BindingExtData
 * Description: 
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class BindingExtData {
  private String urlPattern        = "/*";//default value
  private String authMethod        = null;
  private String transportGarantee = null;
  private String httpMethod        = null;
  private String roleName          = null;
  private String bindingDataName   = null;
  
  public String getAuthMethod() {
    return authMethod;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getTransportGarantee() {
    return transportGarantee;
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public void setAuthMethod(String string) {
    authMethod = string;
  }

  public void setHttpMethod(String string) {
    httpMethod = string;
  }
  
  public void setRoleName(String string) {
    roleName = string;
  }

  public void setTransportGarantee(String string) {
    transportGarantee = string;
  }

  public void setUrlPattern(String string) {
    urlPattern = string;
  }

	public String getBindingDataName() {
		return bindingDataName;
	}

	public void setBindingDataName(String string) {
		bindingDataName = string;
	}

}
