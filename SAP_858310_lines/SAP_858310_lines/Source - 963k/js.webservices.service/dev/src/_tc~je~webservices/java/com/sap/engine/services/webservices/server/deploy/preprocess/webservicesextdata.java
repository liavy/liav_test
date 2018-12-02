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

import java.util.Hashtable;

/**
 * Title: WebServicesExtData
 * Description: Class that contains all necessary values of the properties from the webservices-j2ee-engine-ext.xml
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class WebServicesExtData {
  private String contextRoot       = null;
  private Hashtable bindingData    = null;
  private String webResourceName   = null;
  private String realmName         = "default";//default value
  private String webserviceName    = null;

	public Hashtable getBindingData() {
		return bindingData;
	}
  
	public String getContextRoot() {
		return contextRoot;
	}

	public String getRealmName() {
		return realmName;
	}

	public void setBindingData(Hashtable table) {
		bindingData = table;
	}

	public void setContextRoot(String string) {
		contextRoot = string;
	}

	public void setRealmName(String string) {
		realmName = string;
	}

	public String getWebResourceName() {
		return webResourceName;
	}

	public void setWebResourceName(String string) {
		webResourceName = string;
	}
    
    public void setWebserviceName(String webserviceName) {
        this.webserviceName = webserviceName;
    }
    
    public String getWebServiceName() {
        return webserviceName;
    }

}
