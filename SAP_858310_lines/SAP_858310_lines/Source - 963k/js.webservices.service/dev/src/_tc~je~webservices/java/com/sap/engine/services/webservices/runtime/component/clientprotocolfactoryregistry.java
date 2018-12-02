package com.sap.engine.services.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.component.BaseFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;
import com.sap.engine.services.webservices.exceptions.RegistryException;

/**
 * Title:  
 * Description: 
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ClientProtocolFactoryRegistry extends ComponentRegistry {

  public ClientProtocolFactoryRegistry() {

  }

  public ClientProtocolFactory getClientProtocolFactory(String id) throws RegistryException {
    return (ClientProtocolFactory)getComponent(id);
  }

  public ClientProtocolFactory[] listClientProtocolFactories() {
    BaseFactory[] baseFactories =  listComponents();
    int factoriesSize = baseFactories.length;
    ClientProtocolFactory[] clientProtocolFactories = new ClientProtocolFactory[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) clientProtocolFactories[i] = (ClientProtocolFactory)baseFactories[i];
    return clientProtocolFactories;
  }

  public void  registerClientProtocolFactory(String id, ClientProtocolFactory clientProtocolFactory) throws RegistryException {
    registerComponent(id, clientProtocolFactory);
  }

  public void unregisterClientProtocolFactory(String id) {
    unregisterComponent(id);
  }

}