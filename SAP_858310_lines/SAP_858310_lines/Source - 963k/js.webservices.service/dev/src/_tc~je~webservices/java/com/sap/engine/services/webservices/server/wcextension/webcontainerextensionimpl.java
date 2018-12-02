package com.sap.engine.services.webservices.server.wcextension;

import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerDeploy;
import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerLifecycle;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtension;
import com.sap.engine.services.servlets_jsp.webcontainer_api.extension.IWebContainerExtensionContext;

public class WebContainerExtensionImpl implements IWebContainerExtension{

  private IWebContainerExtensionContext webContainerExtensionContext = null;
  private WebContainerDeployImpl webContainerDeployImpl = null;
  private WebContainerLifecycleImpl webContainerLifecycleImpl = null;
  
  public void destroy() {
    webContainerExtensionContext = null;
    webContainerDeployImpl = null;
    webContainerLifecycleImpl = null;
  }

  public IWebContainerDeploy getWebDeployHandler() {
    return webContainerDeployImpl;
  }

  public IWebContainerLifecycle getWebLifecycleHandler() {
    return webContainerLifecycleImpl;
  }

  public IWebContainerExtensionContext getWebContainerExtensionContext() {
    return webContainerExtensionContext;
  }

  public void init(IWebContainerExtensionContext webContainerExtensionContext) throws WebContainerExtensionDeploymentException {
    this.webContainerExtensionContext = webContainerExtensionContext;
    webContainerDeployImpl = new WebContainerDeployImpl();
    webContainerLifecycleImpl = new WebContainerLifecycleImpl();
  }
}
