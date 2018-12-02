package com.sap.engine.services.webservices.runtime.component;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.TransportBinding;
import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientProtocolFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ClientTransportBindingFactory;
import com.sap.engine.interfaces.webservices.runtime.component.ComponentFactory;
import com.sap.engine.interfaces.webservices.runtime.component.TransportBindingFactory;
import com.sap.engine.services.webservices.exceptions.BaseComponentInstantiationException;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.exceptions.RegistryException;
import com.sap.engine.services.webservices.server.WSContainer;


/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ComponentFactoryImpl implements ComponentFactory, ClientComponentFactory {

//  public Protocol getProtocolInstance(String id) throws BaseComponentInstantiationException {
//    Protocol protocolInterface = null;
//    try {
//      ProtocolFactory protocolFactory = WSContainer.getProtocolFactoryRegistry().getProtocolFactory(id);
//      protocolInterface = protocolFactory.newInstance();
//    } catch (RegistryException e) {
//
//      Object[] args = new Object[]{"there is no protocol factory registered", id, "protocol"};
//      throw new BaseComponentInstantiationException(PatternKeys.COMPONENT_INSTANTIATION, args, e);
//    }
//    return protocolInterface;
//  }

  public TransportBinding getTransportBindingInstance(String id) throws BaseComponentInstantiationException {
    com.sap.engine.interfaces.webservices.runtime.TransportBinding transportBindingInterface = null;
    try {
      TransportBindingFactory transportBindingFactory =
        WSContainer.getTransportBindingFactoryRegistry().getTransportBindingFactory(id);
      transportBindingInterface = transportBindingFactory.newInstance();
    } catch (RegistryException e) {

      Object[] args = new Object[]{"there is no transport binding factory registered", id, "transport binding"};
      throw new BaseComponentInstantiationException(PatternKeys.COMPONENT_INSTANTIATION, args, e);
    }
    return transportBindingInterface;
  }

  public String[] listProtocolIds() {
    //return WSContainer.getProtocolFactoryRegistry().listComponentIds();
    return new String[0];
  }

  public String[] listTransportBindingIds() {
    return WSContainer.getTransportBindingFactoryRegistry().listComponentIds();
  }

//  public Protocol[] listProtocolInterfaces() {
//    ProtocolFactory[] factories =
//      WSContainer.getProtocolFactoryRegistry().listProtocolFactories();
//    int factoriesSize = factories.length;
//    Protocol[] interfaces = new Protocol[factoriesSize];
//    for (int i = 0; i < factoriesSize; i++) interfaces[i] = factories[i].newInstance();
//    return interfaces;
//  }

  public TransportBinding[] listTransportBindingInterfaces() {
    TransportBindingFactory[] factories =
      WSContainer.getTransportBindingFactoryRegistry().listTransportBindingFactories();
    int factoriesSize = factories.length;
    com.sap.engine.interfaces.webservices.runtime.TransportBinding[] interfaces = new com.sap.engine.interfaces.webservices.runtime.TransportBinding[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) interfaces[i] = factories[i].newInstance();
    return interfaces;
  }

  public ClientFeatureProvider getClientProtocolInstance(String id) throws BaseComponentInstantiationException {
    ClientFeatureProvider clientProtocolInterface = null;
    try {
      ClientProtocolFactory clientProtocolFactory =
        WSContainer.getClientProtocolFactoryRegistry().getClientProtocolFactory(id);
       clientProtocolInterface =  clientProtocolFactory.newInstance();
    } catch (RegistryException e) {

      Object[] args = new Object[]{"there is no client protocol factory registered", id, "client protocol"};
      throw new BaseComponentInstantiationException(PatternKeys.COMPONENT_INSTANTIATION, args, e);
    }
    return clientProtocolInterface;
  }

  public ClientFeatureProvider getClientTransportBindingInstance(String id) throws BaseComponentInstantiationException {
    ClientFeatureProvider clientFeatureProviderInterface = null;
    try {
      ClientTransportBindingFactory clientTransportBindingFactory =
        WSContainer.getClientTransportBindingFactoryRegistry().getClientTransportBindingFactory(id);
      clientFeatureProviderInterface = clientTransportBindingFactory.newInstance();
    } catch (RegistryException e) {

      Object[] args = new Object[]{"there is no client transport binding factory registered", id, "client transport binding"};
      throw new BaseComponentInstantiationException(PatternKeys.COMPONENT_INSTANTIATION, args, e);
    }
    return clientFeatureProviderInterface;
  }

  public String[] listClientProtocolIds() {
    return WSContainer.getClientProtocolFactoryRegistry().listComponentIds();
  }

  public String[] listClientTransportBindingIds() {
    return WSContainer.getClientTransportBindingFactoryRegistry().listComponentIds();
  }

  public ClientFeatureProvider[] listClientProtocolInterfaces()  {
    ClientProtocolFactory[] factories =  WSContainer.getClientProtocolFactoryRegistry().listClientProtocolFactories();

    int factoriesSize = factories.length;
    ClientFeatureProvider[] interfaces = new ClientFeatureProvider[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) {
      interfaces[i] = factories[i].newInstance();
    }

    return interfaces;
  }

  public ClientFeatureProvider[] listClientransportBindingInterfaces() {
    ClientTransportBindingFactory[] factories =
      WSContainer.getClientTransportBindingFactoryRegistry().listClientTransportBindingFactories();
    int factoriesSize = factories.length;
    ClientFeatureProvider[] interfaces = new ClientFeatureProvider[factoriesSize];
    for (int i = 0; i < factoriesSize; i++) interfaces[i] = factories[i].newInstance();
    return interfaces;
  }

}

