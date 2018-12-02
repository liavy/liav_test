package com.sap.engine.services.sca.plugins.ws;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.interfaces.sca.SCAEnvironment;
import com.sap.engine.services.sca.plugins.ws.dii.DynamicInvocationInterfacePlugin;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class WebServicePluginFrame
{
  static final Location LOCATION = Location.getLocation(WebServicePluginFrame.class.getName());
  
  private static SCAEnvironment 		iFacade		= null;
  private static WebServicePluginFrame 		iPluginFrame 	= null;
  private WebServicePlugin 			iJaxWsPlugin	= null;
  private DynamicInvocationInterfacePlugin	iDiiPlugin	= null;
  private ApplicationServiceContext 		iContext	= null;
  
		
  public static SCAEnvironment getFacade() {
    return iFacade;
  }
  
  public static WebServicePluginFrame getPlugin() {
    return iPluginFrame;
  }
	
  public void start(ApplicationServiceContext aContext) throws ServiceException {
    LOCATION.logT(Severity.PATH, "Starting WS SCA Plugin.");
    
    iJaxWsPlugin = new WebServicePlugin();
    iDiiPlugin = new DynamicInvocationInterfacePlugin();
    iPluginFrame = this;
    
    // register as SCA plugin
    iContext = aContext;
    iFacade = (SCAEnvironment) iContext.getContainerContext().getObjectRegistry().getProvidedInterface("tc~je~sca~spi");
    if (iFacade != null) {
      iFacade.getSCAExtensionRegistry().register(iDiiPlugin);
      iFacade.getSCAExtensionRegistry().register(iJaxWsPlugin);
    } else {
      throw new ServiceException("WS SCA Plugin cannot start because it refereces tc~je~sca~spi interface which is not started yet.");
    }
    
    LOCATION.logT(Severity.PATH, "WS SCA Plugin started successfully.");
  }
  
  public void stop() throws ServiceRuntimeException
  {
    LOCATION.logT(Severity.PATH, "Stopping WS SCA Plugin.");
    
    SCAEnvironment facade = (SCAEnvironment) iContext.getContainerContext().getObjectRegistry().getProvidedInterface("tc~je~sca~spi");
    if (facade != null) {
      facade.getSCAExtensionRegistry().unregister(iDiiPlugin);
      facade.getSCAExtensionRegistry().unregister(iJaxWsPlugin);
    }
    
    iDiiPlugin = null;
    iJaxWsPlugin = null;

    LOCATION.logT(Severity.PATH, "WS SCA Plugin stopped successfully.");
  }

  public ApplicationServiceContext getServiceContext() {	
    return iContext;
  }
}
