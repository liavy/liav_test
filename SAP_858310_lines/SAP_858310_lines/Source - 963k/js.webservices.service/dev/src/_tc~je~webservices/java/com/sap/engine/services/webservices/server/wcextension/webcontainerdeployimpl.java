package com.sap.engine.services.webservices.server.wcextension;

import com.sap.engine.services.servlets_jsp.webcontainer_api.container.DeployInfo;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerDeploy;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModule;

public class WebContainerDeployImpl implements IWebContainerDeploy {

  public DeployInfo onDeploy(IWebModule arg0, String arg1) throws WebContainerExtensionDeploymentException,
      WebContainerExtensionWarningException {
    return new DeployInfo();
  }

  public void onRemove(IWebModule arg0) throws WebContainerExtensionDeploymentException,
      WebContainerExtensionWarningException {
    // TODO Auto-generated method stub

  }

}
