package com.sap.engine.interfaces.webservices.server;

import java.util.Vector;

import com.sap.engine.interfaces.webservices.esp.ImplementationContainer;
import com.sap.engine.interfaces.webservices.runtime.component.BaseRegistryException;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ProtocolFactory;
import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.interfaces.webservices.server.event.EventContext;
import com.sap.engine.interfaces.webservices.server.management.WSManager;
import com.sap.engine.interfaces.webservices.server.management.exception.WSBaseException;

/**
 * Title: WebServiceInterface
 * Description: WebServiceInterface is the interface, used for communication with the other services.
 * It provides methods for registering additional webservices components, used in runtime processing.
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface WebServiceInterface extends WSManager {

  /**
   * The provider interface name of the WebServiceInterface
   */
  public static final String NAME = "webservices";

  /**
   * Registers a ProtocolFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ProtocolFactory instance is going to be registered.
   * @param   protocolFactory The ProtocolFactory instance, that is going to be registered.
   * @exception   BaseRegistryException Thrown in case a protocol factory with the same identifier has been already registered.
   */
  public void registerProtocolFactory(String id, ProtocolFactory protocolFactory) throws BaseRegistryException;

  /**
   * UnRegisters a ProtocolFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ProtocolFactory instance has been registered.
   */
  public void unregisterProtocolFactory(String id);

  /**
   * Registers a TransportBindingFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the TransportBindingFactory instance is going to be registered.
   * @param   transportBindingFactory The TransportBindingFactory instance, that is going to be registered.
   * @exception   BaseRegistryException Thrown in case a transport binding factory with the same identifier has been already registered.
   */
  public void registerTransportBindingFactory(String id, TransportBindingFactory transportBindingFactory) throws BaseRegistryException;

  /**
   * Registers a TransportBindingFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the TransportBindingFactory instance has been registered.
   */
  public void unregisterTransportBindingFactory(String id);

  /**
   * Unregisters a ClientProtocolFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ClientProtocolFactory instance is going to be registered.
   * @param   clientProtocolFactory The ClientProtocolFactory instance, that is going to be registered.
   * @exception   BaseRegistryException Thrown in case a client protocol factory with the same identifier has been already registered.
   */
  public void registerClientProtocolFactory(String id, ClientProtocolFactory clientProtocolFactory) throws BaseRegistryException;

  /**
   * Unregisters a ClientProtocolFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ClientProtocolFactory instance has been registered.
   */
  public void unregisterClientProtocolFactory(String id);

  /**
   * Registers a ClientTransportBindingFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ClientTransportBindingFactory instance is going to be registered.
   * @param   clientTransportBindingFactory The ClientTransportBindingFactory instance, that is going to be registered.
   * @exception   BaseRegistryException Thrown in case a client transport binding factory with the same identifier has been already registered.
   */
  public void registerClientTransportBindingFactory(String id, ClientTransportBindingFactory clientTransportBindingFactory) throws BaseRegistryException;

  /**
   * Unregisters a ClientTransportBindingFactory component in the webservices runtime framework.
   * @param   id The identifier, under which the ClientTransportBindingFactory instance has been registered.
   */
  public void unregisterClientTransportBindingFactory(String id);

  /**
   * Registers an ImplementationContainer component in the webservices runtime framework.
   * @param   implementationContainerInterface The ImplementationContainer instance, that is going to be registered.
   * @exception   BaseRegistryException Thrown in case an implementation container interface with the same identifier has been already registered.
   */
  public void registerImplementationContainerInterface(ImplementationContainer implementationContainerInterface) throws BaseRegistryException;

  /**
   * Unregisters an ImplementationContainer component in the webservices runtime framework.
   * @param  id The identifier, under which the ImplementationContainer  has been registered.
   */
  public void unregisterImplementationContainerInterface(String id);

  /**
   * The methods returns the event context.
   * @return EventContext - the event context.
   */
  public EventContext getEventContext();

  /**
   * Lists all started ws clients on the current cluster node.
   * @return WSClientStructure[]
   */
  public WSClientStructure[] listWSClients();

  /**
   * Lists all started webservices on the current cluster node.
   * @return WebServiceStructure[]
   */
  public WebServiceStructure[] listWebServices();

  /**
   * This method has to be used in order to update a logical port settings runtime. 
   * 
   * @param applicationName The name of the application, where the WSClient is located
   * @param jndiName The jndiName, under which the ServiceInterface is bound
   * @param lpName The name of the logical port that will be updated
   * @param sldLP Information about SLD entries
   * @return The updated LogicalPortType structure
   * @throws WSBaseException If an error has occured
   */
  public Object updateWSClientFromSLD(String applicationName, String jndiName, String lpName, SLDLogicalPort sldLP) throws WSBaseException;
  
  /**
   * This method has to be used in order to update the endpoint URL runtime. 
   * 
   * @param applicationName The name of the application, where the WSClient is located
   * @param jndiName The jndiName, under which the ServiceInterface is bound
   * @param lpName The name of the logical port that will be updated
   * @param newURL The new endpoint URL
   * @throws WSBaseException If an error has occurred
   */
  public void changeEndpointURL(String applicationName, String jndiName, String lpName, String newURL) throws WSBaseException;
  
  /**
   * This method returns all published SAP_J2EEEngineCluster nodes in SLD
   * 
   * @return A table with System IDs. The returned Vector contains other Vectors, which elements are SystemID (index 0) and SystemCaption (index 1)
   * @throws WSBaseException If any error has occured
   */
  public Vector getAvailableSLDSystems() throws WSBaseException;
  
  /**
   * This method returns all published SAP_WebService nodes in SLD per specific SAP_J2EEEngineCluster
   * 
   * @param systemID The ID/Name of the system 
   * @return A table with WS IDs. The returned Vector contains other Vectors, which elements are wsID (index 0) and wsCaption (index 1)
   * @throws WSBaseException If any error has occured
   */
  public Vector getAvailableSLDWebServices(String systemID) throws WSBaseException;
  
  /**
   * This method returns all published SAP_WebServicePort nodes in SLD per specific SAP_J2EEEngineCluster and SAP_WebService
   * 
   * @param systemID The ID/Name of the system 
   * @param wsID The ID/Name of the Web service entry 
   * @return A table with WS Port IDs. The returned Vector contains other Vectors, which elements are wsPortID (index 0) and wsPortCaption (index 1)
   * @throws WSBaseException
   */
  public Vector getAvailableSLDWSPorts(String systemID, String wsID) throws WSBaseException;
  
  /**
   * Provides access to ServiceContextAccessHandler object.
   */
  public ServiceContextAccessHandler getServiceContextAccessHandler();
  
  public WebServicesContainerManipulator getWebServicesContainerManipulator(boolean isConsumer);
}
