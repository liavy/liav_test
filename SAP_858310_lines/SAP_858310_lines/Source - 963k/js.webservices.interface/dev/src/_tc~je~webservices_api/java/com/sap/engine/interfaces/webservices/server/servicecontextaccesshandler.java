package com.sap.engine.interfaces.webservices.server;
/**
 * Title: ServiceContextAccessHandler
 * Description: Interface providing access to the objects contained by 
 *              com.sap.engine.services.webservices.server.container.ws.ServiceContext and com.sap.engine.services.webservices.server.container.wsclients.ServiceRefContext.
 * Company: SAP Labs Sofia 
 * @author aneta-a
 * @deprecated Use WebServicesContainerManipulator in the same package
 */
public interface ServiceContextAccessHandler {
  
  public String[] getApplicationNames(boolean consumer);
  public Object[] getInterfaceDefinitionsByApplication(boolean consumer, String applicationName);
  public Object[] getServicesByApplication(boolean consumer, String applicationName);
  
  public Object[] getServicesPerInterface(boolean consumer, Object interfaceDefinition);

  public Object getInterfaceDefinitionByName(boolean consumer, String ifDefName);
  public String getApplicationOfInterface(boolean consumer, Object interfaceDefinition);

  public void createService(boolean consumer, Object service, String applicationName);

  public void removeService(boolean consumer, Object service, String applicationName);

  public void updateService(boolean consumer, Object service, String applicationName);

}
