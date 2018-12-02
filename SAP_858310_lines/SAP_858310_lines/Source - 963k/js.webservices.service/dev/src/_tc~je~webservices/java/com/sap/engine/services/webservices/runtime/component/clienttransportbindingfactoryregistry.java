package com.sap.engine.services.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.component.BaseFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;
import com.sap.engine.services.webservices.exceptions.RegistryException;
/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ClientTransportBindingFactoryRegistry extends ComponentRegistry {

  public ClientTransportBindingFactoryRegistry() {

  }

  public ClientTransportBindingFactory getClientTransportBindingFactory(String id) throws RegistryException {
    return (ClientTransportBindingFactory)getComponent(id);
  }

  public ClientTransportBindingFactory[] listClientTransportBindingFactories() {
    BaseFactory[] baseFactories =  listComponents();
    int factoriesSize = baseFactories.length;
    ClientTransportBindingFactory[] clientTrBindingFactories = new ClientTransportBindingFactory[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) clientTrBindingFactories[i] = (ClientTransportBindingFactory)baseFactories[i];
    return clientTrBindingFactories;
  }

  public void registerClientTransportBindingFactory(String id, ClientTransportBindingFactory clientTrBindingFactory) throws RegistryException {
    registerComponent(id, clientTrBindingFactory);
  }

  public void unregisterClientTransportBindingFactory(String id) {
    unregisterComponent(id);
  }

}