package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.util.Hashtable;

import com.sap.engine.lib.descriptors.ws04wsdd.WSDeploymentDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeDescriptor;
import com.sap.engine.services.webservices.server.deploy.descriptors.ws.WebservicesType;

/**
 * Class used for collecting information from different ws-configurations
 * @author aneta-a
 */
public class WSMigrationData {
  
  private String jarName;
  private WSRuntimeDescriptor wsRuntimeDescriptor;
  private WSDeploymentDescriptor wsDeploymentDescriptor;
  private Hashtable vi_wsdStructures;
  private WebservicesType webservicesType;
  
  public WSMigrationData(String jarName) {
    this.jarName = jarName;
  }

	public WSDeploymentDescriptor getWsDeploymentDescriptor() {
		return wsDeploymentDescriptor;
	}

	public WSRuntimeDescriptor getWsRuntimeDescriptor() {
		return wsRuntimeDescriptor;
	}

	public void setWsDeploymentDescriptor(WSDeploymentDescriptor descriptor) {
		wsDeploymentDescriptor = descriptor;
	}

	public void setWsRuntimeDescriptor(WSRuntimeDescriptor descriptor) {
		wsRuntimeDescriptor = descriptor;
	}

	public Hashtable getVi_wsdStructures() {
		return vi_wsdStructures;
	}

	public void setVi_wsdStructures(Hashtable hashtable) {
		vi_wsdStructures = hashtable;
	}

	public String getJarName() {
		return jarName;
	}

	public WebservicesType getWebservicesType() {
		return webservicesType;
	}

	public void setWebservicesType(WebservicesType type) {
		webservicesType = type;
	}

}
