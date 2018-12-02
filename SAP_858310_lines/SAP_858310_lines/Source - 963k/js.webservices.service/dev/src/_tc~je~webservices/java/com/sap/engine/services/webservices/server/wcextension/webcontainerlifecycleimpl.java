package com.sap.engine.services.webservices.server.wcextension;

import java.util.Hashtable;

import com.sap.engine.services.servlets_jsp.webcontainer_api.container.IWebContainerLifecycle;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionWarningException;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.IWebModuleContext;
import com.sap.engine.services.servlets_jsp.webcontainer_api.module.ServletDeclaration;

public class WebContainerLifecycleImpl implements IWebContainerLifecycle{
  
  public void onStart(IWebModuleContext moduleContext) throws WebContainerExtensionDeploymentException, WebContainerExtensionWarningException {
  }

  
  public void onStop(IWebModuleContext moduleContext) throws WebContainerExtensionWarningException {
  }
}
