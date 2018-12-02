/*
* Copyright (c) 2006 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.DeployInfo;
import com.sap.tc.logging.Location;

import java.util.Hashtable;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class DeployInfoExtension extends DeployInfo {
	private static final Location traceLocation = LogContext.getLocationWebContainerProvider();

  private Hashtable wceInDeploy = new Hashtable();
  private WarningException warningException = null;

  public Hashtable getWceInDeploy() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfoExtension.getWceInDeploy(), get result: [" + this.wceInDeploy + "]."); 
		}
  	
    return wceInDeploy;
  }//end of getWceInDeploy()

  public void setWceInDeploy(Hashtable wceInDeploy) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfoExtension.setWceInDeploy(" + wceInDeploy + ")."); 
		}
  	
    this.wceInDeploy = wceInDeploy;
  }//end of setWceInDeploy(Vector wceInDeploy)

  public WarningException getWarningException() {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfoExtension.getWarningException(), get result: [" + this.warningException + "]."); 
		}
  	
    return warningException;
  }//end of getWarningException()

  public void setWarningException(WarningException warningException) {
  	if (traceLocation.beDebug()) {
			traceLocation.debugT("DeployInfoExtension.setWarningException(" + warningException + ")."); 
		}
  	
    this.warningException = warningException;
  }//end of setWarningException(WarningException warningException)
  
  /**
   * Returns a string representation of this object.
   * 
   * @return  a string representation of this object.
   */
  public String toString() {
  	StringBuilder builder = new StringBuilder();
  	builder.append(super.toString()).append(", \r\nadditional ");
  	builder.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(":");
  	builder.append("[");
  	builder.append("wceInDeploy=[").append(wceInDeploy).append("], ");
  	builder.append("warningException=").append(warningException);
  	builder.append("]");
  	return builder.toString();
  }//end of toString()

}//end of class
