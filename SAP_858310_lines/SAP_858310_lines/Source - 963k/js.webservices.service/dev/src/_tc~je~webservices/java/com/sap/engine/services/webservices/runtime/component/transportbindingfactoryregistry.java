package com.sap.engine.services.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.runtime.component.BaseFactory;
import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.services.webservices.exceptions.RegistryException;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */
public class TransportBindingFactoryRegistry extends ComponentRegistry {

  public TransportBindingFactoryRegistry() {

  }

  public TransportBindingFactory getTransportBindingFactory(String id) throws RegistryException {
    return (TransportBindingFactory)getComponent(id);
  }

  public TransportBindingFactory[] listTransportBindingFactories() {
    BaseFactory[] baseFactories =  listComponents();
    int factoriesSize = baseFactories.length;
    TransportBindingFactory[] trBindingFactories = new TransportBindingFactory[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) trBindingFactories[i] = (TransportBindingFactory)baseFactories[i];
    return trBindingFactories;
  }

  public void registerTransportBindingFactory(String id, TransportBindingFactory transportBindingFactory) throws RegistryException {
    registerComponent(id, transportBindingFactory);
  }

  public void unregisterTransportBindingFactory(String id) {
    unregisterComponent(id);
  }
}

