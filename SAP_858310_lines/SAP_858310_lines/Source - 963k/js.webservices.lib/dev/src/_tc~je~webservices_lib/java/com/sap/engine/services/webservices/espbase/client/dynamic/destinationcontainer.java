package com.sap.engine.services.webservices.espbase.client.dynamic;

import java.util.Hashtable;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;

/**
 * Configuration and Web service description container for use in conjunction with the Dynamic Proxy API.
 * This container is a temporary WSDL description and Configuration storage and does not provide persistency.
 * The stored entities can be reffered as Meatadata destinations and Logical Destinations in the Dynamic Proxy API.
 * These destinations should have unique non conflicting names and can not overlap actual destinations made by single configuraion
 * API. 
 * The class provides singleton factory access method and is internally synchronized. 
 * @author Chavdar Baikov (i024072)
 */
public class DestinationContainer {
  
  private int counter;
  private Hashtable<String,BindingData> destinationMap;
  private Hashtable<String,Definitions> wsdlMap;
  private static final DestinationContainer CONTATINER = new DestinationContainer();
  
  public DestinationContainer() {
    this.counter = 0;
    this.destinationMap = new Hashtable<String,BindingData>();
    this.wsdlMap = new Hashtable<String,Definitions>();    
  }
    
  private synchronized String getUniqueId() {
    counter++;
    String result = " id_"+Integer.toString(counter);
    return result;
  }
  
  /**
   * Adds Logical Destination to the destination container.
   * Unique Destination name is automatically created and returned.
   * This id should be used to query the Logical Destination at later time.
   * The logical destination should not be changed when invocation that uses the same
   * The returned id is used to query DInterface.getInterfaceInvoker(String destName) method. 
   * destination is done. 
   * @param bData
   * @return
   */
  public String addDestination(BindingData bData) {
    String uniqueId = getUniqueId();
    destinationMap.put(uniqueId,bData);
    return uniqueId;
  }
  
  /**
   * Adds WSDL source destination. Returns unique destination id to reference this metadata.
   * The returned id used to query GenericServiceFactory.createService(String lmtName, QName interfaceName) method.
   * @param definitions
   * @return
   */
  public String addWSDLDestination(Definitions definitions) {
    String uniqueId = getUniqueId();
    wsdlMap.put(uniqueId,definitions);
    return uniqueId;
  }
  
  /**
   * Returns Logical Destination configuration for specific destination id.
   * @param destinationId
   * @return
   */
  public BindingData getDestination(String destinationId) {
    return destinationMap.get(destinationId);
  }
  
  /**
   * Returns WSDL description of specific metadata id.
   * @param destinationId
   * @return
   */
  public Definitions getWSDLDestination(String destinationId) {
    return wsdlMap.get(destinationId);
  }
  
  /**
   * Releases destination resource.
   * @param destinationId
   * @return
   */
  public String releaseDestination(String destinationId) {
    Object temp = destinationMap.remove(destinationId);
    if (temp != null) {
      return destinationId;
    }      
    temp = wsdlMap.remove(destinationId);
    if (temp != null) {
      return destinationId;
    }            
    return null;
  }  
  
  /**
   * Checks if specific destination is existing.
   * @param destinationId
   * @return
   */
  public boolean containsDestination(String destinationId) {
    if (destinationMap.containsKey(destinationId)) {
      return true;
    }
    if (wsdlMap.containsKey(destinationId)) {
      return true;
    }
    return false;
  }
  
  /**
   * Check if specific destination is WSDL destination.
   * If the destination exists but it is not WSDL destination then it is logical destination.
   * @param destinationId
   * @return
   */
  public boolean isWSDLDestination(String destinationId) {
    if (wsdlMap.containsKey(destinationId)) {
      return true;
    }
    return false;    
  }   
  
  /**
   * Clears WSDL destinations.
   */
  public void clearWSDLDestinations() {
    wsdlMap.clear();  
  }
  
  /**
   * Clears logical destinations.
   */
  public void clearDestinations() {
    destinationMap.clear(); 
  }
  
  /**
   * Returns DestinationContainer instance.
   * @return
   */
  public static DestinationContainer getDestinationContainer() {
    return CONTATINER;
  }
  
}
